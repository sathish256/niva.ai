package ai.niva.storage.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object NivaMigrations {
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS documents (
                    id TEXT NOT NULL PRIMARY KEY,
                    displayName TEXT NOT NULL,
                    mimeType TEXT NOT NULL,
                    sizeBytes INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    modifiedAt INTEGER NOT NULL,
                    checksum TEXT NOT NULL,
                    encryptedPath TEXT NOT NULL,
                    processingStatus TEXT NOT NULL,
                    pageCount INTEGER
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS document_chunks (
                    id TEXT NOT NULL PRIMARY KEY,
                    documentId TEXT NOT NULL,
                    pageNumber INTEGER,
                    chunkIndex INTEGER NOT NULL,
                    encryptedText TEXT NOT NULL,
                    section TEXT,
                    metadataJson TEXT,
                    FOREIGN KEY(documentId) REFERENCES documents(id) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_document_chunks_documentId ON document_chunks(documentId)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS embeddings (
                    chunkId TEXT NOT NULL PRIMARY KEY,
                    modelId TEXT NOT NULL,
                    dimensions INTEGER NOT NULL,
                    vector BLOB NOT NULL,
                    FOREIGN KEY(chunkId) REFERENCES document_chunks(id) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_embeddings_chunkId ON embeddings(chunkId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_embeddings_modelId ON embeddings(modelId)")
        }
    }
}
