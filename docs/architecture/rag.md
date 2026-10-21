# Niva — local RAG (Sprint 3)

```
Import (PDF/txt/md) → extract → chunk → embed → SQLite
User question → hybrid search → prompt + LocalLLM → answer + sources
```

- **Module:** `:rag`
- **Storage:** `documents`, `document_chunks`, `embeddings` (SQLCipher-backed DB v3)
- **Embeddings:** `EmbeddingModel` interface; `HashEmbeddingModel` until EmbeddingGemma GGUF runtime lands
- **Search:** cosine similarity + keyword SQL `LIKE`, weighted via `HybridSearchWeights`
- **Network:** `NetworkPolicy.rag = DENY`

Chat shows source labels (`displayName — page N`) only when retrieval returns chunks — no fabricated citations.
