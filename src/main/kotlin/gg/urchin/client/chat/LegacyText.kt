package gg.urchin.client.chat

private const val NAME_CHAR = "[A-Za-z0-9_]"
private const val FORMATTING_CODE = "§[0-9a-fk-orA-FK-OR]"
private val FORMATTING_CODES = Regex(FORMATTING_CODE)
private val singleNamePatterns = HashMap<String, Regex>()

fun namePattern(names: Collection<String>): Regex =
    Regex("(?:(?<=$FORMATTING_CODE)|(?<!$NAME_CHAR))(?:${names.joinToString("|") { Regex.escape(it) }})(?!$NAME_CHAR)")

fun nameEnd(text: String, name: String): Int? =
    singleNamePatterns.getOrPut(name) { namePattern(listOf(name)) }.find(text)?.let { it.range.last + 1 }

fun insertAfterName(text: String, name: String, insert: String): String {
    val end = nameEnd(text, name) ?: return text + insert
    return text.substring(0, end) + insert + activeFormatting(text.substring(0, end)) + text.substring(end)
}

fun activeFormatting(text: String): String =
    FORMATTING_CODES.findAll(text).fold("") { active, code ->
        when (code.value[1].lowercaseChar()) {
            'r' -> ""
            in 'k'..'o' -> active + code.value
            else -> code.value
        }
    }
