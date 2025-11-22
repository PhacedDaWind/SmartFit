package com.example.smartfit.ui.tips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val imageUrl: String? = null
)

class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Hello! Ask me for a workout tip.", false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMsg = ChatMessage(text, true)
        _messages.value = _messages.value + userMsg
        _isLoading.value = true

        viewModelScope.launch {
            val (responseText, imageKeyword) = chatRepository.sendMessage(text)

            val imageUrl = if (imageKeyword != null) {
                try {
                    // --- FIX: ENCODE THE KEYWORD ---
                    // This turns "chicken salad" into "chicken%20salad"
                    val encodedKeyword = URLEncoder.encode(imageKeyword, "UTF-8")
                    "https://image.pollinations.ai/prompt/$encodedKeyword"
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }

            val aiMsg = ChatMessage(responseText, false, imageUrl)
            _messages.value = _messages.value + aiMsg
            _isLoading.value = false
        }
    }
}