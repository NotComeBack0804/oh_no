package com.easyaccounting.ui.ai

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.easyaccounting.ai.AiSettingsStore
import com.easyaccounting.ai.CompanionChatService
import com.easyaccounting.data.entity.AiChatMessage
import com.easyaccounting.data.entity.AiChatMessage.Companion.DEFAULT_CONVERSATION_ID
import com.easyaccounting.data.repository.AiChatRepository
import com.easyaccounting.data.repository.BillRepository
import com.easyaccounting.data.repository.CategoryRepository
import com.easyaccounting.data.repository.IncomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AiChatViewModel(
    appContext: Context,
    private val aiChatRepository: AiChatRepository,
    billRepository: BillRepository,
    incomeRepository: IncomeRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {
    private val settingsStore = AiSettingsStore(appContext)
    private val companionChatService = CompanionChatService(
        context = appContext,
        aiChatRepository = aiChatRepository,
        billRepository = billRepository,
        incomeRepository = incomeRepository,
        categoryRepository = categoryRepository
    )

    val messages: LiveData<List<AiChatMessage>> =
        aiChatRepository.getConversationMessages(DEFAULT_CONVERSATION_ID).asLiveData()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _modeHint = MutableStateFlow(settingsStore.buildModeHint())
    val modeHint: StateFlow<String> = _modeHint.asStateFlow()

    private val _toastEvent = MutableLiveData<String?>()
    val toastEvent: LiveData<String?> = _toastEvent

    fun sendMessage(text: String) {
        if (text.isBlank() || _isSending.value) return

        viewModelScope.launch {
            _isSending.value = true
            try {
                companionChatService.sendMessage(DEFAULT_CONVERSATION_ID, text)
                refreshModeHint()
            } catch (e: Exception) {
                _toastEvent.value = e.message ?: "发送失败，请稍后再试。"
            } finally {
                _isSending.value = false
            }
        }
    }

    fun clearConversation() {
        viewModelScope.launch {
            aiChatRepository.clearConversation(DEFAULT_CONVERSATION_ID)
        }
    }

    fun refreshModeHint() {
        _modeHint.value = settingsStore.buildModeHint()
    }

    fun consumeToastEvent() {
        _toastEvent.value = null
    }
}

class AiChatViewModelFactory(
    private val appContext: Context,
    private val aiChatRepository: AiChatRepository,
    private val billRepository: BillRepository,
    private val incomeRepository: IncomeRepository,
    private val categoryRepository: CategoryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AiChatViewModel::class.java)) {
            return AiChatViewModel(
                appContext = appContext,
                aiChatRepository = aiChatRepository,
                billRepository = billRepository,
                incomeRepository = incomeRepository,
                categoryRepository = categoryRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
