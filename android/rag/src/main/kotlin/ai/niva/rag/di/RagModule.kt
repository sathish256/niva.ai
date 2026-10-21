package ai.niva.rag.di

import ai.niva.domain.rag.EmbeddingModel
import ai.niva.domain.rag.RagContextProvider
import ai.niva.domain.rag.TextChunker
import ai.niva.rag.RagRetriever
import ai.niva.rag.embedding.HashEmbeddingModel
import ai.niva.rag.search.HybridSearchRanker
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RagBindsModule {
    @Binds
    @Singleton
    abstract fun bindRagContextProvider(impl: RagRetriever): RagContextProvider

    @Binds
    @Singleton
    abstract fun bindEmbeddingModel(impl: HashEmbeddingModel): EmbeddingModel
}

@Module
@InstallIn(SingletonComponent::class)
object RagProvidesModule {
    @Provides
    @Singleton
    fun provideChunker(): TextChunker = TextChunker()

    @Provides
    @Singleton
    fun provideRanker(): HybridSearchRanker = HybridSearchRanker()
}
