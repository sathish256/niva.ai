package ai.niva.rag

import ai.niva.domain.rag.DocumentProcessingStatus
import ai.niva.domain.rag.DocumentRecord
import ai.niva.domain.rag.TextChunker
import ai.niva.rag.embedding.HashEmbeddingModel
import ai.niva.rag.search.HybridSearchRanker
import ai.niva.storage.local.DocumentChunkEntity
import ai.niva.storage.local.DocumentDao
import ai.niva.storage.local.DocumentEntity
import ai.niva.storage.local.EmbeddingEntity
import ai.niva.rag.search.VectorMath
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class RagPipelineIntegrationTest {

    @Test
    fun `index and retrieve offline`() = runTest {
        val dao = mockk<DocumentDao>(relaxed = true)
        val embedding = HashEmbeddingModel()
        val indexer = RagIndexer(dao, embedding, TextChunker())
        val record = DocumentRecord(
            id = "d1",
            displayName = "policy.pdf",
            mimeType = "application/pdf",
            sizeBytes = 10,
            createdAt = 0,
            modifiedAt = 0,
            checksum = "abc",
            processingStatus = DocumentProcessingStatus.PENDING,
            pageCount = 1,
        )
        coEvery { dao.upsertDocument(any()) } returns Unit
        coEvery { dao.upsertChunk(any()) } returns Unit
        coEvery { dao.upsertEmbedding(any()) } returns Unit
        indexer.indexDocument(record, "/tmp/x.enc", "Your deductible is 50000 rupees on page 12", pageTexts = listOf(12 to "deductible 50000"))
        val chunk = DocumentChunkEntity("c1", "d1", 12, 0, "deductible 50000", null, null)
        val vector = embedding.embed("deductible 50000")
        coEvery { dao.getAllChunks() } returns listOf(chunk)
        coEvery { dao.getEmbeddingsForModel(embedding.modelId) } returns listOf(
            EmbeddingEntity("c1", embedding.modelId, embedding.dimensions, VectorMath.toBytes(vector)),
        )
        coEvery { dao.getDocument("d1") } returns DocumentEntity(
            "d1", "policy.pdf", "application/pdf", 1, 0, 0, "x", "/tmp", "READY", 1,
        )
        coEvery { dao.keywordSearch(any()) } returns listOf(chunk)
        val retriever = RagRetriever(dao, embedding, HybridSearchRanker(), ai.niva.domain.network.NetworkPolicy())
        val context = retriever.retrieve("What is my deductible?", topK = 2)
        assertTrue(context.excerpts.isNotEmpty())
        assertTrue(context.excerpts.first().source.pageNumber == 12)
    }
}
