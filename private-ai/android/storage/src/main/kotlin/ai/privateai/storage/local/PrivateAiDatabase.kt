package ai.privateai.storage.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ConversationEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class PrivateAiDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
}
