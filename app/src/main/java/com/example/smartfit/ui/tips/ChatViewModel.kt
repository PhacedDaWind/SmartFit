package com.example.smartfit.ui.tips

import android.util.Log // <--- Import
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

    private val TAG = "ChatViewModel" // <--- Log Tag
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Automatically load messages for the current logged-in user
    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<ChatMessage>> = userPreferencesRepository.currentUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                Log.d(TAG, "Loading chat history for User ID: $userId") // <--- Log

                // Check and insert welcome message immediately when User ID is found
                viewModelScope.launch {
                    chatRepository.ensureWelcomeMessage(userId)
                }

                chatRepository.getMessages(userId).map { entities ->
                    entities.map { entity ->
                        val displayUrl = entity.imageUrl?.let { keyword ->
                            try {
                                val encoded = URLEncoder.encode(keyword, "UTF-8")
                                "https://image.pollinations.ai/prompt/$encoded"
                            } catch (e: Exception) {
                                Log.e(TAG, "Error encoding image URL for keyword: $keyword", e) // <--- Log Error
                                null
                            }
                        }
                        ChatMessage(entity.text, entity.isFromUser, displayUrl)
                    }
                }
            } else {
                Log.d(TAG, "No user logged in. Showing empty chat.") // <--- Log
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        Log.d(TAG, "User sending message: $text") // <--- Log
        _isLoading.value = true

        viewModelScope.launch {
            val userId = userPreferencesRepository.currentUserId.first()
            if (userId != null) {
                try {
                    chatRepository.sendMessage(userId, text)
                    Log.i(TAG, "Message successfully processed by AI") // <--- Log Success
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to send message: ${e.localizedMessage}") // <--- Log Failure
                }
            } else {
                Log.w(TAG, "Cannot send message: User ID is null") // <--- Log Warning
            }
            _isLoading.value = false
        }
    }
}