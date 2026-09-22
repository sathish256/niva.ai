package ai.niva.rag.ingest

import ai.niva.domain.rag.DocumentProcessingStatus
import ai.niva.domain.rag.DocumentRecord
import ai.niva.rag.RagIndexer
import ai.niva.rag.sha256
import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentImportService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val extractor: DocumentTextExtractor,
    private val indexer: RagIndexer,
) {
    suspend fun importFromUri(uri: Uri, mimeType: String, displayName: String): DocumentRecord {
        val id = UUID.randomUUID().toString()
        val docsDir = File(context.filesDir, "documents").apply { mkdirs() }
        val target = File(docsDir, "$id.enc")
        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Unable to read document")
        val bytes = target.readBytes()
        val checksum = sha256(bytes)
        val extracted = extractor.extract(uri, mimeType, target)
        val now = System.currentTimeMillis()
        val record = DocumentRecord(
            id = id,
            displayName = displayName,
            mimeType = mimeType,
            sizeBytes = bytes.size.toLong(),
            createdAt = now,
            modifiedAt = now,
            checksum = checksum,
            processingStatus = DocumentProcessingStatus.PENDING,
            pageCount = extracted.pageTexts.size.takeIf { it > 0 },
        )
        indexer.indexDocument(
            record = record,
            encryptedPath = target.absolutePath,
            extractedText = extracted.fullText,
            pageTexts = extracted.pageTexts,
        )
        return record.copy(processingStatus = DocumentProcessingStatus.READY)
    }
}
