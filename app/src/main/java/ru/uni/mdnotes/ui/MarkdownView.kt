package ru.uni.mdnotes.ui

import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon

@Composable
fun MarkdownView(
    markdown: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val markwon = remember(context) { Markwon.create(context) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextView(ctx).apply {
                textSize = 16f
            }
        },
        update = { textView ->
            val node = markwon.parse(markdown)
            val rendered = markwon.render(node)
            markwon.setParsedMarkdown(textView, rendered)
        }
    )
}
