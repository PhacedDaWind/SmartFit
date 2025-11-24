package com.example.smartfit.data.repository

import android.util.Log
import com.example.smartfit.BuildConfig
import com.example.smartfit.data.local.ChatDao
import com.example.smartfit.data.local.ChatMessageEntity
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ChatRepository(private val chatDao: ChatDao) {

    private val TAG = "ChatRepository"
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private val systemPrompt = """
        You are SmartFit AI, a fitness coach.
        
        STRICT RULES:
        1. Answer the user's question DIRECTLY. 
        2. DO NOT say "Hello" or "I am SmartFit AI" unless the user specifically asks "Who are you?".
        3. REQUIRED: If the user asks to SEE something, you MUST describe it in 1-2 sentences BEFORE generating the image prompt.
        4. REQUIRED: If the request implies an image (like "show me a salad"), add a new line at the very end with this format:
        IMAGE_PROMPT: [simple keyword for search]
        
        Example:
        User: Show me a salad.
        AI: Here is a healthy green salad with tomatoes and chicken.
        IMAGE_PROMPT: healthy chicken salad
    """.trimIndent()

    // Get messages from DB for the UI
    fun getMessages(userId: Int): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForUser(userId)
    }

    suspend fun sendMessage(userId: Int, userMessage: String) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Sending message to Gemini: $userMessage")
            // 1. Save User Message to DB immediately
            chatDao.insertMessage(
                ChatMessageEntity(userId = userId, text = userMessage, isFromUser = true)
            )

            // 2. Reconstruct History from DB for Gemini
            val dbHistory = chatDao.getMessagesForUser(userId).first()

            val chatHistory = dbHistory.map { msg ->
                content(role = if (msg.isFromUser) "user" else "model") { text(msg.text) }
            }.toMutableList()

            // Add System Prompt at the start
            chatHistory.add(0, content(role = "user") { text(systemPrompt) })
            chatHistory.add(1, content(role = "model") { text("Understood.") })

            // 3. Start Chat
            val chat = generativeModel.startChat(history = chatHistory)

            // 4. Send Message
            val response = chat.sendMessage(userMessage)
            val fullText = response.text ?: "I couldn't understand that."

            Log.d(TAG, "Received AI response: $fullText")

            // 5. Parse Image & Save AI Response
            var messageText = fullText
            var imageKeyword: String? = null

            if (fullText.contains("IMAGE_PROMPT:")) {
                val parts = fullText.split("IMAGE_PROMPT:")
                // Part 0 is the text, Part 1 is the keyword
                if (parts.isNotEmpty()) {
                    messageText = parts[0].trim()
                }
                if (parts.size > 1) {
                    imageKeyword = parts[1].trim()
                }
                Log.d(TAG, "Image keyword detected: $imageKeyword")
            }

            // --- CRITICAL FIX: Ensure text is never empty ---
            if (messageText.isBlank() && imageKeyword != null) {
                messageText = "Here is the image you asked for:"
            }
            // ------------------------------------------------

            // Save to DB
            chatDao.insertMessage(
                ChatMessageEntity(
                    userId = userId,
                    text = messageText,
                    isFromUser = false,
                    imageUrl = imageKeyword
                )
            )
            Log.d(TAG, "AI Message saved to Database")

        } catch (e: Exception) {
            Log.e(TAG, "Error in sendMessage: ${e.localizedMessage}")
            chatDao.insertMessage(
                ChatMessageEntity(userId = userId, text = "Error: ${e.localizedMessage}", isFromUser = false)
            )
        }
    }

    suspend fun ensureWelcomeMessage(userId: Int) {
        val currentMessages = chatDao.getMessagesForUser(userId).first()
        if (currentMessages.isEmpty()) {
            Log.d(TAG, "No history found. Inserting Welcome Message.")
            chatDao.insertMessage(
                ChatMessageEntity(
                    userId = userId,
                    text = "Hello! I am your SmartFit AI Coach. Ask me for a workout tip or a meal plan! 💪",
                    isFromUser = false
                )
            )
        }
    }
}