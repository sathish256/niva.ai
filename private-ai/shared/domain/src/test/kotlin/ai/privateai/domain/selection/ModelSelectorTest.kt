package ai.privateai.domain.selection

import ai.privateai.domain.device.DeviceCapabilities
import ai.privateai.domain.device.GpuBackend
import ai.privateai.domain.device.ThermalState
import ai.privateai.domain.model.ModelCapabilities
import ai.privateai.domain.model.ModelDescriptor
import ai.privateai.domain.model.ModelFormat
import ai.privateai.domain.model.ModelType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelSelectorTest {

    private val selector = ModelSelector()

    private fun device(availableGb: Long) = DeviceCapabilities(
        totalRamBytes = availableGb * 1024 * 1024 * 1024,
        availableRamBytes = availableGb * 1024 * 1024 * 1024,
        architecture = "arm64-v8a",
        gpuBackend = GpuBackend.VULKAN,
        availableStorageBytes = 32L * 1024 * 1024 * 1024,
        supportsVulkan = true,
        supportsMetal = false,
        supportsNpu = false,
        osVersion = "14",
        thermalState = ThermalState.NOMINAL,
        batteryPercent = 80,
    )

    private fun qwen4b(minRamGb: Long = 5) = ModelDescriptor(
        id = "qwen3-4b-q4",
        name = "Qwen3 4B Q4",
        type = ModelType.LLM,
        version = "3",
        format = ModelFormat.GGUF,
        quantization = "Q4_K_M",
        sizeBytes = 2_500_000_000,
        minimumRamBytes = minRamGb * 1024 * 1024 * 1024,
        contextLength = 8192,
        capabilities = ModelCapabilities(supportsChat = true),
        checksum = null,
        localPath = "/data/models/qwen.gguf",
    )

    @Test
    fun `prefers qwen on 8GB device`() {
        val result = selector.selectLlm(device(8), listOf(qwen4b()))
        assertTrue(result is ModelSelectionResult.Selected)
        assertEquals("qwen3-4b-q4", (result as ModelSelectionResult.Selected).model.id)
    }

    @Test
    fun `rejects when no model fits memory`() {
        val result = selector.selectLlm(device(3), listOf(qwen4b(minRamGb = 6)))
        assertTrue(result is ModelSelectionResult.None)
    }
}
