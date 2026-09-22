package ai.niva.domain.rag

import java.util.UUID

data class TextChunk(
    val id: String,
    val documentId: String,
    val text: String,
    val pageNumber: Int?,
    val section: String?,
    val chunkIndex: Int,
)

/**
 * Approximates tokens as ~4 characters for on-device chunking without a tokenizer dependency.
 */
class TextChunker(
    private val config: ChunkConfig = ChunkConfig(),
) {
    fun chunk(
        documentId: String,
        text: String,
        pageNumber: Int? = null,
        section: String? = null,
    ): List<TextChunk> {
        val normalized = text.replace(Regex("\\s+"), " ").trim()
        if (normalized.isEmpty()) return emptyList()
        val targetChars = config.targetTokens * CHARS_PER_TOKEN
        val overlapChars = config.overlapTokens * CHARS_PER_TOKEN
        val chunks = mutableListOf<TextChunk>()
        var start = 0
        var index = 0
        while (start < normalized.length) {
            val end = (start + targetChars).coerceAtMost(normalized.length)
            val piece = normalized.substring(start, end).trim()
            if (piece.isNotEmpty()) {
                chunks.add(
                    TextChunk(
                        id = UUID.randomUUID().toString(),
                        documentId = documentId,
                        text = piece,
                        pageNumber = pageNumber,
                        section = section,
                        chunkIndex = index++,
                    ),
                )
            }
            if (end >= normalized.length) break
            start = (end - overlapChars).coerceAtLeast(start + 1)
        }
        return chunks
    }

    companion object {
        const val CHARS_PER_TOKEN = 4
    }
}
