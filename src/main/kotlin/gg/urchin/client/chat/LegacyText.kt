package gg.urchin.client.chat

import gg.urchin.api.TagType

private const val NAME_CHAR = "[A-Za-z0-9_]"
private const val FORMATTING_CODE = "§[0-9a-fk-orA-FK-OR]"
private val TAG_ICONS = TagType.entries.joinToString("|") { Regex.escape(it.icon) }
private val TAG_BADGE = """\[(?:$FORMATTING_CODE)*(?:$TAG_ICONS)(?:$FORMATTING_CODE)*]"""
private val ANY_TAG_BADGE = Regex(TAG_BADGE)
private val LEADING_TAG_BADGE = Regex("""(?:$FORMATTING_CODE|\s)*$TAG_BADGE""")
private const val FORMATTING_PREFIX = '§'

fun namePattern(names: Collection<String>): Regex =
    Regex("(?:(?<=$FORMATTING_CODE)|(?<!$NAME_CHAR))(?:${names.joinToString("|") { Regex.escape(it) }})(?!$NAME_CHAR)")

fun nameEnd(text: String, name: String): Int? {
    var start = text.indexOf(name)
    while (start >= 0) {
        val end = start + name.length
        if (opensName(text, start) && (end == text.length || !text[end].isNameChar())) return end
        start = text.indexOf(name, start + 1)
    }
    return null
}

private fun opensName(text: String, index: Int): Boolean =
    index == 0 || !text[index - 1].isNameChar() || (index >= 2 && text[index - 2] == FORMATTING_PREFIX && text[index - 1].isFormattingCode())

private fun Char.isNameChar(): Boolean = this in 'a'..'z' || this in 'A'..'Z' || this in '0'..'9' || this == '_'

private fun Char.isFormattingCode(): Boolean = lowercaseChar().let { it in '0'..'9' || it in 'a'..'f' || it in 'k'..'o' || it == 'r' }

fun insertAfterName(text: String, name: String, insert: String): String {
    val end = nameEnd(text, name) ?: return text + insert
    return text.substring(0, end) + insert + activeFormatting(text.substring(0, end)) + text.substring(end)
}

fun containsTagBadge(text: String): Boolean = ANY_TAG_BADGE.containsMatchIn(text)

fun startsWithTagBadge(text: String, index: Int): Boolean = LEADING_TAG_BADGE.matchesAt(text, index)

fun activeFormatting(text: String): String {
    val active = StringBuilder()
    var index = text.indexOf(FORMATTING_PREFIX)
    while (index >= 0 && index + 1 < text.length) {
        val code = text[index + 1]
        if (!code.isFormattingCode()) {
            index = text.indexOf(FORMATTING_PREFIX, index + 1)
            continue
        }
        when (code.lowercaseChar()) {
            'r' -> active.setLength(0)
            in 'k'..'o' -> active.append(FORMATTING_PREFIX).append(code)
            else -> {
                active.setLength(0)
                active.append(FORMATTING_PREFIX).append(code)
            }
        }
        index = text.indexOf(FORMATTING_PREFIX, index + 2)
    }
    return active.toString()
}
