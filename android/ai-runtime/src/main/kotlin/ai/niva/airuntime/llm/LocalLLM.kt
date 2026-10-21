package ai.niva.airuntime.llm

import ai.niva.domain.model.ModelDescriptor
import kotlinx.coroutines.flow.Flow

data class GenerationRequest(
    val prompt: String,
    val maxTokens: Int = 512,
    val temperature: Float = 0.7f,
    val stopSequences: List<String> = emptyList(),
)

data class GenerationChunk(
    val text: String,
    val done: Boolean = false,
)

enum class LlmStatus {
    UNLOADED,
    LOADING,
    READY,
    GENERATING,
    ERROR,
}

data class LoadedModelInfo(
    val descriptorId: String,
    val contextLength: Int,
)

interface LocalLLM {
    suspend fun loadModel(model: ModelDescriptor)
    suspend fun unloadModel()
    fun isLoaded(): Boolean
    suspend fun generate(request: GenerationRequest): String
    fun streamGenerate(request: GenerationRequest): Flow<GenerationChunk>
    fun cancel()
    fun status(): LlmStatus
    fun getModelInfo(): LoadedModelInfo?
}
