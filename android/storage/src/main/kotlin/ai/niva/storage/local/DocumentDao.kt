package ai.niva.storage.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY createdAt DESC")
    fun observeDocuments(): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDocument(document: DocumentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChunk(chunk: DocumentChunkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEmbedding(embedding: EmbeddingEntity)

    @Query("SELECT * FROM document_chunks")
    suspend fun getAllChunks(): List<DocumentChunkEntity>

    @Query("SELECT * FROM embeddings WHERE modelId = :modelId")
    suspend fun getEmbeddingsForModel(modelId: String): List<EmbeddingEntity>

    @Query(
        """
        SELECT c.* FROM document_chunks c
        INNER JOIN documents d ON d.id = c.documentId
        WHERE c.encryptedText LIKE '%' || :term || '%'
        """,
    )
    suspend fun keywordSearch(term: String): List<DocumentChunkEntity>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocument(id: String): DocumentEntity?

    @Query("DELETE FROM documents")
    suspend fun deleteAllDocuments()

    @Query("DELETE FROM document_chunks")
    suspend fun deleteAllChunks()

    @Query("DELETE FROM embeddings")
    suspend fun deleteAllEmbeddings()

    @Transaction
    suspend fun clearIndex() {
        deleteAllEmbeddings()
        deleteAllChunks()
        deleteAllDocuments()
    }
}
