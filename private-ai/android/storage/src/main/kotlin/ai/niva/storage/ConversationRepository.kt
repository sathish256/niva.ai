package ai.niva.storage

import ai.niva.domain.model.Conversation
import ai.niva.domain.model.Message
import ai.niva.domain.model.MessageRole
import ai.niva.storage.local.ConversationDao
import ai.niva.storage.local.ConversationEntity
import ai.niva.storage.local.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ConversationRepository(
    private val dao: ConversationDao,
) {
    fun observeConversations(): Flow<List<Conversation>> =
        dao.observeConversations().map { list -> list.map { it.toDomain() } }

    fun observeMessages(conversationId: String): Flow<List<Message>> =
        dao.observeMessages(conversationId).map { list -> list.map { it.toDomain() } }

    suspend fun saveConversation(conversation: Conversation) {
        dao.upsertConversation(
            ConversationEntity(
                id = conversation.id,
                title = conversation.title,
                createdAt = conversation.createdAt,
                updatedAt = conversation.updatedAt,
            ),
        )
    }

    suspend fun saveMessage(message: Message) {
        dao.upsertMessage(
            MessageEntity(
                id = message.id,
                conversationId = message.conversationId,
                role = message.role.name,
                content = message.content,
                createdAt = message.createdAt,
            ),
        )
    }

    suspend fun deleteConversation(conversationId: String) {
        dao.deleteConversation(conversationId)
    }

    suspend fun deleteMessage(messageId: String) {
        dao.deleteMessage(messageId)
    }
}

private fun ConversationEntity.toDomain() = Conversation(
    id = id,
    title = title,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun MessageEntity.toDomain() = Message(
    id = id,
    conversationId = conversationId,
    role = MessageRole.valueOf(role),
    content = content,
    createdAt = createdAt,
)
