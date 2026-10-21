package ai.niva.storage

import ai.niva.domain.security.DatabasePassphraseProvider
import ai.niva.storage.local.DocumentDao
import ai.niva.storage.local.PlaintextDatabaseMigrator
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class DataWipeResult(
    val conversationsCleared: Boolean,
    val databaseRemoved: Boolean,
    val tempFilesRemoved: Int,
)

@Singleton
class LocalAiDataWiper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val databaseFactory: EncryptedNivaDatabaseFactory,
    private val conversationRepository: ConversationRepository,
    private val documentDao: DocumentDao,
    private val passphraseProvider: DatabasePassphraseProvider,
) {

    suspend fun clearConversationsOnly() {
        conversationRepository.clearAll()
    }

    suspend fun deleteAllLocalAiData(deleteModels: Boolean = false): DataWipeResult {
        conversationRepository.clearAll()
        documentDao.clearIndex()
        databaseFactory.closeAndClearInstance()
        val dbRemoved = context.deleteDatabase(PlaintextDatabaseMigrator.DATABASE_NAME)
        passphraseProvider.clearPassphraseMaterial()
        var tempRemoved = 0
        tempDirs().forEach { dir ->
            if (dir.exists()) {
                tempRemoved += dir.walkTopDown().filter { it.isFile }.onEach { it.delete() }.count()
            }
        }
        if (deleteModels) {
            val models = File(context.filesDir, "models")
            if (models.exists()) {
                tempRemoved += models.walkTopDown().filter { it.isFile }.onEach { it.delete() }.count()
            }
        }
        return DataWipeResult(
            conversationsCleared = true,
            databaseRemoved = dbRemoved,
            tempFilesRemoved = tempRemoved,
        )
    }

    private fun tempDirs(): List<File> = listOf(
        File(context.cacheDir, "niva-temp"),
        File(context.filesDir, "attachments"),
        File(context.filesDir, "documents"),
    )
}
