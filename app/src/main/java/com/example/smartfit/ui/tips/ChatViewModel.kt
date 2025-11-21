package com.example.smartfit.ui.tips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isFromUser: Boolean)

class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Hello! Ask me for a workout tip.", false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // 1. Add user message immediately
        val userMsg = ChatMessage(text, true)
        _messages.value = _messages.value + userMsg
        _isLoading.value = true

        viewModelScope.launch {
            // 2. Call Repository
            val responseText = chatRepository.sendMessage(text)

            // 3. Add AI response
            val aiMsg = ChatMessage(responseText, false)
            _messages.value = _messages.value + aiMsg
            _isLoading.value = false
        }
    }
}