package ai.niva.airuntime.orchestrator

import ai.niva.airuntime.FakeLocalLLM
import ai.niva.domain.model.Message
import ai.niva.domain.model.MessageRole
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class AIOrchestratorTest {

    @Test
    fun `streams assistant tokens offline`() = runTest {
        val orchestrator = AIOrchestrator(FakeLocalLLM("hello"), EmptyRagContextProvider())
        val events = orchestrator.streamAssistantReply(
            UserChatRequest(
                conversationId = "c1",
                userMessage = "Hi",
                history = emptyList(),
                systemPrompt = "test",
                ragSystemPrompt = "rag",
            ),
        ).toList()
        val text = events.filter { !it.finished }.joinToString("") { it.deltaText }
        assertTrue(text.contains("h"))
    }

    @Test
    fun `buildPrompt includes history`() {
        val orchestrator = AIOrchestrator(FakeLocalLLM(), EmptyRagContextProvider())
        val prompt = orchestrator.buildPrompt(
            systemPrompt = "sys",
            ragSystemPrompt = "rag",
            ragExcerpts = listOf("doc p1: excerpt"),
            history = listOf(
                Message("1", "c", MessageRole.USER, "Earlier", 0),
            ),
            userMessage = "Now",
        )
        assertTrue(prompt.contains("Earlier"))
        assertTrue(prompt.contains("Now"))
    }
}
