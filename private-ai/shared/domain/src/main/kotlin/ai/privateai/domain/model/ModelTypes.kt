package ai.privateai.domain.model

enum class ModelType {
    LLM,
    EMBEDDING,
    STT,
    TTS,
    OCR,
    VLM,
}

enum class ModelFormat {
    GGUF,
    ONNX,
    OTHER,
}

enum class ModelInstallState {
    NOT_INSTALLED,
    DOWNLOADING,
    VERIFYING,
    INSTALLING,
    READY,
    IN_USE,
    FAILED,
    REMOVED,
}

data class ModelCapabilities(
    val supportsChat: Boolean = false,
    val supportsEmbeddings: Boolean = false,
    val supportsVision: Boolean = false,
    val supportsToolUse: Boolean = false,
)

data class ModelDescriptor(
    val id: String,
    val name: String,
    val type: ModelType,
    val version: String,
    val format: ModelFormat,
    val quantization: String?,
    val sizeBytes: Long,
    val minimumRamBytes: Long,
    val contextLength: Int,
    val capabilities: ModelCapabilities,
    val checksum: String?,
    val localPath: String?,
)
