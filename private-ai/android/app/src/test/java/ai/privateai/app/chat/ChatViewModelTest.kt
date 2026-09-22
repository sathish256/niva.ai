package ai.privateai.app.chat

import ai.privateai.airuntime.llm.GenerationChunk
import ai.privateai.airuntime.llm.LocalLLM
import ai.privateai.airuntime.llm.LlmStatus
import ai.privateai.airuntime.llm.LoadedModelInfo
import ai.privateai.airuntime.orchestrator.AIOrchestrator
import ai.privateai.app.device.AndroidDeviceCapabilitiesProvider
import ai.privateai.domain.model.ModelDescriptor
import ai.privateai.domain.network.NetworkPolicy
import ai.privateai.modelmanager.ModelCatalog
import ai.privateai.modelmanager.ModelManager
import ai.privateai.prompts.PromptRepository
import ai.privateai.storage.ConversationRepository
import ai.privateai.storage.local.ConversationDao
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
        every { device.current() } returns ai.privateai.domain.device.DeviceCapabilities(
            totalRamBytes = 8L * 1024 * 1024 * 1024,
            availableRamBytes = 8L * 1024 * 1024 * 1024,
            architecture = "arm64-v8a",
            gpuBackend = ai.privateai.domain.device.GpuBackend.VULKAN,
            availableStorageBytes = 8L * 1024 * 1024 * 1024,
            supportsVulkan = true,
            supportsMetal = false,
            supportsNpu = false,
            osVersion = "14",
            thermalState = ai.privateai.domain.device.ThermalState.NOMINAL,
            batteryPercent = 100,
        )
        val viewModel = ChatViewModel(
            orchestrator = AIOrchestrator(llm, NetworkPolicy()),
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
