package ru.uni.mdnotes.notes

data class NoteMeta(
    val id: String,
    val title: String,
    val preview: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val pinned: Boolean = false,
)
