package ai.niva.storage

import ai.niva.domain.security.DatabasePassphraseProvider
import ai.niva.storage.local.ConversationDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LocalAiDataWiperTest {

    @Test
    fun `clear conversations only wipes repository`() = runTest {
        val dao = mockk<ConversationDao>(relaxed = true)
        coEvery { dao.deleteAllMessages() } returns Unit
        coEvery { dao.deleteAllConversations() } returns Unit
        val repository = ConversationRepository(dao)
        val factory = mockk<EncryptedNivaDatabaseFactory>(relaxed = true)
        val passphrase = mockk<DatabasePassphraseProvider>(relaxed = true)
        val context = mockk<android.content.Context>(relaxed = true)
        every { context.filesDir } returns java.io.File.createTempFile("niva", "").parentFile!!
        every { context.cacheDir } returns java.io.File.createTempFile("cache", "").parentFile!!
        val documentDao = mockk<ai.niva.storage.local.DocumentDao>(relaxed = true)
        coEvery { documentDao.clearIndex() } returns Unit
        val wiper = LocalAiDataWiper(context, factory, repository, documentDao, passphrase)
        wiper.clearConversationsOnly()
        coVerify { dao.deleteAllMessages() }
        coVerify { dao.deleteAllConversations() }
    }
}
