package ai.niva.app.documents

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(viewModel: DocumentsViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val name = uri.lastPathSegment ?: "document"
            viewModel.importDocument(uri, mime, name)
        }
    }
    Scaffold(topBar = { TopAppBar(title = { Text("Documents") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Processed on this device", style = MaterialTheme.typography.labelMedium)
            Button(onClick = { picker.launch(arrayOf("application/pdf", "text/plain", "text/markdown")) }) {
                Text("Import PDF / text / Markdown")
            }
            state.statusMessage?.let { Text(it) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.documents) { doc ->
                    Text("${doc.displayName} — ${doc.processingStatus}")
                }
            }
        }
    }
}
