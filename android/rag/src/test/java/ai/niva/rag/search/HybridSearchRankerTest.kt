package ai.niva.rag.search

import org.junit.Assert.assertEquals
import org.junit.Test

class HybridSearchRankerTest {

    @Test
    fun `combines vector and keyword scores`() {
        val ranker = HybridSearchRanker()
        val result = ranker.rank(
            vectorScores = mapOf("a" to 1f, "b" to 0.2f),
            keywordScores = mapOf("b" to 1f),
            topK = 2,
        )
        assertEquals("a", result.first().chunkId)
        assertEquals(2, result.size)
    }
}
