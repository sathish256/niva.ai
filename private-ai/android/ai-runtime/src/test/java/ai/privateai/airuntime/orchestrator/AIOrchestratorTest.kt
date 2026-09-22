package ai.privateai.airuntime.orchestrator

import ai.privateai.airuntime.FakeLocalLLM
import ai.privateai.domain.model.Message
import ai.privateai.domain.model.MessageRole
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class AIOrchestratorTest {

    @Test
    fun `streams assistant tokens offline`() = runTest {
        val orchestrator = AIOrchestrator(FakeLocalLLM("hello"))
        val events = orchestrator.streamAssistantReply(
            UserChatRequest(
                conversationId = "c1",
                userMessage = "Hi",
                history = emptyList(),
                systemPrompt = "test",
            ),
        ).toList()
        val text = events.filter { !it.finished }.joinToString("") { it.deltaText }
        assertTrue(text.contains("h"))
    }

    @Test
    fun `buildPrompt includes history`() {
        val orchestrator = AIOrchestrator(FakeLocalLLM())
        val prompt = orchestrator.buildPrompt(
            systemPrompt = "sys",
            history = listOf(
                Message("1", "c", MessageRole.USER, "Earlier", 0),
            ),
            userMessage = "Now",
        )
        assertTrue(prompt.contains("Earlier"))
        assertTrue(prompt.contains("Now"))
    }
}
