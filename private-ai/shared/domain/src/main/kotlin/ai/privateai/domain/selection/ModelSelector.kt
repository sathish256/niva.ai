package ai.privateai.domain.selection

import ai.privateai.domain.device.DeviceCapabilities
import ai.privateai.domain.model.ModelDescriptor
import ai.privateai.domain.model.ModelType

/**
 * Selects an LLM from installed candidates based on device RAM and model requirements.
 */
class ModelSelector {

    fun selectLlm(
        device: DeviceCapabilities,
        installed: List<ModelDescriptor>,
    ): ModelSelectionResult {
        val llms = installed.filter { it.type == ModelType.LLM && it.localPath != null }
        if (llms.isEmpty()) {
            return ModelSelectionResult.None("No local LLM is installed")
        }

        val ramGb = device.availableRamBytes / (1024L * 1024 * 1024)
        val preferredMaxParams = when {
            ramGb < 4 -> 2L
            ramGb < 12 -> 4L
            else -> 8L
        }

        val sorted = llms.sortedBy { it.minimumRamBytes }
        val safe = sorted.filter { model ->
            val headroom = device.availableRamBytes - model.minimumRamBytes
            headroom > MIN_HEADROOM_BYTES
        }

        if (safe.isEmpty()) {
            return ModelSelectionResult.None(
                "Insufficient memory to load any installed model safely",
            )
        }

        val chosen = safe.lastOrNull { descriptor ->
            descriptor.name.contains("qwen", ignoreCase = true) ||
                parseParamBillions(descriptor.name) <= preferredMaxParams
        } ?: safe.first()

        return ModelSelectionResult.Selected(chosen)
    }

    private fun parseParamBillions(name: String): Long {
        val match = Regex("(\\d+)b", RegexOption.IGNORE_CASE).find(name)
        return match?.groupValues?.get(1)?.toLongOrNull() ?: Long.MAX_VALUE
    }

    companion object {
        /** Reserve RAM for OS, UI, and KV cache beyond static model weights. */
        const val MIN_HEADROOM_BYTES: Long = 512L * 1024 * 1024
    }
}

sealed class ModelSelectionResult {
    data class Selected(val model: ModelDescriptor) : ModelSelectionResult()
    data class None(val reason: String) : ModelSelectionResult()
}
