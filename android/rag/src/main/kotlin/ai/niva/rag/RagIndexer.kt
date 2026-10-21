package ai.niva.rag

import ai.niva.domain.rag.DocumentProcessingStatus
import ai.niva.domain.rag.DocumentRecord
import ai.niva.domain.rag.EmbeddingModel
import ai.niva.domain.rag.TextChunker
import ai.niva.rag.search.VectorMath
import ai.niva.storage.local.DocumentChunkEntity
import ai.niva.storage.local.DocumentDao
import ai.niva.storage.local.DocumentEntity
import ai.niva.storage.local.EmbeddingEntity
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RagIndexer @Inject constructor(
    private val documentDao: DocumentDao,
    private val embeddingModel: EmbeddingModel,
    private val chunker: TextChunker,
) {
    suspend fun indexDocument(
        record: DocumentRecord,
        encryptedPath: String,
        extractedText: String,
        pageTexts: List<Pair<Int, String>> = emptyList(),
    ) {
        val statusEntity = record.toEntity(encryptedPath, DocumentProcessingStatus.PROCESSING)
        documentDao.upsertDocument(statusEntity)
        val chunks = if (pageTexts.isNotEmpty()) {
            pageTexts.flatMap { (page, text) ->
                chunker.chunk(record.id, text, pageNumber = page)
            }
        } else {
            chunker.chunk(record.id, extractedText)
        }
        val embeddings = embeddingModel.embedBatch(chunks.map { it.text })
        chunks.zip(embeddings).forEach { (chunk, vector) ->
            documentDao.upsertChunk(
                DocumentChunkEntity(
                    id = chunk.id,
                    documentId = chunk.documentId,
                    pageNumber = chunk.pageNumber,
                    chunkIndex = chunk.chunkIndex,
                    encryptedText = chunk.text,
                    section = chunk.section,
                    metadataJson = null,
                ),
            )
            documentDao.upsertEmbedding(
                EmbeddingEntity(
                    chunkId = chunk.id,
                    modelId = embeddingModel.modelId,
                    dimensions = embeddingModel.dimensions,
                    vector = VectorMath.toBytes(vector),
                ),
            )
        }
        documentDao.upsertDocument(
            statusEntity.copy(
                processingStatus = DocumentProcessingStatus.READY.name,
                pageCount = pageTexts.size.takeIf { it > 0 },
            ),
        )
    }

    private fun DocumentRecord.toEntity(path: String, status: DocumentProcessingStatus) =
        DocumentEntity(
            id = id,
            displayName = displayName,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            createdAt = createdAt,
            modifiedAt = modifiedAt,
            checksum = checksum,
            encryptedPath = path,
            processingStatus = status.name,
            pageCount = pageCount,
        )
}

fun sha256(bytes: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(bytes).joinToString("") { "%02x".format(it) }
}
