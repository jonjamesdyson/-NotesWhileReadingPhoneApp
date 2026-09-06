package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SophisticatedGold
import com.example.ui.theme.SophisticatedGoldContainer
import com.example.ui.theme.SophisticatedSurfaceVariant
import com.example.ui.theme.SophisticatedTextMuted
import com.example.ui.theme.SophisticatedTextPrimary

/**
 * Parses markdown inline styles (bold, italic, inline code) into an AnnotatedString.
 */
fun parseInlineMarkdown(
    text: String,
    baseColor: Color = SophisticatedTextPrimary,
    accentColor: Color = SophisticatedGold
): AnnotatedString {
    return buildAnnotatedString {
        var index = 0
        val length = text.length

        while (index < length) {
            // Bold italic: ***text***
            if (index + 3 < length && text.substring(index, index + 3) == "***") {
                val endIndex = text.indexOf("***", index + 3)
                if (endIndex != -1) {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            color = baseColor
                        )
                    ) {
                        append(text.substring(index + 3, endIndex))
                    }
                    index = endIndex + 3
                    continue
                }
            }

            // Bold: **text** or __text__
            if (index + 2 < length && (text.substring(index, index + 2) == "**" || text.substring(index, index + 2) == "__")) {
                val delimiter = text.substring(index, index + 2)
                val endIndex = text.indexOf(delimiter, index + 2)
                if (endIndex != -1) {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = baseColor
                        )
                    ) {
                        append(text.substring(index + 2, endIndex))
                    }
                    index = endIndex + 2
                    continue
                }
            }

            // Italic: *text* or _text_
            if (index + 1 < length && (text[index] == '*' || text[index] == '_') && (index == 0 || text[index - 1].isWhitespace())) {
                val delimiter = text[index]
                val endIndex = text.indexOf(delimiter, index + 1)
                if (endIndex != -1 && endIndex > index + 1) {
                    withStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic,
                            color = baseColor
                        )
                    ) {
                        append(text.substring(index + 1, endIndex))
                    }
                    index = endIndex + 1
                    continue
                }
            }

            // Inline code: `code`
            if (text[index] == '`') {
                val endIndex = text.indexOf('`', index + 1)
                if (endIndex != -1) {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = SophisticatedSurfaceVariant,
                            color = accentColor,
                            fontSize = 12.sp
                        )
                    ) {
                        append(" ${text.substring(index + 1, endIndex)} ")
                    }
                    index = endIndex + 1
                    continue
                }
            }

            // Regular character
            withStyle(SpanStyle(color = baseColor)) {
                append(text[index].toString())
            }
            index++
        }
    }
}

private sealed interface MarkdownBlock {
    data class Paragraph(val text: String) : MarkdownBlock
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class BulletItem(val text: String) : MarkdownBlock
    data class NumberedItem(val number: String, val text: String) : MarkdownBlock
    data class Quote(val text: String) : MarkdownBlock
    data object Blank : MarkdownBlock
}

private fun parseMarkdownBlocks(content: String): List<MarkdownBlock> {
    val lines = content.lines()
    val blocks = mutableListOf<MarkdownBlock>()
    val quoteAccumulator = mutableListOf<String>()

    fun flushQuote() {
        if (quoteAccumulator.isNotEmpty()) {
            blocks.add(MarkdownBlock.Quote(quoteAccumulator.joinToString("\n")))
            quoteAccumulator.clear()
        }
    }

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.startsWith(">")) {
            quoteAccumulator.add(trimmed.removePrefix(">").trimStart())
            continue
        } else {
            flushQuote()
        }

        if (trimmed.isEmpty()) {
            blocks.add(MarkdownBlock.Blank)
            continue
        }

        if (trimmed.startsWith("### ")) {
            blocks.add(MarkdownBlock.Heading(3, trimmed.removePrefix("### ").trim()))
            continue
        }
        if (trimmed.startsWith("## ")) {
            blocks.add(MarkdownBlock.Heading(2, trimmed.removePrefix("## ").trim()))
            continue
        }
        if (trimmed.startsWith("# ")) {
            blocks.add(MarkdownBlock.Heading(1, trimmed.removePrefix("# ").trim()))
            continue
        }
        if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
            blocks.add(MarkdownBlock.BulletItem(trimmed.substring(2).trim()))
            continue
        }
        val numMatch = Regex("^([0-9]+)\\.\\s+(.*)").find(trimmed)
        if (numMatch != null) {
            blocks.add(MarkdownBlock.NumberedItem(numMatch.groupValues[1], numMatch.groupValues[2]))
            continue
        }
        blocks.add(MarkdownBlock.Paragraph(trimmed))
    }
    flushQuote()
    return blocks
}

/**
 * Rich Composable that renders full block-level and inline Markdown:
 * - # Heading 1, ## Heading 2, ### Heading 3
 * - > Blockquotes
 * - - / * Bullet lists
 * - 1. Numbered lists
 * - Inline bold, italic, code
 */
@Composable
fun MarkdownContent(
    content: String,
    modifier: Modifier = Modifier,
    baseFontSize: TextUnit = 14.sp,
    textColor: Color = SophisticatedTextPrimary,
    accentColor: Color = SophisticatedGold
) {
    val blocks = remember(content) { parseMarkdownBlocks(content) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (block in blocks) {
            when (block) {
                is MarkdownBlock.Heading -> {
                    val fontScale = when (block.level) {
                        1 -> 4
                        2 -> 2
                        else -> 1
                    }
                    val weight = if (block.level == 1) FontWeight.Bold else FontWeight.SemiBold
                    val topPadding = if (block.level == 1) 4.dp else 2.dp
                    Text(
                        text = parseInlineMarkdown(block.text, baseColor = textColor, accentColor = accentColor),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = weight,
                            fontSize = (baseFontSize.value + fontScale).sp,
                            color = accentColor
                        ),
                        modifier = Modifier.padding(top = topPadding, bottom = 2.dp)
                    )
                }
                is MarkdownBlock.Quote -> {
                    QuoteCard(text = block.text, accentColor = accentColor)
                }
                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 6.dp, top = 2.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 7.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Text(
                            text = parseInlineMarkdown(block.text, baseColor = textColor, accentColor = accentColor),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = baseFontSize,
                                lineHeight = (baseFontSize.value + 6).sp
                            )
                        )
                    }
                }
                is MarkdownBlock.NumberedItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 6.dp, top = 2.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${block.number}.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                fontSize = baseFontSize
                            )
                        )
                        Text(
                            text = parseInlineMarkdown(block.text, baseColor = textColor, accentColor = accentColor),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = baseFontSize,
                                lineHeight = (baseFontSize.value + 6).sp
                            )
                        )
                    }
                }
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = parseInlineMarkdown(block.text, baseColor = textColor, accentColor = accentColor),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = baseFontSize,
                            lineHeight = (baseFontSize.value + 6).sp,
                            color = textColor
                        )
                    )
                }
                is MarkdownBlock.Blank -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun MarkdownContent(
    markdown: String,
    modifier: Modifier = Modifier
) {
    MarkdownContent(content = markdown, modifier = modifier)
}

@Composable
private fun QuoteCard(
    text: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .height(IntrinsicSize.Min)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(accentColor)
        )
        Text(
            text = parseInlineMarkdown(text, baseColor = SophisticatedTextPrimary.copy(alpha = 0.9f), accentColor = accentColor),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontStyle = FontStyle.Italic,
                lineHeight = 20.sp,
                fontSize = 13.sp
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

/**
 * Quick formatting action buttons for inserting Markdown tags into notes and reviews.
 */
@Composable
fun MarkdownToolbar(
    currentText: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    MarkdownToolbar(
        onInsert = { prefix, suffix ->
            onTextChange(currentText + prefix + suffix)
        },
        modifier = modifier
    )
}

@Composable
fun MarkdownToolbar(
    onInsert: (prefix: String, suffix: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MarkdownChip(label = "B", description = "Bold") { onInsert("**", "**") }
        MarkdownChip(label = "I", fontStyle = FontStyle.Italic, description = "Italic") { onInsert("*", "*") }
        MarkdownChip(label = "H1", description = "Heading 1") { onInsert("# ", "") }
        MarkdownChip(label = "H2", description = "Heading 2") { onInsert("## ", "") }
        MarkdownChip(label = "\"", description = "Quote") { onInsert("> ", "") }
        MarkdownChip(label = "• List", description = "Bullet List") { onInsert("- ", "") }
        MarkdownChip(label = "`Code`", fontFamily = FontFamily.Monospace, description = "Code") { onInsert("`", "`") }
    }
}

@Composable
private fun MarkdownChip(
    label: String,
    description: String,
    fontStyle: FontStyle = FontStyle.Normal,
    fontFamily: FontFamily = FontFamily.Default,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = SophisticatedSurfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = fontStyle,
                    fontFamily = fontFamily,
                    color = SophisticatedGold,
                    fontSize = 11.sp
                )
            )
        }
    }
}
