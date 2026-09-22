# Niva

**Your AI. Your Phone. Your Data.**

Niva is a personal AI that lives on your phone—not in someone else's cloud. Your conversations, habits, and context stay on the device you already trust. No account required to get started; no silent export of your life to a data broker.

## Why Niva

| Principle | What it means |
|-----------|----------------|
| **Your AI** | Assistance tuned to you—insights, focus, and answers without treating you as training fuel. |
| **Your Phone** | Native, fast, and available offline when the model allows. Built for the platform, not a generic web wrapper. |
| **Your Data** | Local-first storage. You choose what to keep, share, or delete. |

## Privacy promise

- Data stored on your device by default  
- No selling or renting your information  
- No using your content to train shared foundation models without clear, explicit consent  
- Delete your data from the app when you want—without a support ticket  

## PrivateAI (offline-first mobile assistant)

Product code lives under [`private-ai/`](private-ai/). Sprint 1 delivers the Android vertical slice:

`Compose UI → ChatViewModel → AIOrchestrator → LocalLLM (llama.cpp JNI) → streaming chat`

See [`private-ai/docs/architecture/android-mvp.md`](private-ai/docs/architecture/android-mvp.md) and build from [`private-ai/android/`](private-ai/android/).

## Status

- **Sprint 1 (in progress):** Android chat MVP with local LLM abstraction, model manager, Room conversation storage, unit/offline integration tests.
- **Next:** encrypted SQLite (Sprint 2), then local RAG.

## License

License TBD. Until a `LICENSE` file is added, all rights reserved by the project owners.
