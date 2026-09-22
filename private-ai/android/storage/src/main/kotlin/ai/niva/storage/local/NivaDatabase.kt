package ai.niva.storage.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ConversationEntity::class, MessageEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class NivaDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
}
