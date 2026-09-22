package ai.privateai.prompts

object DefaultPrompts {
    const val VERSION = "1.0.0"

    val defaultAssistant = PromptDefinition(
        id = "assistant.default",
        version = VERSION,
        body = """
            You are PrivateAI, a helpful assistant that runs entirely on the user's phone.
            Be concise, accurate, and never claim to access the internet.
        """.trimIndent(),
    )

    val rag = PromptDefinition(
        id = "rag.default",
        version = VERSION,
        body = "Answer using only the provided document excerpts. Cite page numbers when available.",
    )
}

data class PromptDefinition(
    val id: String,
    val version: String,
    val body: String,
)

class PromptRepository {
    fun systemPrompt(id: String = DefaultPrompts.defaultAssistant.id): String = when (id) {
        DefaultPrompts.defaultAssistant.id -> DefaultPrompts.defaultAssistant.body
        DefaultPrompts.rag.id -> DefaultPrompts.rag.body
        else -> DefaultPrompts.defaultAssistant.body
    }
}
