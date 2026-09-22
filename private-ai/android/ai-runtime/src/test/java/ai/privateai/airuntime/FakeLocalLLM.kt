package ai.privateai.airuntime

import ai.privateai.airuntime.llm.GenerationChunk
import ai.privateai.airuntime.llm.GenerationRequest
import ai.privateai.airuntime.llm.LocalLLM
import ai.privateai.airuntime.llm.LlmStatus
import ai.privateai.airuntime.llm.LoadedModelInfo
import ai.privateai.domain.model.ModelDescriptor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeLocalLLM(
    private val response: String = "offline-ok",
) : LocalLLM {
    private var loaded = false
    private var descriptorId: String? = null

    override suspend fun loadModel(model: ModelDescriptor) {
        loaded = true
        descriptorId = model.id
    }

    override suspend fun unloadModel() {
        loaded = false
        descriptorId = null
    }

    override fun isLoaded(): Boolean = loaded

    override suspend fun generate(request: GenerationRequest): String = response

    override fun streamGenerate(request: GenerationRequest): Flow<GenerationChunk> = flow {
        response.forEach { ch ->
            emit(GenerationChunk(text = ch.toString()))
        }
        emit(GenerationChunk(text = "", done = true))
    }

    override fun cancel() = Unit

    override fun status(): LlmStatus = if (loaded) LlmStatus.READY else LlmStatus.UNLOADED

    override fun getModelInfo(): LoadedModelInfo? =
        descriptorId?.let { LoadedModelInfo(it, 8192) }
}
