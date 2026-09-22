package ai.privateai.airuntime.llm

import ai.privateai.domain.model.ModelDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class LlamaCppLLM : LocalLLM {

    private val mutex = Mutex()
    private var loadedDescriptor: ModelDescriptor? = null
    private var llmStatus: LlmStatus = LlmStatus.UNLOADED
    private val cancelled = AtomicBoolean(false)

    override suspend fun loadModel(model: ModelDescriptor) {
        mutex.withLock {
            val path = model.localPath
                ?: throw ModelLoadException("Model ${model.id} has no local path")
            llmStatus = LlmStatus.LOADING
            val ok = withContext(Dispatchers.IO) {
                LlamaNative.loadModel(path, model.contextLength)
            }
            if (!ok) {
                llmStatus = LlmStatus.ERROR
                throw ModelLoadException("The AI model could not be loaded on this device. Try the smaller model.")
            }
            loadedDescriptor = model
            llmStatus = LlmStatus.READY
        }
    }

    override suspend fun unloadModel() {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                LlamaNative.unloadModel()
            }
            loadedDescriptor = null
            llmStatus = LlmStatus.UNLOADED
        }
    }

    override fun isLoaded(): Boolean = llmStatus == LlmStatus.READY || llmStatus == LlmStatus.GENERATING

    override suspend fun generate(request: GenerationRequest): String {
        val builder = StringBuilder()
        streamGenerate(request).collect { chunk ->
            builder.append(chunk.text)
        }
        return builder.toString()
    }

    override fun streamGenerate(request: GenerationRequest): Flow<GenerationChunk> = callbackFlow {
        if (!isLoaded()) {
            close(ModelLoadException("Model is not loaded"))
            return@callbackFlow
        }
        cancelled.set(false)
        llmStatus = LlmStatus.GENERATING
        val emitted = withContext(Dispatchers.IO) {
            LlamaNative.streamCompletion(
                prompt = request.prompt,
                maxTokens = request.maxTokens,
                temperature = request.temperature,
                onToken = { token ->
                    if (cancelled.get()) {
                        return@streamCompletion false
                    }
                    trySend(GenerationChunk(text = token))
                    true
                },
            )
        }
        llmStatus = LlmStatus.READY
        trySend(GenerationChunk(text = "", done = true))
        if (!emitted && !cancelled.get()) {
            close(GenerationException("Generation failed"))
        }
        awaitClose { cancel() }
    }.flowOn(Dispatchers.IO)

    override fun cancel() {
        cancelled.set(true)
        LlamaNative.cancelGeneration()
    }

    override fun status(): LlmStatus = llmStatus

    override fun getModelInfo(): LoadedModelInfo? {
        val descriptor = loadedDescriptor ?: return null
        return LoadedModelInfo(descriptor.id, descriptor.contextLength)
    }
}

class ModelLoadException(message: String) : Exception(message)
class GenerationException(message: String) : Exception(message)
