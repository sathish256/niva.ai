package ai.niva.app.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ai.niva.domain.model.MessageRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val state by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Niva Chat")
                        Text(
                            text = state.privacyBanner,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = state.modelStatus, style = MaterialTheme.typography.labelMedium)
            state.errorMessage?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.messages) { message ->
                    val prefix = when (message.role) {
                        MessageRole.USER -> "You"
                        MessageRole.ASSISTANT -> "Assistant"
                        MessageRole.SYSTEM -> "System"
                        MessageRole.TOOL -> "Tool"
                    }
                    Text(text = "$prefix: ${message.content}")
                }
                if (state.isGenerating && state.streamingAssistantText.isNotEmpty()) {
                    item {
                        Text(text = "Assistant: ${state.streamingAssistantText}")
                    }
                }
            }
            OutlinedTextField(
                value = state.draft,
                onValueChange = viewModel::updateDraft,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Message") },
                enabled = !state.isGenerating,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = viewModel::sendMessage,
                    enabled = !state.isGenerating,
                ) { Text("Send") }
                Button(
                    onClick = viewModel::stopGeneration,
                    enabled = state.isGenerating,
                ) { Text("Stop") }
            }
        }
    }
}
