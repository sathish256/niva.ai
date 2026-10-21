# Niva — Android MVP architecture (Sprint 1)

```
Compose UI (ChatScreen)
        ↓
ChatViewModel
        ↓
AIOrchestrator ── NetworkPolicy (DENY)
        ↓
LocalLLM ← LlamaCppLLM ← JNI ← llama.cpp (optional submodule)
        ↓
ModelManager / ModelDescriptor
        ↓
ConversationRepository → Room + SQLCipher (`:security` Keystore passphrase)
```

## Rules

- UI never calls llama.cpp directly.
- AI subsystems assert `NetworkPolicy` deny at construction.
- Model filenames are never hardcoded; use `ModelDescriptor.id` and registry paths.
- Native inference is isolated in `:ai-runtime`.

## Submodule

Initialize llama.cpp before release builds:

```bash
git submodule update --init native/llama.cpp
```

Without the submodule, JNI builds a stub library and `loadModel()` fails until a compatible `.so` and GGUF are present on device.
