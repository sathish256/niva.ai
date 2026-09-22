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
| [`android/`](android/) | **Niva** Android app (`applicationId`: `ai.niva.app`) |
| [`shared/`](shared/) | Domain models, prompts, shared logic |
| [`docs/`](docs/) | Architecture and security notes |
| [`models/`](models/) | Model install guide (binaries not committed) |
| [`native/`](native/) | Native runtimes (llama.cpp submodule) |
| [`ios/`](ios/) | iOS placeholder |

## Android

```bash
cd android
echo "sdk.dir=$ANDROID_HOME" > local.properties
./gradlew :app:assembleDebug
./gradlew testDebugUnitTest
```

| Sprint | Status | Highlights |
|--------|--------|------------|
| **1** | Done | Compose chat, `LocalLLM`, llama.cpp JNI, model manager, Room history |
| **2** | Done | SQLCipher + Keystore passphrase, plaintext migration, Privacy screen, delete-all |
| **3+** | Planned | Local RAG, voice, vision, memory |

On-device chat: [`native/llama.cpp`](native/llama.cpp), `enableNativeLlm=true`, Qwen3 4B Q4 GGUF — [`android/README.md`](android/README.md). Encryption: [`docs/security/encryption.md`](docs/security/encryption.md).

## Privacy

No mandatory backend or internet for AI features. Network use for AI subsystems is denied by default (`NetworkPolicy`). No analytics or cloud inference in the initial release.

## License

License TBD until a `LICENSE` file is added.
