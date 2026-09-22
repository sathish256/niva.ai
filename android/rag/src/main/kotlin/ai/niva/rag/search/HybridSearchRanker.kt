package ai.niva.rag.search

import ai.niva.domain.rag.HybridSearchWeights

data class RankedChunk(
    val chunkId: String,
    val score: Float,
    val vectorScore: Float,
    val keywordScore: Float,
)

class HybridSearchRanker(
    private val weights: HybridSearchWeights = HybridSearchWeights(),
) {
    fun rank(
        vectorScores: Map<String, Float>,
        keywordScores: Map<String, Float>,
        topK: Int,
    ): List<RankedChunk> {
        val ids = vectorScores.keys + keywordScores.keys
        return ids.map { id ->
            val v = vectorScores[id] ?: 0f
            val k = keywordScores[id] ?: 0f
            val combined = v * weights.vectorWeight + k * weights.keywordWeight
            RankedChunk(id, combined, v, k)
        }
            .sortedByDescending { it.score }
            .take(topK)
    }
}
