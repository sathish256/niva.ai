package ai.niva.domain.rag

enum class DocumentProcessingStatus {
    PENDING,
    PROCESSING,
    READY,
    FAILED,
}

data class DocumentRecord(
    val id: String,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val createdAt: Long,
    val modifiedAt: Long,
    val checksum: String,
    val processingStatus: DocumentProcessingStatus,
    val pageCount: Int?,
)

data class RagSource(
    val documentId: String,
    val displayName: String,
    val pageNumber: Int?,
    val chunkIndex: Int,
)

data class RagExcerpt(
    val chunkId: String,
    val text: String,
    val source: RagSource,
    val score: Float,
)

data class RagContext(
    val excerpts: List<RagExcerpt>,
) {
    companion object {
        fun empty() = RagContext(emptyList())
    }
}

data class ChunkConfig(
    val targetTokens: Int = 650,
    val overlapTokens: Int = 75,
)

data class HybridSearchWeights(
    val vectorWeight: Float = 0.7f,
    val keywordWeight: Float = 0.3f,
)
