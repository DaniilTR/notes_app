package ru.uni.mdnotes.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import ru.uni.mdnotes.notes.NotesUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    noteId: String,
    notesStateFlow: StateFlow<NotesUiState>,
    onLoad: () -> Unit,
    onSave: (content: String) -> Unit,
    onBack: () -> Unit,
) {
    val state by notesStateFlow.collectAsState()

    var editText by remember(noteId) { mutableStateOf("") }
    var isLoaded by remember(noteId) { mutableStateOf(false) }
    var preview by remember(noteId) { mutableStateOf(true) }

    LaunchedEffect(noteId) {
        onLoad()
        isLoaded = false
    }

    LaunchedEffect(state.editorNoteId, state.editorContent) {
        if (!isLoaded && state.editorNoteId == noteId) {
            editText = state.editorContent
            isLoaded = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Заметка") },
                actions = {
                    TextButton(onClick = { preview = !preview }) {
                        Text(if (preview) "Редактировать" else "Просмотр")
                    }
                    if (!preview) {
                        TextButton(onClick = { onSave(editText) }) { Text("Сохранить") }
                    }
                    TextButton(onClick = onBack) { Text("Назад") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (!preview) {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize(),
                    label = { Text("Markdown (.md)") },
                )
            } else {
                MarkdownView(
                    markdown = editText,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }
}
