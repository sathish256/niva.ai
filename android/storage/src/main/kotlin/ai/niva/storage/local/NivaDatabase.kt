package ai.niva.storage.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        DocumentEntity::class,
        DocumentChunkEntity::class,
        EmbeddingEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class NivaDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun documentDao(): DocumentDao
}
