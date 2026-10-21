package ai.niva.modelmanager

import ai.niva.domain.model.ModelCapabilities
import ai.niva.domain.model.ModelDescriptor
import ai.niva.domain.model.ModelFormat
import ai.niva.domain.model.ModelInstallState
import ai.niva.domain.model.ModelType

object ModelCatalog {
    /** Default on-device LLM; filename is illustrative — actual path comes from registry/import. */
    fun defaultEmbeddingGemma300m(localPath: String? = null): ModelDescriptor = ModelDescriptor(
        id = "embedding-gemma-300m",
        name = "EmbeddingGemma 300M",
        type = ModelType.EMBEDDING,
        version = "1",
        format = ModelFormat.GGUF,
        quantization = "Q8",
        sizeBytes = 300_000_000L,
        minimumRamBytes = 2L * 1024 * 1024 * 1024,
        contextLength = 2048,
        capabilities = ModelCapabilities(supportsEmbeddings = true),
        checksum = null,
        localPath = localPath,
    )

    fun defaultQwen3_4b_Q4(localPath: String? = null): ModelDescriptor = ModelDescriptor(
        id = "qwen3-4b-q4",
        name = "Qwen3 4B Q4",
        type = ModelType.LLM,
        version = "3",
        format = ModelFormat.GGUF,
        quantization = "Q4_K_M",
        sizeBytes = 2_600_000_000L,
        minimumRamBytes = 5L * 1024 * 1024 * 1024,
        contextLength = 8192,
        capabilities = ModelCapabilities(
            supportsChat = true,
            supportsToolUse = true,
        ),
        checksum = null,
        localPath = localPath,
    )
}

data class RegistryEntry(
    val descriptor: ModelDescriptor,
    val state: ModelInstallState,
)
