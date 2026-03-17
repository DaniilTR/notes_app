package ru.uni.mdnotes.notes

interface NotesRepository {
    fun listNotes(): List<NoteMeta>
    fun createNoteId(): String
    fun readNoteContent(id: String): String
    fun writeNoteContent(id: String, content: String)
    fun deleteNote(id: String)
}
