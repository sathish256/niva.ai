package ai.niva.domain.model

enum class MessageRole {
    SYSTEM,
    USER,
    ASSISTANT,
    TOOL,
}

data class Conversation(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
)

data class Message(
    val id: String,
    val conversationId: String,
    val role: MessageRole,
    val content: String,
    val createdAt: Long,
    val attachments: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
)
