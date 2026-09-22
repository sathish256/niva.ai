package ai.niva.security.di

import ai.niva.domain.security.DatabasePassphraseProvider
import ai.niva.security.keystore.AndroidDatabasePassphraseProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {
    @Binds
    @Singleton
    abstract fun bindDatabasePassphraseProvider(
        impl: AndroidDatabasePassphraseProvider,
    ): DatabasePassphraseProvider
}
