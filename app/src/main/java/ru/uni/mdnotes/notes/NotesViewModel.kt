package ru.uni.mdnotes.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotesUiState(
    val notes: List<NoteMeta> = emptyList(),
    val editorNoteId: String? = null,
    val editorContent: String = "",
)

class NotesViewModel(
    private val repository: NotesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(NotesUiState())
    val state: StateFlow<NotesUiState> = _state

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val notes = repository.listNotes()
            _state.update { it.copy(notes = notes) }
        }
    }

    fun createNewNote(): String {
        val id = repository.createNoteId()
        repository.writeNoteContent(id, "")
        refresh()
        return id
    }

    fun deleteNote(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(id)
            _state.update {
                if (it.editorNoteId == id) it.copy(editorNoteId = null, editorContent = "") else it
            }
            refresh()
        }
    }

    fun loadNote(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val content = repository.readNoteContent(id)
            _state.update { it.copy(editorNoteId = id, editorContent = content) }
        }
    }

    fun saveNote(id: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.writeNoteContent(id, content)
            _state.update { it.copy(editorNoteId = id, editorContent = content) }
            refresh()
        }
    }

    companion object {
        fun factory(repository: NotesRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NotesViewModel(repository) as T
                }
            }
    }
}
