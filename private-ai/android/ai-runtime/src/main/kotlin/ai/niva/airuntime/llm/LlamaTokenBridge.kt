package ai.niva.airuntime.llm

internal object LlamaTokenBridge {
    @Volatile
    private var handler: ((String) -> Boolean)? = null

    fun setHandler(handler: ((String) -> Boolean)?) {
        this.handler = handler
    }

    @JvmStatic
    fun onToken(token: String): Boolean = handler?.invoke(token) ?: false
}
