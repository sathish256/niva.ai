# PrivateAI — Android

Offline-first personal AI (Sprint 1 MVP).

## Build

```bash
export ANDROID_HOME=...   # Android SDK
echo "sdk.dir=$ANDROID_HOME" > local.properties
./gradlew :app:assembleDebug
```

### Native llama.cpp (on-device inference)

1. `git submodule update --init ../native/llama.cpp`
2. Install NDK via Android SDK Manager.
3. Set `enableNativeLlm=true` in `gradle.properties`.
4. Import a Qwen3 4B Q4 GGUF via the app’s model directory (`files/models/`).

Without native build, the JNI stub loads but `loadModel()` fails until a native library is built.

## Architecture

See [../docs/architecture/android-mvp.md](../docs/architecture/android-mvp.md).

## Tests

```bash
./gradlew :shared-domain:test :ai-runtime:testDebugUnitTest :model-manager:testDebugUnitTest :app:testDebugUnitTest
```
