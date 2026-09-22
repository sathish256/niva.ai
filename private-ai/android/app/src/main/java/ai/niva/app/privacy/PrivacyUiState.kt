package ai.niva.app.privacy

data class PrivacyUiState(
    val aiStatus: String = "AI: On Device",
    val networkStatus: String = "Network: Not Required",
    val encryptionStatus: String = "Database: Encrypted (SQLCipher)",
    val processingStatus: String = "Processed on this device",
    val lastActionMessage: String? = null,
    val confirmDeleteAll: Boolean = false,
    val confirmClearChats: Boolean = false,
)
