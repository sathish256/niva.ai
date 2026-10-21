package ai.niva.app.chat

import ai.niva.domain.model.Message
import ai.niva.domain.rag.RagSource

data class ChatUiState(
    val conversationId: String,
    val title: String,
    val messages: List<Message> = emptyList(),
    val draft: String = "",
    val isGenerating: Boolean = false,
    val streamingAssistantText: String = "",
    val modelStatus: String = "Model not loaded",
    val privacyBanner: String = "AI: On Device · Network: Not Required",
    val errorMessage: String? = null,
    val lastSources: List<RagSource> = emptyList(),
)
