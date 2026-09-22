package ai.privateai.modelmanager

import ai.privateai.domain.device.DeviceCapabilities
import ai.privateai.domain.device.GpuBackend
import ai.privateai.domain.device.ThermalState
import ai.privateai.domain.model.ModelInstallState
import ai.privateai.domain.selection.ModelSelectionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ModelManagerTest {

    @Test
    fun `import marks model ready`() {
        val root = File.createTempFile("models", "").apply { delete(); mkdir() }
        val source = File(root, "source.gguf").apply { writeText("fake-model") }
        val manager = ModelManager(root)
        val descriptor = ModelCatalog.defaultQwen3_4b_Q4()
        val installed = manager.importModelFile(source, descriptor)
        assertTrue(installed.localPath!!.endsWith("qwen3-4b-q4.gguf"))
        val entry = manager.getRegistry().first { it.descriptor.id == installed.id }
        assertEquals(ModelInstallState.READY, entry.state)
    }

    @Test
    fun `activate selects installed llm`() {
        val root = File.createTempFile("models2", "").apply { delete(); mkdir() }
        val manager = ModelManager(root)
        val source = File(root, "incoming.gguf").apply { writeText("x") }
        manager.importModelFile(source, ModelCatalog.defaultQwen3_4b_Q4())
        val device = DeviceCapabilities(
            totalRamBytes = 8L * 1024 * 1024 * 1024,
            availableRamBytes = 8L * 1024 * 1024 * 1024,
            architecture = "arm64-v8a",
            gpuBackend = GpuBackend.VULKAN,
            availableStorageBytes = 10L * 1024 * 1024 * 1024,
            supportsVulkan = true,
            supportsMetal = false,
            supportsNpu = false,
            osVersion = "14",
            thermalState = ThermalState.NOMINAL,
            batteryPercent = 100,
        )
        val result = manager.activateLlm(device)
        assertTrue(result is ModelSelectionResult.Selected)
    }
}
