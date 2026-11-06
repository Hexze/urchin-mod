package gg.urchin.client.tags

import gg.urchin.client.StarfishPlugin
import gg.urchin.client.settings.Settings
import gg.urchin.client.chat.Chat
import gg.urchin.client.chat.ChatHistory
import gg.urchin.client.chat.TagText
import gg.urchin.client.chat.activeFormatting
import gg.urchin.client.chat.namePattern
import gg.urchin.client.chat.startsWithTagBadge
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
//? if = 1.8.9 {
/*import gg.urchin.client.chat.NativeText
import net.minecraft.text.Text
*///?} else {
import org.polyfrost.oneconfig.api.event.v1.eventHandler
import org.polyfrost.oneconfig.api.event.v1.events.ChatEvent
//?}

private const val SENDER_END = ':'
private const val FORMATTING_PREFIX = '§'
private const val NEUTRAL_CODES = "7fr"
private const val COLOR_CODES = "0123456789abcdef"
private val NEUTRAL_COLORS = setOf(NamedTextColor.WHITE.value(), NamedTextColor.GRAY.value())

private class Segment(val text: String, val neutral: Boolean)

private class Colon(val index: Int, val neutral: Boolean)

object ChatLabels {
    //? if = 1.8.9 {
    /*@JvmStatic
    fun labelIncoming(message: Text): Text {
        val names = taggedNamePattern() ?: return message
        if (!names.containsMatchIn(NativeText.searchableText(message))) return message
        val original = NativeText.toAdventure(message) ?: return message
        val labeled = labeled(original, names)
        return if (labeled == original) message else NativeText.toNative(labeled)
    }
    *///?} else {
    fun register() = eventHandler { event: ChatEvent.Receive ->
        val names = taggedNamePattern() ?: return@eventHandler
        event.message = labeled(event.message, names)
    }
    //?}

    fun labelEarlier(names: Collection<String>, since: Int) {
        if (!shown()) return
        val pattern = namePattern(names)
        ChatHistory.rewriteSince(since, pattern) { labeled(it, pattern) }
    }

    private fun shown(): Boolean = Settings.enabled && Settings.chatLabels

    private fun taggedNamePattern(): Regex? =
        TaggedPlayers.names.takeIf { shown() && it.isNotEmpty() }?.let(::namePattern)

    private fun labeled(message: Component, names: Regex): Component {
        if (Chat.isOwn(message)) return message
        return labelNames(message, names, skipBadged = StarfishPlugin.listening) { name -> TagText.badges(TaggedPlayers.tagsNamed(name), name) }
    }
}

internal fun labelNames(message: Component, names: Regex, skipBadged: Boolean, badges: (String) -> List<Component>): Component {
    val bodyStart = senderColonOffset(message) ?: Int.MAX_VALUE
    val content = segments(message, inheritedNeutral = true).joinToString("") { it.text }
    var offset = 0

    fun label(component: Component): Component {
        val text = (component as? TextComponent)?.content().orEmpty()
        val start = offset
        offset += text.length
        val found = names.findAll(text)
            .filter { start + it.range.first < bodyStart && !(skipBadged && startsWithTagBadge(content, start + it.range.last + 1)) }
            .toList()
        val children = component.children().map(::label)
        if (found.isEmpty()) return component.children(children)
        return (component as TextComponent).content("").children(splitAfterNames(text, found, badges) + children)
    }

    return label(message)
}

private fun senderColonOffset(message: Component): Int? {
    var offset = 0
    for (segment in segments(message, inheritedNeutral = true)) {
        firstColonIn(segment)?.let { return if (it.neutral) offset + it.index else null }
        offset += segment.text.length
    }
    return null
}

private fun segments(component: Component, inheritedNeutral: Boolean): List<Segment> {
    val neutral = component.color()?.let { it.value() in NEUTRAL_COLORS } ?: inheritedNeutral
    val text = (component as? TextComponent)?.content().orEmpty()
    return listOf(Segment(text, neutral)) + component.children().flatMap { segments(it, neutral) }
}

private fun firstColonIn(segment: Segment): Colon? {
    var neutral = segment.neutral
    segment.text.forEachIndexed { index, char ->
        when {
            segment.text.getOrNull(index - 1) == FORMATTING_PREFIX -> neutral = neutralAfter(char.lowercaseChar(), neutral)
            char == SENDER_END -> return Colon(index, neutral)
        }
    }
    return null
}

private fun neutralAfter(code: Char, neutral: Boolean): Boolean = when (code) {
    in NEUTRAL_CODES -> true
    in COLOR_CODES -> false
    else -> neutral
}

private fun splitAfterNames(text: String, names: List<MatchResult>, badges: (String) -> List<Component>): List<Component> {
    var start = 0
    val pieces = mutableListOf<Component>()
    for (name in names) {
        val end = name.range.last + 1
        pieces += textFrom(text, start, end)
        pieces += TagText.gap()
        pieces += badges(name.value)
        start = end
    }
    if (start < text.length) pieces += textFrom(text, start, text.length)
    return pieces
}

private fun textFrom(text: String, start: Int, end: Int): Component =
    Component.text(activeFormatting(text.substring(0, start)) + text.substring(start, end))
