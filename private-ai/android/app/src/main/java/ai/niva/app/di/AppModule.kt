package ai.niva.app.di

import ai.niva.airuntime.llm.LlamaCppLLM
import ai.niva.airuntime.llm.LocalLLM
import ai.niva.airuntime.orchestrator.AIOrchestrator
import ai.niva.domain.network.NetworkPolicy
import ai.niva.modelmanager.ModelManager
import ai.niva.prompts.PromptRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import android.content.Context
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNetworkPolicy(): NetworkPolicy = NetworkPolicy()

    @Provides
    @Singleton
    fun provideLocalLlm(): LocalLLM = LlamaCppLLM()

    @Provides
    @Singleton
    fun providePromptRepository(): PromptRepository = PromptRepository()

    @Provides
    @Singleton
    fun provideAiOrchestrator(
        localLlm: LocalLLM,
        networkPolicy: NetworkPolicy,
    ): AIOrchestrator = AIOrchestrator(localLlm, networkPolicy)

    @Provides
    @Singleton
    fun provideModelsRoot(@ApplicationContext context: Context): File =
        File(context.filesDir, "models").apply { mkdirs() }

    @Provides
    @Singleton
    fun provideModelManager(modelsRoot: File): ModelManager = ModelManager(modelsRoot)
}
