# Niva — Android

Offline-first personal AI for Android (Sprint 1).

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

1. `git submodule update --init private-ai/native/llama.cpp` (from repo root)
2. Install NDK via Android SDK Manager
3. Set `enableNativeLlm=true` in `gradle.properties`
4. Place or import a Qwen3 4B Q4 GGUF under app-private `files/models/`

## Architecture

See [../docs/architecture/android-mvp.md](../docs/architecture/android-mvp.md).

## Tests

```bash
./gradlew :shared-domain:test :ai-runtime:testDebugUnitTest :model-manager:testDebugUnitTest :app:testDebugUnitTest
```
