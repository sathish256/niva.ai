# Model artifacts (not committed)

Niva never commits large model binaries to Git. Install models on-device via **Models → Import** (or adb push into the app-private `models/` directory).

## Initial LLM

| Field | Value |
|-------|--------|
| Family | Qwen3 |
| Parameters | 4B |
| Format | GGUF |
| Quantization | 4-bit (e.g. Q4_K_M) |
| Runtime | llama.cpp |

Use a `ModelDescriptor` entry (default id: `qwen3-4b-q4`) — do not hardcode filenames in application code.

## Embeddings (Sprint 3+)

EmbeddingGemma 300M (GGUF or runtime-specific format) with versioned `model_id` stored alongside vectors.

## Speech / vision (later sprints)

Whisper GGUF variants (`tiny`, `base`, `small`), offline Piper voices, and optional VLM packages — each registered in the local model registry.

## Licensing

Document upstream model licenses and redistribution terms before shipping builds that bundle or download models.
