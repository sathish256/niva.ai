package ai.niva.storage.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val createdAt: Long,
    val modifiedAt: Long,
    val checksum: String,
    val encryptedPath: String,
    val processingStatus: String,
    val pageCount: Int?,
)

@Entity(
    tableName = "document_chunks",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("documentId")],
)
data class DocumentChunkEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val pageNumber: Int?,
    val chunkIndex: Int,
    val encryptedText: String,
    val section: String?,
    val metadataJson: String?,
)

@Entity(
    tableName = "embeddings",
    foreignKeys = [
        ForeignKey(
            entity = DocumentChunkEntity::class,
            parentColumns = ["id"],
            childColumns = ["chunkId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("chunkId"), Index("modelId")],
)
data class EmbeddingEntity(
    @PrimaryKey val chunkId: String,
    val modelId: String,
    val dimensions: Int,
    val vector: ByteArray,
)
