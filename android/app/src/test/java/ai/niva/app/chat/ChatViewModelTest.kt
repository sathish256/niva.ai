package ai.niva.app.chat

import ai.niva.airuntime.llm.GenerationChunk
import ai.niva.airuntime.llm.LocalLLM
import ai.niva.airuntime.llm.LlmStatus
import ai.niva.airuntime.llm.LoadedModelInfo
import ai.niva.airuntime.orchestrator.AIOrchestrator
import ai.niva.domain.rag.RagContext
import ai.niva.domain.rag.RagContextProvider
import ai.niva.app.device.AndroidDeviceCapabilitiesProvider
import ai.niva.domain.model.ModelDescriptor
import ai.niva.domain.network.NetworkPolicy
import ai.niva.modelmanager.ModelCatalog
import ai.niva.modelmanager.ModelManager
import ai.niva.prompts.PromptRepository
import ai.niva.storage.ConversationRepository
import ai.niva.storage.local.ConversationDao
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sendMessage streams assistant reply when model loaded`() = runTest(dispatcher) {
        val dao = mockk<ConversationDao>(relaxed = true)
        every { dao.observeMessages(any()) } returns flowOf(emptyList())
        coEvery { dao.upsertConversation(any()) } returns Unit
        coEvery { dao.upsertMessage(any()) } returns Unit
        val repository = ConversationRepository(dao)
        val llm = mockk<LocalLLM>(relaxed = true)
        every { llm.isLoaded() } returns true
        coEvery { llm.loadModel(any()) } returns Unit
        every { llm.status() } returns LlmStatus.READY
        every { llm.getModelInfo() } returns LoadedModelInfo("qwen3-4b-q4", 8192)
        every { llm.streamGenerate(any()) } returns flow {
            emit(GenerationChunk("Hi"))
            emit(GenerationChunk("", done = true))
        }
        val root = File.createTempFile("vm-models", "").apply { delete(); mkdir() }
        val modelFile = File(root, "incoming.gguf").apply { writeText("model") }
        val modelManager = ModelManager(root)
        modelManager.importModelFile(modelFile, ModelCatalog.defaultQwen3_4b_Q4())
        val device = mockk<AndroidDeviceCapabilitiesProvider>()
        every { device.current() } returns ai.niva.domain.device.DeviceCapabilities(
            totalRamBytes = 8L * 1024 * 1024 * 1024,
            availableRamBytes = 8L * 1024 * 1024 * 1024,
            architecture = "arm64-v8a",
            gpuBackend = ai.niva.domain.device.GpuBackend.VULKAN,
            availableStorageBytes = 8L * 1024 * 1024 * 1024,
            supportsVulkan = true,
            supportsMetal = false,
            supportsNpu = false,
            osVersion = "14",
            thermalState = ai.niva.domain.device.ThermalState.NOMINAL,
            batteryPercent = 100,
        )
        val rag = mockk<RagContextProvider>()
        coEvery { rag.retrieve(any(), any()) } returns RagContext.empty()
        val viewModel = ChatViewModel(
            orchestrator = AIOrchestrator(llm, rag, NetworkPolicy()),
            localLlm = llm,
            modelManager = modelManager,
            deviceCapabilities = device,
            conversationRepository = repository,
            promptRepository = PromptRepository(),
        )
        advanceUntilIdle()
        viewModel.updateDraft("Hello")
        viewModel.sendMessage()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isGenerating)
        assertEquals("Loaded Qwen3 4B Q4", viewModel.uiState.value.modelStatus)
    }
}
