# Niva

**Your AI. Your Phone. Your Data.**

Niva is an offline-first personal AI assistant for mobile. Models run on your phone; your chats, documents, and memories are designed to stay on the device—not on a vendor’s cloud.

## Principles

| | |
|---|---|
| **Your AI** | On-device inference (llama.cpp), no default cloud LLM fallback |
| **Your Phone** | Native Android (iOS planned); works in airplane mode after models are installed |
| **Your Data** | Local storage, encryption in progress; you control export and deletion |

## Repository layout

| Path | Purpose |
|------|---------|
| [`private-ai/android/`](private-ai/android/) | **Niva** Android app (`applicationId`: `ai.niva.app`) |
| [`private-ai/shared/`](private-ai/shared/) | Domain models, prompts, shared logic |
| [`private-ai/docs/`](private-ai/docs/) | Architecture and security notes |
| [`private-ai/models/`](private-ai/models/) | Model install guide (binaries not committed) |

The `private-ai/` directory is the engineering codename for the monorepo; the product name everywhere in the app and docs is **Niva**.

## Android (Sprint 1)

Vertical slice:

`Compose UI → ChatViewModel → AIOrchestrator → LocalLLM → llama.cpp JNI`

```bash
cd private-ai/android
echo "sdk.dir=$ANDROID_HOME" > local.properties
./gradlew :app:assembleDebug
./gradlew :shared-domain:test :ai-runtime:testDebugUnitTest :model-manager:testDebugUnitTest :app:testDebugUnitTest
```

On-device chat: init [`private-ai/native/llama.cpp`](private-ai/native/llama.cpp), set `enableNativeLlm=true` in `gradle.properties`, import a Qwen3 4B Q4 GGUF. Details: [`private-ai/android/README.md`](private-ai/android/README.md).

## Roadmap

- **Sprint 1:** Android chat MVP, streaming, model manager, Room history (current)
- **Sprint 2:** Encrypted SQLite + Keystore
- **Sprint 3+:** Local RAG, voice, vision, memory (per product spec)

## Privacy

No mandatory backend or internet for AI features. Network use for AI subsystems is denied by default (`NetworkPolicy`). No analytics or cloud inference in the initial release.

## License

License TBD until a `LICENSE` file is added.
