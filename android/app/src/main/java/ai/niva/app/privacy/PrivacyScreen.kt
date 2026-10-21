package ai.niva.app.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(viewModel: PrivacyViewModel) {
    val state by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = { TopAppBar(title = { Text("Privacy") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(state.aiStatus, style = MaterialTheme.typography.titleMedium)
            Text(state.networkStatus)
            Text(state.encryptionStatus)
            Text(state.processingStatus)
            state.lastActionMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            Button(onClick = viewModel::requestClearConversations) {
                Text("Clear conversations")
            }
            Button(onClick = viewModel::requestDeleteAllData) {
                Text("Delete all local AI data")
            }
        }
    }
    if (state.confirmClearChats) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDialogs,
            title = { Text("Clear conversations?") },
            text = { Text("This removes all chats and messages from this device.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmClearConversations) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDialogs) { Text("Cancel") }
            },
        )
    }
    if (state.confirmDeleteAll) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDialogs,
            title = { Text("Delete all local AI data?") },
            text = {
                Text(
                    "Deletes encrypted conversations, messages, and temp files. " +
                        "Installed models are not removed.",
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDeleteAllLocalAiData) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDialogs) { Text("Cancel") }
            },
        )
    }
}
