package ai.niva.app.di

import ai.niva.airuntime.llm.LlamaCppLLM
import ai.niva.airuntime.llm.LocalLLM
import ai.niva.airuntime.orchestrator.AIOrchestrator
import ai.niva.app.device.AndroidDeviceCapabilitiesProvider
import ai.niva.domain.network.NetworkPolicy
import ai.niva.modelmanager.ModelManager
import ai.niva.prompts.PromptRepository
import ai.niva.storage.ConversationRepository
import ai.niva.storage.local.ConversationDao
import ai.niva.storage.local.NivaDatabase
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NivaDatabase =
        Room.databaseBuilder(context, NivaDatabase::class.java, "niva.db").build()

    @Provides
    fun provideConversationDao(db: NivaDatabase): ConversationDao = db.conversationDao()

    @Provides
    @Singleton
    fun provideConversationRepository(dao: ConversationDao): ConversationRepository =
        ConversationRepository(dao)
}
