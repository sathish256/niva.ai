package ai.niva.app.documents

import ai.niva.domain.rag.DocumentRecord
import ai.niva.rag.ingest.DocumentImportService
import ai.niva.storage.local.DocumentDao
import ai.niva.storage.local.DocumentEntity
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ai.niva.domain.rag.DocumentProcessingStatus

data class DocumentsUiState(
    val documents: List<DocumentRecord> = emptyList(),
    val statusMessage: String? = null,
)

@HiltViewModel
class DocumentsViewModel @Inject constructor(
    private val importService: DocumentImportService,
    private val documentDao: DocumentDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentsUiState())
    val uiState: StateFlow<DocumentsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            documentDao.observeDocuments().map { list -> list.map { it.toDomain() } }
                .collect { docs -> _uiState.update { it.copy(documents = docs) } }
        }
    }

    fun importDocument(uri: Uri, mimeType: String, name: String) {
        viewModelScope.launch {
            try {
                importService.importFromUri(uri, mimeType, name)
                _uiState.update { it.copy(statusMessage = "Indexed on this device: $name") }
            } catch (ex: Exception) {
                _uiState.update { it.copy(statusMessage = "Import failed") }
            }
        }
    }
}

private fun DocumentEntity.toDomain() = DocumentRecord(
    id = id,
    displayName = displayName,
    mimeType = mimeType,
    sizeBytes = sizeBytes,
    createdAt = createdAt,
    modifiedAt = modifiedAt,
    checksum = checksum,
    processingStatus = DocumentProcessingStatus.valueOf(processingStatus),
    pageCount = pageCount,
)
