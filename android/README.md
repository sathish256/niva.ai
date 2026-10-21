# Niva — Android

Offline-first personal AI for Android (Sprints 1–2).

- **App name:** Niva  
- **Application ID:** `ai.niva.app`  
- **Package root:** `ai.niva.*`

## Build

```bash
export ANDROID_HOME=...   # Android SDK
echo "sdk.dir=$ANDROID_HOME" > local.properties
./gradlew :app:assembleDebug
```

### Native llama.cpp

1. `git submodule update --init native/llama.cpp` (from repository root)
2. Install NDK via Android SDK Manager
3. Set `enableNativeLlm=true` in `gradle.properties`
4. Place or import a Qwen3 4B Q4 GGUF under app-private `files/models/`

## Architecture

See [../docs/architecture/android-mvp.md](../docs/architecture/android-mvp.md).

## Sprint 3 — local RAG

- `:rag` — import, chunk, embed, hybrid retrieval
- **Docs** tab — import PDF / text / Markdown (on-device)
- Chat attaches **sources** when RAG hits indexed chunks
- See [../docs/architecture/rag.md](../docs/architecture/rag.md)

## Sprint 2 — encryption & privacy

- `:security` — Keystore-backed `DatabasePassphraseProvider`
- SQLCipher Room database (`EncryptedNivaDatabaseFactory`)
- **Privacy** tab — clear chats, delete all local AI data (models kept)
- Migrates plaintext Sprint 1 `niva.db` automatically

## Tests

```bash
./gradlew :shared-domain:test \
  :security:testDebugUnitTest \
  :storage:testDebugUnitTest \
  :ai-runtime:testDebugUnitTest \
  :model-manager:testDebugUnitTest \
  :app:testDebugUnitTest
```
