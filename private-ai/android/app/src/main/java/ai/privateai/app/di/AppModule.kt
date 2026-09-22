package ai.privateai.app.di

import ai.privateai.airuntime.llm.LlamaCppLLM
import ai.privateai.airuntime.llm.LocalLLM
import ai.privateai.airuntime.orchestrator.AIOrchestrator
import ai.privateai.app.device.AndroidDeviceCapabilitiesProvider
import ai.privateai.domain.network.NetworkPolicy
import ai.privateai.modelmanager.ModelManager
import ai.privateai.prompts.PromptRepository
import ai.privateai.storage.ConversationRepository
import ai.privateai.storage.local.ConversationDao
import ai.privateai.storage.local.PrivateAiDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): PrivateAiDatabase =
        Room.databaseBuilder(context, PrivateAiDatabase::class.java, "private_ai.db").build()

    @Provides
    fun provideConversationDao(db: PrivateAiDatabase): ConversationDao = db.conversationDao()

    @Provides
    @Singleton
    fun provideConversationRepository(dao: ConversationDao): ConversationRepository =
        ConversationRepository(dao)
}
