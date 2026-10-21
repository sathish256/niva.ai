package ai.niva.storage.local

import android.content.Context
import androidx.room.Room
import java.io.File
import kotlinx.coroutines.runBlocking

internal data class PlaintextSnapshot(
    val conversations: List<ConversationEntity>,
    val messages: List<MessageEntity>,
)

/**
 * Reads Sprint 1 plaintext Room data before replacing the file with SQLCipher.
 */
internal object PlaintextDatabaseMigrator {

    const val DATABASE_NAME = "niva.db"

    fun readPlaintextIfPresent(context: Context): PlaintextSnapshot? {
        val dbPath = context.getDatabasePath(DATABASE_NAME)
        if (!dbPath.exists() || !isPlaintextSqlite(dbPath)) {
            return null
        }
        val plaintext = Room.databaseBuilder(context, NivaDatabase::class.java, DATABASE_NAME)
            .allowMainThreadQueries()
            .build()
        return try {
            runBlocking {
                PlaintextSnapshot(
                    conversations = plaintext.conversationDao().getAllConversationsOnce(),
                    messages = plaintext.conversationDao().getAllMessagesOnce(),
                )
            }
        } finally {
            plaintext.close()
        }
    }

    fun deletePlaintextFiles(context: Context) {
        context.deleteDatabase(DATABASE_NAME)
        val parent = context.getDatabasePath(DATABASE_NAME).parentFile
        parent?.listFiles()?.filter { it.name.startsWith(DATABASE_NAME) }?.forEach { it.delete() }
    }

    suspend fun restore(snapshot: PlaintextSnapshot, database: NivaDatabase) {
        snapshot.conversations.forEach { database.conversationDao().upsertConversation(it) }
        snapshot.messages.forEach { database.conversationDao().upsertMessage(it) }
    }

    private fun isPlaintextSqlite(dbPath: File): Boolean {
        dbPath.inputStream().use { input ->
            val header = ByteArray(16)
            if (input.read(header) < 16) return false
            val sqliteMagic = "SQLite format 3\u0000".toByteArray(Charsets.UTF_8)
            return header.copyOf(sqliteMagic.size).contentEquals(sqliteMagic)
        }
    }
}
