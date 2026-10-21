package ai.niva.domain.rag

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextChunkerTest {

    private val chunker = TextChunker(ChunkConfig(targetTokens = 50, overlapTokens = 10))

    @Test
    fun `chunks preserve document id and indices`() {
        val text = "word ".repeat(400)
        val chunks = chunker.chunk("doc-1", text, pageNumber = 2)
        assertTrue(chunks.size > 1)
        assertEquals("doc-1", chunks.first().documentId)
        assertEquals(0, chunks.first().chunkIndex)
        assertEquals(2, chunks.first().pageNumber)
    }
}
