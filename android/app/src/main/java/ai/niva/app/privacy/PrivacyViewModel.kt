package ai.niva.app.privacy

import ai.niva.storage.LocalAiDataWiper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PrivacyViewModel @Inject constructor(
    private val dataWiper: LocalAiDataWiper,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrivacyUiState())
    val uiState: StateFlow<PrivacyUiState> = _uiState.asStateFlow()

    fun requestClearConversations() {
        _uiState.update { it.copy(confirmClearChats = true) }
    }

    fun requestDeleteAllData() {
        _uiState.update { it.copy(confirmDeleteAll = true) }
    }

    fun dismissDialogs() {
        _uiState.update { it.copy(confirmClearChats = false, confirmDeleteAll = false) }
    }

    fun confirmClearConversations() {
        viewModelScope.launch {
            dataWiper.clearConversationsOnly()
            _uiState.update {
                it.copy(
                    confirmClearChats = false,
                    lastActionMessage = "All conversations cleared.",
                )
            }
        }
    }

    fun confirmDeleteAllLocalAiData() {
        viewModelScope.launch {
            val result = dataWiper.deleteAllLocalAiData(deleteModels = false)
            _uiState.update {
                it.copy(
                    confirmDeleteAll = false,
                    lastActionMessage = "Local AI data removed. Models were kept. (${result.tempFilesRemoved} temp files)",
                )
            }
        }
    }
}
