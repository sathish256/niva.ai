#include <jni.h>
#include <android/log.h>
#include <atomic>
#include <mutex>
#include <string>
#include <vector>

#define LOG_TAG "PrivateAILlama"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static std::atomic<bool> g_cancel{false};
static std::mutex g_mutex;

#if defined(PRIVATEAI_LLAMA_CPP)
#include "llama.h"
#include "common.h"

static llama_model *g_model = nullptr;
static llama_context *g_ctx = nullptr;
#endif

static bool call_token_bridge(JNIEnv *env, const std::string &token) {
    jclass bridge = env->FindClass("ai/privateai/airuntime/llm/LlamaTokenBridge");
    if (!bridge) {
        LOGE("LlamaTokenBridge not found");
        return false;
    }
    jmethodID method = env->GetStaticMethodID(bridge, "onToken", "(Ljava/lang/String;)Z");
    if (!method) {
        LOGE("onToken not found");
        return false;
    }
    jstring jtoken = env->NewStringUTF(token.c_str());
    jboolean cont = env->CallStaticBooleanMethod(bridge, method, jtoken);
    env->DeleteLocalRef(jtoken);
    return cont == JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_ai_privateai_airuntime_llm_LlamaNative_loadModel(JNIEnv *env, jobject /*thiz*/, jstring path,
                                                      jint context_length) {
#if !defined(PRIVATEAI_LLAMA_CPP)
    LOGE("llama.cpp not compiled into native library");
    return JNI_FALSE;
#else
    std::lock_guard<std::mutex> lock(g_mutex);
    const char *model_path = env->GetStringUTFChars(path, nullptr);
    llama_backend_init();

    llama_model_params mparams = llama_model_default_params();
    g_model = llama_load_model_from_file(model_path, mparams);
    env->ReleaseStringUTFChars(path, model_path);
    if (!g_model) {
        LOGE("MODEL_LOAD_FAILED");
        return JNI_FALSE;
    }

    llama_context_params cparams = llama_context_default_params();
    cparams.n_ctx = static_cast<uint32_t>(context_length);
    g_ctx = llama_init_from_model(g_model, cparams);
    if (!g_ctx) {
        LOGE("CONTEXT_INIT_FAILED");
        llama_free_model(g_model);
        g_model = nullptr;
        return JNI_FALSE;
    }
    return JNI_TRUE;
#endif
}

extern "C" JNIEXPORT void JNICALL
Java_ai_privateai_airuntime_llm_LlamaNative_unloadModel(JNIEnv * /*env*/, jobject /*thiz*/) {
#if defined(PRIVATEAI_LLAMA_CPP)
    std::lock_guard<std::mutex> lock(g_mutex);
    if (g_ctx) {
        llama_free(g_ctx);
        g_ctx = nullptr;
    }
    if (g_model) {
        llama_free_model(g_model);
        g_model = nullptr;
    }
    llama_backend_free();
#endif
}

extern "C" JNIEXPORT void JNICALL
Java_ai_privateai_airuntime_llm_LlamaNative_cancelGeneration(JNIEnv * /*env*/, jobject /*thiz*/) {
    g_cancel.store(true);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_ai_privateai_airuntime_llm_LlamaNative_streamCompletionNative(JNIEnv *env, jobject /*thiz*/,
                                                                   jstring prompt, jint max_tokens,
                                                                   jfloat temperature) {
#if !defined(PRIVATEAI_LLAMA_CPP)
    LOGE("streamCompletion unavailable without llama.cpp");
    return JNI_FALSE;
#else
    if (!g_ctx || !g_model) {
        LOGE("Model not loaded");
        return JNI_FALSE;
    }
    g_cancel.store(false);
    const char *prompt_c = env->GetStringUTFChars(prompt, nullptr);
    std::string prompt_str(prompt_c);
    env->ReleaseStringUTFChars(prompt, prompt_c);

    std::vector<llama_token> tokens = common_tokenize(g_ctx, prompt_str, true, true);
    llama_batch batch = llama_batch_get_one(tokens.data(), static_cast<int32_t>(tokens.size()));
    if (llama_decode(g_ctx, batch) != 0) {
        LOGE("Initial decode failed");
        return JNI_FALSE;
    }

    const int n_vocab = llama_n_vocab(g_model);
    llama_sampler *sampler = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(sampler, llama_sampler_init_temp(temperature));
    llama_sampler_chain_add(sampler, llama_sampler_init_dist(LLAMA_DEFAULT_SEED));

    for (int i = 0; i < max_tokens; ++i) {
        if (g_cancel.load()) {
            break;
        }
        llama_token next = llama_sampler_sample(sampler, g_ctx, -1);
        llama_sampler_accept(sampler, next);
        if (llama_token_is_eog(g_model, next)) {
            break;
        }
        std::string piece = common_token_to_piece(g_ctx, next);
        if (!call_token_bridge(env, piece)) {
            break;
        }
        llama_batch next_batch = llama_batch_get_one(&next, 1);
        if (llama_decode(g_ctx, next_batch) != 0) {
            LOGE("Decode failed during generation");
            llama_sampler_free(sampler);
            return JNI_FALSE;
        }
    }
    llama_sampler_free(sampler);
    return JNI_TRUE;
#endif
}
