package ai.niva.airuntime.orchestrator

import ai.niva.domain.rag.RagContext
import ai.niva.domain.rag.RagContextProvider

class EmptyRagContextProvider : RagContextProvider {
    override suspend fun retrieve(query: String, topK: Int): RagContext = RagContext.empty()
}
