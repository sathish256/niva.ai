package ai.niva.domain.rag

interface RagContextProvider {
    suspend fun retrieve(query: String, topK: Int = 4): RagContext
}
