package ai.niva.airuntime.llm

internal object LlamaNative {
    init {
        try {
            System.loadLibrary("niva-llama-jni")
        } catch (_: UnsatisfiedLinkError) {
            // Native library unavailable (e.g. JVM unit tests). JNI calls will fail gracefully.
        }
    }

    external fun loadModel(path: String, contextLength: Int): Boolean
    external fun unloadModel()
    fun streamCompletion(
        prompt: String,
        maxTokens: Int,
        temperature: Float,
        onToken: (String) -> Boolean,
    ): Boolean {
        LlamaTokenBridge.setHandler(onToken)
        return try {
            streamCompletionNative(prompt, maxTokens, temperature)
        } finally {
            LlamaTokenBridge.setHandler(null)
        }
    }

    private external fun streamCompletionNative(
        prompt: String,
        maxTokens: Int,
        temperature: Float,
    ): Boolean

    external fun cancelGeneration()
}
