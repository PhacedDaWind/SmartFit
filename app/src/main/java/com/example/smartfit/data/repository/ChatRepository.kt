package com.example.smartfit.data.repository

import com.example.smartfit.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRepository {
    // Initialize Gemini with your API Key
    // Note: If BuildConfig is red, Build > Clean Project, then Rebuild Project.
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun sendMessage(userMessage: String): String = withContext(Dispatchers.IO) {
        try {
            // The System Prompt to make it a "Workout Coach"
            val prompt = """
                You are SmartFit AI, an enthusiastic and professional fitness coach.
                
                Rules:
                1. Only answer questions related to fitness, nutrition, workouts, and health.
                2. If the user asks about other topics, politely refuse.
                3. Keep your answers concise and motivating.
                
                User Question: $userMessage
            """.trimIndent()

            val response = generativeModel.generateContent(
                content { text(prompt) }
            )
            response.text ?: "I couldn't understand that."
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }
}