package ai.niva.rag

import ai.niva.domain.network.NetworkAccess
import ai.niva.domain.network.NetworkPolicy
import ai.niva.domain.rag.EmbeddingModel
import ai.niva.domain.rag.HybridSearchWeights
import ai.niva.domain.rag.RagContext
import ai.niva.domain.rag.RagContextProvider
import ai.niva.domain.rag.RagExcerpt
import ai.niva.domain.rag.RagSource
import ai.niva.rag.search.HybridSearchRanker
import ai.niva.rag.search.VectorMath
import ai.niva.storage.local.DocumentDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RagRetriever @Inject constructor(
    private val documentDao: DocumentDao,
    private val embeddingModel: EmbeddingModel,
    private val ranker: HybridSearchRanker,
    private val networkPolicy: NetworkPolicy,
) : RagContextProvider {

    init {
        networkPolicy.assertNoNetwork("rag", networkPolicy.rag)
    }

    override suspend fun retrieve(query: String, topK: Int): RagContext {
        if (query.isBlank()) return RagContext.empty()
        val queryVector = embeddingModel.embed(query)
        val stored = documentDao.getEmbeddingsForModel(embeddingModel.modelId)
        val chunks = documentDao.getAllChunks().associateBy { it.id }
        val vectorScores = stored.associate { row ->
            val vector = VectorMath.fromBytes(row.vector, row.dimensions)
            row.chunkId to VectorMath.cosineSimilarity(queryVector, vector)
        }
        val keywordScores = keywordScoresForQuery(query)
        val ranked = ranker.rank(vectorScores, keywordScores, topK)
        val excerpts = ranked.mapNotNull { rankedChunk ->
            val chunk = chunks[rankedChunk.chunkId] ?: return@mapNotNull null
            val doc = documentDao.getDocument(chunk.documentId) ?: return@mapNotNull null
            RagExcerpt(
                chunkId = chunk.id,
                text = chunk.encryptedText,
                source = RagSource(
                    documentId = chunk.documentId,
                    displayName = doc.displayName,
                    pageNumber = chunk.pageNumber,
                    chunkIndex = chunk.chunkIndex,
                ),
                score = rankedChunk.score,
            )
        }
        return RagContext(excerpts)
    }

    private suspend fun keywordScoresForQuery(query: String): Map<String, Float> {
        val terms = query.lowercase().split(Regex("\\W+")).filter { it.length > 2 }
        if (terms.isEmpty()) return emptyMap()
        val scores = mutableMapOf<String, Float>()
        terms.forEach { term ->
            documentDao.keywordSearch(term).forEach { chunk ->
                scores[chunk.id] = (scores[chunk.id] ?: 0f) + 1f / terms.size
            }
        }
        return scores
    }
}
