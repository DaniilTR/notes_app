package ru.uni.mdnotes.notes

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FileNotesRepository(context: Context) : NotesRepository {
    private val baseDir: File = File(context.filesDir, "notes").apply { mkdirs() }

    override fun listNotes(): List<NoteMeta> {
        ensureMarkdownBasicsExists()

        val files = baseDir.listFiles { file -> file.isFile && file.extension.lowercase() == "md" }
            ?.toList()
            ?: emptyList()

        val metas = files
            .map { file ->
                val content = runCatching { file.readText() }.getOrDefault("")
                NoteMeta(
                    id = file.nameWithoutExtension,
                    title = extractTitle(content),
                    preview = extractPreview(content),
                    createdAtMillis = parseCreatedAtMillis(file.nameWithoutExtension)
                        ?: file.lastModified(),
                    updatedAtMillis = file.lastModified(),
                    pinned = file.nameWithoutExtension == MARKDOWN_BASICS_ID,
                )
            }

        val pinned = metas.firstOrNull { it.id == MARKDOWN_BASICS_ID }
        val rest = metas
            .asSequence()
            .filterNot { it.id == MARKDOWN_BASICS_ID }
            .sortedByDescending { it.updatedAtMillis }
            .toList()

        return listOfNotNull(pinned) + rest
    }

    override fun createNoteId(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        return sdf.format(Date())
    }

    override fun readNoteContent(id: String): String {
        val file = noteFile(id)
        return if (file.exists()) file.readText() else ""
    }

    override fun writeNoteContent(id: String, content: String) {
        val file = noteFile(id)
        file.writeText(content)
    }

    override fun deleteNote(id: String) {
        if (id == MARKDOWN_BASICS_ID) return
        noteFile(id).delete()
    }

    private fun noteFile(id: String): File = File(baseDir, "$id.md")

    private fun ensureMarkdownBasicsExists() {
        val file = noteFile(MARKDOWN_BASICS_ID)
        if (file.exists()) return

        file.writeText(MARKDOWN_BASICS_CONTENT)
    }

    private fun extractTitle(markdown: String): String {
        val lines = markdown.lineSequence().map { it.trim() }.toList()
        val heading = lines.firstOrNull { it.startsWith("# ") }
        if (heading != null) return heading.removePrefix("# ").trim().ifBlank { "Без названия" }

        val firstNonEmpty = lines.firstOrNull { it.isNotBlank() }
        val raw = firstNonEmpty ?: "Без названия"
        return raw.take(40)
    }

    private fun extractPreview(markdown: String): String {
        val lines = markdown.lineSequence().map { it.trim() }
        val candidate = lines
            .filter { it.isNotBlank() }
            .firstOrNull { line ->
                // Заголовок не используем как превью
                !line.startsWith("#")
            }

        val raw = candidate ?: "Нет текста"
        return raw.take(120)
    }

    private fun parseCreatedAtMillis(id: String): Long? {
        // id создаётся как yyyyMMdd_HHmmss
        return runCatching {
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            sdf.isLenient = false
            sdf.parse(id)?.time
        }.getOrNull()
    }

    private companion object {
        private const val MARKDOWN_BASICS_ID = "00_markdown_basics"

        private val MARKDOWN_BASICS_CONTENT = """
# Основы .md (Markdown)

Ниже — самые частые элементы MD-синтаксиса.

## Заголовки

# Заголовок 1
## Заголовок 2
### Заголовок 3

## Текст

Обычный текст.

**Жирный**

*Курсив*

~~Зачёркнутый~~

## Списки

- пункт
- пункт
  - вложенный пункт

1. первый
2. второй
3. третий

## Чекбоксы

- [ ] задача
- [x] сделано

## Ссылки

[Google](https://google.com)

## Цитаты

> Это цитата

## Код

`inline code`

```kotlin
fun main() {
    println("Hello")
}
```

## Таблица

| Колонка | Колонка |
|--------:|:--------|
| справа  | слева   |
""".trimIndent()
    }
}
