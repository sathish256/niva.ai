package ai.niva.domain.rag

interface EmbeddingModel {
    val modelId: String
    val dimensions: Int
    suspend fun embed(text: String): FloatArray
    suspend fun embedBatch(texts: List<String>): List<FloatArray>
}
