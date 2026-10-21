package ai.niva.rag.embedding

import ai.niva.domain.rag.EmbeddingModel
import java.security.MessageDigest
import javax.inject.Inject
import kotlin.math.sqrt

/**
 * Deterministic on-device embedder for development/tests until EmbeddingGemma GGUF is wired.
 * Replace with a GGUF-backed implementation when the embedding runtime is available.
 */
class HashEmbeddingModel @Inject constructor() : EmbeddingModel {
    override val modelId: String = MODEL_ID
    override val dimensions: Int = 384

    override suspend fun embed(text: String): FloatArray = embedBatch(listOf(text)).first()

    override suspend fun embedBatch(texts: List<String>): List<FloatArray> =
        texts.map { text -> normalize(hashToVector(text)) }

    private fun hashToVector(text: String): FloatArray {
        val vector = FloatArray(dimensions)
        val base = text.toByteArray(Charsets.UTF_8)
        for (i in 0 until dimensions) {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(base)
            digest.update(i.toByte())
            val hash = digest.digest()
            vector[i] = (hash[0].toInt() and 0xFF) / 255f
        }
        return vector
    }

    private fun normalize(vector: FloatArray): FloatArray {
        var sum = 0f
        vector.forEach { sum += it * it }
        val norm = sqrt(sum).takeIf { it > 0f } ?: 1f
        return FloatArray(vector.size) { i -> vector[i] / norm }
    }

    companion object {
        const val MODEL_ID = "embedding-gemma-300m-hash-dev"
    }
}
