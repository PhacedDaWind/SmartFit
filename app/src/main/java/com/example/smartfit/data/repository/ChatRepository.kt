package com.example.smartfit.data.repository

import com.example.smartfit.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRepository {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    // --- 1. DEFINE THE PERSONA & RULES ONCE ---
    private val systemPrompt = """
        You are SmartFit AI, a fitness coach.
        
        STRICT RULES:
        1. Answer the user's question DIRECTLY. 
        2. DO NOT say "Hello" or "I am SmartFit AI" unless the user specifically asks "Who are you?".
        3. If the user asks to SEE something (e.g., "show me", "what does X look like"), you MUST describe it briefly.
        4. REQUIRED: If the user's request implies an image (like "show me a salad"), add a new line at the very end with this format:
        IMAGE_PROMPT: [simple keyword for search]
        
        Example 1:
        User: Show me a salad.
        AI: Here is a healthy green salad.
        IMAGE_PROMPT: healthy chicken salad
    """.trimIndent()

    // --- 2. INITIALIZE THE CHAT WITH HISTORY ---
    // We "pre-fill" the chat history with the rules so the AI knows them immediately.
    private val chat = generativeModel.startChat(
        history = listOf(
            content(role = "user") { text(systemPrompt) },
            content(role = "model") { text("Understood. I am ready to act as SmartFit AI.") }
        )
    )

    suspend fun sendMessage(userMessage: String): Pair<String, String?> = withContext(Dispatchers.IO) {
        try {
            // --- 3. SEND MESSAGE TO THE EXISTING CHAT SESSION ---
            // The 'chat' object automatically remembers previous messages!
            val response = chat.sendMessage(userMessage)

            val fullText = response.text ?: "I couldn't understand that."

            // --- PARSING LOGIC (Same as before) ---
            if (fullText.contains("IMAGE_PROMPT:")) {
                val parts = fullText.split("IMAGE_PROMPT:")
                val messageText = parts[0].trim()
                val imageKeyword = parts[1].trim()
                return@withContext Pair(messageText, imageKeyword)
            } else {
                return@withContext Pair(fullText, null)
            }

        } catch (e: Exception) {
            return@withContext Pair("Error: ${e.localizedMessage}", null)
        }
    }
}