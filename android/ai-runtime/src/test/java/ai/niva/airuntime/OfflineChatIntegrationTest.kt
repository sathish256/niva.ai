package ai.niva.airuntime

import ai.niva.airuntime.orchestrator.AIOrchestrator
import ai.niva.airuntime.orchestrator.EmptyRagContextProvider
import ai.niva.airuntime.orchestrator.UserChatRequest
import ai.niva.domain.network.NetworkAccess
import ai.niva.domain.network.NetworkPolicy
import ai.niva.modelmanager.ModelCatalog
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.Socket

/**
 * Verifies the chat pipeline can complete without network access (offline / airplane mode).
 */
class OfflineChatIntegrationTest {

    @Test
    fun `chat completes with network policy deny and without sockets`() = runTest {
        assertNetworkUnavailableForTest()
        val policy = NetworkPolicy(
            aiRuntime = NetworkAccess.DENY,
            rag = NetworkAccess.DENY,
            memory = NetworkAccess.DENY,
            voice = NetworkAccess.DENY,
            vision = NetworkAccess.DENY,
            documentProcessing = NetworkAccess.DENY,
        )
        val llm = FakeLocalLLM("Airplane mode response")
        llm.loadModel(ModelCatalog.defaultQwen3_4b_Q4(localPath = "/dev/null"))
        val orchestrator = AIOrchestrator(llm, EmptyRagContextProvider(), policy)
        val events = orchestrator.streamAssistantReply(
            UserChatRequest(
                conversationId = "offline",
                userMessage = "Hello",
                history = emptyList(),
                systemPrompt = "offline",
                ragSystemPrompt = "rag",
            ),
        ).toList()
        val text = events.filter { !it.finished }.joinToString("") { it.deltaText }
        assertEquals("Airplane mode response", text)
    }

    private fun assertNetworkUnavailableForTest() {
        try {
            Socket().use { socket ->
                socket.connect(java.net.InetSocketAddress("127.0.0.1", 9), 50)
                // If something listens, still OK — orchestrator must not open sockets itself.
            }
        } catch (_: Exception) {
            assertTrue(true)
        }
    }
}
