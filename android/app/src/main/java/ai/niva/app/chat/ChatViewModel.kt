package ai.niva.app.chat

import ai.niva.airuntime.llm.LocalLLM
import ai.niva.airuntime.llm.ModelLoadException
import ai.niva.airuntime.orchestrator.AIOrchestrator
import ai.niva.airuntime.orchestrator.UserChatRequest
import ai.niva.app.device.AndroidDeviceCapabilitiesProvider
import ai.niva.domain.model.Conversation
import ai.niva.domain.model.Message
import ai.niva.domain.model.MessageRole
import ai.niva.domain.selection.ModelSelectionResult
import ai.niva.modelmanager.ModelManager
import ai.niva.prompts.PromptRepository
import ai.niva.storage.ConversationRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val orchestrator: AIOrchestrator,
    private val localLlm: LocalLLM,
    private val modelManager: ModelManager,
    private val deviceCapabilities: AndroidDeviceCapabilitiesProvider,
    private val conversationRepository: ConversationRepository,
    private val promptRepository: PromptRepository,
) : ViewModel() {

    private val conversationId = UUID.randomUUID().toString()
    private var generationJob: Job? = null

    private val _uiState = MutableStateFlow(
        ChatUiState(
            conversationId = conversationId,
            title = "New chat",
        ),
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            conversationRepository.saveConversation(
                Conversation(
                    id = conversationId,
                    title = "New chat",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                ),
            )
            conversationRepository.observeMessages(conversationId).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
        ensureModelLoaded()
    }

    fun updateDraft(value: String) {
        _uiState.update { it.copy(draft = value) }
    }

    fun sendMessage() {
        val text = _uiState.value.draft.trim()
        if (text.isEmpty() || _uiState.value.isGenerating) return
        if (!localLlm.isLoaded()) {
            _uiState.update { it.copy(errorMessage = "Load a local model before chatting.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(draft = "", errorMessage = null) }
            val userMessage = Message(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                role = MessageRole.USER,
                content = text,
                createdAt = System.currentTimeMillis(),
            )
            conversationRepository.saveMessage(userMessage)
            val history = _uiState.value.messages + userMessage
            startAssistantGeneration(history)
        }
    }

    fun stopGeneration() {
        localLlm.cancel()
        generationJob?.cancel()
        _uiState.update { it.copy(isGenerating = false) }
    }

    fun regenerate() {
        val lastUser = _uiState.value.messages.lastOrNull { it.role == MessageRole.USER } ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(draft = lastUser.content) }
            sendMessage()
        }
    }

    private fun startAssistantGeneration(history: List<Message>) {
        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, streamingAssistantText = "") }
            val assistantId = UUID.randomUUID().toString()
            var assembled = ""
            try {
                orchestrator.streamAssistantReply(
                    UserChatRequest(
                        conversationId = conversationId,
                        userMessage = history.lastOrNull { it.role == MessageRole.USER }?.content ?: "",
                        history = history,
                        systemPrompt = promptRepository.systemPrompt(),
                    ),
                ).collect { event ->
                    if (!event.finished) {
                        assembled += event.deltaText
                        _uiState.update { it.copy(streamingAssistantText = assembled) }
                    }
                }
                if (assembled.isNotBlank()) {
                    conversationRepository.saveMessage(
                        Message(
                            id = assistantId,
                            conversationId = conversationId,
                            role = MessageRole.ASSISTANT,
                            content = assembled,
                            createdAt = System.currentTimeMillis(),
                        ),
                    )
                }
            } catch (ex: Exception) {
                _uiState.update {
                    it.copy(errorMessage = ex.message ?: "Generation failed")
                }
            } finally {
                _uiState.update { it.copy(isGenerating = false, streamingAssistantText = "") }
            }
        }
    }

    private fun ensureModelLoaded() {
        viewModelScope.launch {
            when (val selection = modelManager.activateLlm(deviceCapabilities.current())) {
                is ModelSelectionResult.Selected -> {
                    try {
                        localLlm.loadModel(selection.model)
                        modelManager.markInUse(selection.model.id)
                        _uiState.update {
                            it.copy(modelStatus = "Loaded ${selection.model.name}")
                        }
                    } catch (ex: ModelLoadException) {
                        _uiState.update {
                            it.copy(modelStatus = "No model loaded", errorMessage = ex.message)
                        }
                    }
                }
                is ModelSelectionResult.None -> {
                    _uiState.update {
                        it.copy(
                            modelStatus = "Import a GGUF model (e.g. Qwen3 4B Q4)",
                            errorMessage = selection.reason,
                        )
                    }
                }
            }
        }
    }
}
