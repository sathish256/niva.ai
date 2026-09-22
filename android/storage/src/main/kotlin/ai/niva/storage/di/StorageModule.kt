package ai.niva.storage.di

import ai.niva.storage.ConversationRepository
import ai.niva.storage.EncryptedNivaDatabaseFactory
import ai.niva.storage.local.ConversationDao
import ai.niva.storage.local.NivaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideDatabase(factory: EncryptedNivaDatabaseFactory): NivaDatabase =
        factory.getDatabase()

    @Provides
    fun provideConversationDao(db: NivaDatabase): ConversationDao = db.conversationDao()

    @Provides
    @Singleton
    fun provideConversationRepository(dao: ConversationDao): ConversationRepository =
        ConversationRepository(dao)
}
