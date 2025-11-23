package com.example.smartfit.ui.tips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.repository.ChatRepository
import com.example.smartfit.data.repository.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.URLEncoder

// We still use a UI model for display logic
data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val imageUrl: String? = null
)

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Automatically load messages for the current logged-in user
    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<ChatMessage>> = userPreferencesRepository.currentUserId
        .flatMapLatest { userId ->
            if (userId != null) {

                // --- ADD THIS BLOCK ---
                // Check and insert welcome message immediately when User ID is found
                viewModelScope.launch {
                    chatRepository.ensureWelcomeMessage(userId)
                }
                // ----------------------

                chatRepository.getMessages(userId).map { entities ->
                    entities.map { entity ->
                        val displayUrl = entity.imageUrl?.let { keyword ->
                            try {
                                val encoded = URLEncoder.encode(keyword, "UTF-8")
                                "https://image.pollinations.ai/prompt/$encoded"
                            } catch (e: Exception) { null }
                        }
                        ChatMessage(entity.text, entity.isFromUser, displayUrl)
                    }
                }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        _isLoading.value = true

        viewModelScope.launch {
            val userId = userPreferencesRepository.currentUserId.first()
            if (userId != null) {
                chatRepository.sendMessage(userId, text)
            }
            _isLoading.value = false
        }
    }
}