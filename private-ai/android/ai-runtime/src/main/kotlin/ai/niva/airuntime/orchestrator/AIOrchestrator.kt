package ai.niva.airuntime.orchestrator

import ai.niva.airuntime.llm.GenerationRequest
import ai.niva.airuntime.llm.LocalLLM
import ai.niva.domain.model.Message
import ai.niva.domain.model.MessageRole
import ai.niva.domain.network.NetworkAccess
import ai.niva.domain.network.NetworkPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserChatRequest(
    val conversationId: String,
    val userMessage: String,
    val history: List<Message>,
    val systemPrompt: String,
)

data class AssistantStreamEvent(
    val deltaText: String,
    val finished: Boolean = false,
)

class AIOrchestrator(
    private val localLlm: LocalLLM,
    private val networkPolicy: NetworkPolicy = NetworkPolicy(),
) {

    init {
        networkPolicy.assertNoNetwork("ai-runtime", networkPolicy.aiRuntime)
    }

    fun streamAssistantReply(request: UserChatRequest): Flow<AssistantStreamEvent> {
        val prompt = buildPrompt(request.systemPrompt, request.history, request.userMessage)
        return localLlm.streamGenerate(
            GenerationRequest(
                prompt = prompt,
                maxTokens = 512,
                temperature = 0.7f,
            ),
        ).map { chunk ->
            AssistantStreamEvent(deltaText = chunk.text, finished = chunk.done)
        }
    }

    fun buildPrompt(
        systemPrompt: String,
        history: List<Message>,
        userMessage: String,
    ): String {
        val builder = StringBuilder()
        builder.append("<<SYS>>\n").append(systemPrompt).append("\n<</SYS>>\n")
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
