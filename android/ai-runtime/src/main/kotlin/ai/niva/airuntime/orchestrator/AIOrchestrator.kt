package ai.niva.airuntime.orchestrator

import ai.niva.airuntime.llm.GenerationRequest
import ai.niva.airuntime.llm.LocalLLM
import ai.niva.domain.model.Message
import ai.niva.domain.model.MessageRole
import ai.niva.domain.network.NetworkAccess
import ai.niva.domain.network.NetworkPolicy
import ai.niva.domain.rag.RagContextProvider
import ai.niva.domain.rag.RagSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

data class UserChatRequest(
    val conversationId: String,
    val userMessage: String,
    val history: List<Message>,
    val systemPrompt: String,
    val ragSystemPrompt: String,
    val useRag: Boolean = true,
)

data class AssistantStreamEvent(
    val deltaText: String,
    val finished: Boolean = false,
    val sources: List<RagSource> = emptyList(),
)

class AIOrchestrator(
    private val localLlm: LocalLLM,
    private val ragContextProvider: RagContextProvider,
    private val networkPolicy: NetworkPolicy = NetworkPolicy(),
) {

    init {
        networkPolicy.assertNoNetwork("ai-runtime", networkPolicy.aiRuntime)
    }

    fun streamAssistantReply(request: UserChatRequest): Flow<AssistantStreamEvent> = flow {
        val rag = if (request.useRag) {
            ragContextProvider.retrieve(request.userMessage)
        } else {
            ai.niva.domain.rag.RagContext.empty()
        }
        val sources = rag.excerpts.map { it.source }
        val prompt = buildPrompt(
            systemPrompt = request.systemPrompt,
            ragSystemPrompt = request.ragSystemPrompt,
            ragExcerpts = rag.excerpts.map { "${it.source.displayName} p${it.source.pageNumber ?: "?"}: ${it.text}" },
            history = request.history,
            userMessage = request.userMessage,
        )
        localLlm.streamGenerate(
            GenerationRequest(
                prompt = prompt,
                maxTokens = 512,
                temperature = 0.7f,
            ),
        ).collect { chunk ->
            emit(
                AssistantStreamEvent(
                    deltaText = chunk.text,
                    finished = chunk.done,
                    sources = if (chunk.done) sources else emptyList(),
                ),
            )
        }
    }

    fun buildPrompt(
        systemPrompt: String,
        ragSystemPrompt: String,
        ragExcerpts: List<String>,
        history: List<Message>,
        userMessage: String,
    ): String {
        val builder = StringBuilder()
        builder.append("<<SYS>>\n").append(systemPrompt).append('\n')
        if (ragExcerpts.isNotEmpty()) {
            builder.append(ragSystemPrompt).append('\n')
            builder.append("[context]\n")
            ragExcerpts.forEach { builder.append(it).append('\n') }
        }
        builder.append("<</SYS>>\n")
        history.forEach { message ->
            val tag = when (message.role) {
                MessageRole.SYSTEM -> "system"
                MessageRole.USER -> "user"
                MessageRole.ASSISTANT -> "assistant"
                MessageRole.TOOL -> "tool"
            }
            builder.append("[$tag]\n").append(message.content).append('\n')
        }
        builder.append("[user]\n").append(userMessage).append('\n')
        builder.append("[assistant]\n")
        return builder.toString()
    }
}
