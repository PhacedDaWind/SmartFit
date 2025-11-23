package com.example.smartfit.data.repository

import com.example.smartfit.BuildConfig
import com.example.smartfit.data.local.ChatDao
import com.example.smartfit.data.local.ChatMessageEntity
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ChatRepository(private val chatDao: ChatDao) { // Inject DAO

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private val systemPrompt = """
        You are SmartFit AI, a fitness coach.
        STRICT RULES:
        1. Answer DIRECTLY. 
        2. DO NOT say "Hello" unless asked.
        3. REQUIRED: If the request implies an image, add this at the end:
        IMAGE_PROMPT: [keyword]
    """.trimIndent()

    // Get messages from DB for the UI
    fun getMessages(userId: Int): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForUser(userId)
    }

    suspend fun sendMessage(userId: Int, userMessage: String) = withContext(Dispatchers.IO) {
        try {
            // 1. Save User Message to DB immediately
            chatDao.insertMessage(
                ChatMessageEntity(userId = userId, text = userMessage, isFromUser = true)
            )

            // 2. Reconstruct History from DB for Gemini
            // We fetch the current list from DB to give Gemini memory of previous chats
            val dbHistory = chatDao.getMessagesForUser(userId).first()

            val chatHistory = dbHistory.map { msg ->
                content(role = if (msg.isFromUser) "user" else "model") { text(msg.text) }
            }.toMutableList()

            // Add System Prompt at the very beginning of history
            chatHistory.add(0, content(role = "user") { text(systemPrompt) })
            chatHistory.add(1, content(role = "model") { text("Understood.") })

            // 3. Start Chat with History
            val chat = generativeModel.startChat(history = chatHistory)

            // 4. Send Message
            val response = chat.sendMessage(userMessage)
            val fullText = response.text ?: "I couldn't understand that."

            // 5. Parse Image & Save AI Response to DB
            var finalParams = Pair(fullText, null as String?)

            if (fullText.contains("IMAGE_PROMPT:")) {
                val parts = fullText.split("IMAGE_PROMPT:")
                finalParams = Pair(parts[0].trim(), parts[1].trim())
            }

            // Save to DB
            chatDao.insertMessage(
                ChatMessageEntity(
                    userId = userId,
                    text = finalParams.first,
                    isFromUser = false,
                    imageUrl = finalParams.second // Save the keyword if found
                )
            )

        } catch (e: Exception) {
            // Save error message to DB so user sees it
            chatDao.insertMessage(
                ChatMessageEntity(userId = userId, text = "Error: ${e.localizedMessage}", isFromUser = false)
            )
        }
    }

    suspend fun ensureWelcomeMessage(userId: Int) {
        // 1. Get the current list once
        val currentMessages = chatDao.getMessagesForUser(userId).first()

        // 2. If empty, insert the welcome message
        if (currentMessages.isEmpty()) {
            chatDao.insertMessage(
                ChatMessageEntity(
                    userId = userId,
                    text = "Hello! I am your SmartFit AI Coach. Ask me for a workout tip or a meal plan! 💪",
                    isFromUser = false // It's from the AI
                )
            )
        }
    }
}