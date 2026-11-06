package gg.urchin.client.chat

import gg.urchin.api.TagType

private const val NAME_CHAR = "[A-Za-z0-9_]"
private const val FORMATTING_CODE = "§[0-9a-fk-orA-FK-OR]"
private val FORMATTING_CODES = Regex(FORMATTING_CODE)
private val TAG_ICONS = TagType.entries.joinToString("|") { Regex.escape(it.icon) }
private val TAG_BADGE = """\[(?:$FORMATTING_CODE)*(?:$TAG_ICONS)(?:$FORMATTING_CODE)*]"""
private val ANY_TAG_BADGE = Regex(TAG_BADGE)
private val LEADING_TAG_BADGE = Regex("""(?:$FORMATTING_CODE|\s)*$TAG_BADGE""")
private val singleNamePatterns = HashMap<String, Regex>()

fun namePattern(names: Collection<String>): Regex =
    Regex("(?:(?<=$FORMATTING_CODE)|(?<!$NAME_CHAR))(?:${names.joinToString("|") { Regex.escape(it) }})(?!$NAME_CHAR)")

fun nameEnd(text: String, name: String): Int? =
    singleNamePatterns.getOrPut(name) { namePattern(listOf(name)) }.find(text)?.let { it.range.last + 1 }

fun insertAfterName(text: String, name: String, insert: String): String {
    val end = nameEnd(text, name) ?: return text + insert
    return text.substring(0, end) + insert + activeFormatting(text.substring(0, end)) + text.substring(end)
}

fun containsTagBadge(text: String): Boolean = ANY_TAG_BADGE.containsMatchIn(text)

fun startsWithTagBadge(text: String, index: Int): Boolean = LEADING_TAG_BADGE.matchesAt(text, index)

// For text that may or may not be the player's name (the score under a name tag is drawn the same way) and may
// already carry the label: only text showing the name gets it, and only once.
fun labelNameOnce(text: String, name: String, label: String): String =
    if (label in text || nameEnd(text, name) == null) text else insertAfterName(text, name, label)

fun activeFormatting(text: String): String =
    FORMATTING_CODES.findAll(text).fold("") { active, code ->
        when (code.value[1].lowercaseChar()) {
            'r' -> ""
            in 'k'..'o' -> active + code.value
            else -> code.value
        }
    }
