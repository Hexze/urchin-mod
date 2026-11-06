package gg.urchin.client.alerts

import gg.urchin.api.MinecraftSession
import gg.urchin.client.Game
import gg.urchin.client.TaggedPlayer
import gg.urchin.client.settings.Settings
import gg.urchin.client.chat.Chat
import gg.urchin.client.chat.SEPARATOR
import gg.urchin.client.chat.TagText
import gg.urchin.client.chat.lines
import gg.urchin.client.chat.suggesting
import gg.urchin.client.tags.isLiveNick
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.util.UUID

object Alerts {
    fun show(tagged: List<TaggedPlayer>, partyMembers: Set<UUID>) {
        val alerted = tagged.filterNot { ignored(it, partyMembers) }
        if (alerted.isEmpty()) return
        alerted.forEach { Chat.send(message(it)) }
        if (Settings.alertSound) Game.playAlertSound()
    }
}

private fun message(tagged: TaggedPlayer): Component {
    val name = tagged.player.name
    val line = Component.textOfChildren(
        shownName(tagged)
            .hoverEvent(Component.text("Click to copy name", NamedTextColor.DARK_GRAY))
            .suggesting(name),
        TagText.gap(),
        *TagText.badges(tagged.tags, name).toTypedArray(),
    )
    if (Settings.compactAlerts) return line
    return lines(listOf(line, SEPARATOR) + TagText.stacked(tagged.tags) { TagText.details(it, name) } + SEPARATOR)
}

private fun shownName(tagged: TaggedPlayer): Component {
    val listed = Game.tabName(tagged.player.uuid)
    val real = tagged.displayName?.let(Component::text)
    val plain = Component.text(tagged.player.name, NamedTextColor.WHITE)
    if (!tagged.player.uuid.isLiveNick()) return listed ?: real ?: plain
    val nick = listed ?: plain
    if (real == null) return nick
    return Component.textOfChildren(nick, Component.text(" (", NamedTextColor.GRAY), real, Component.text(")", NamedTextColor.GRAY))
}

private fun ignored(tagged: TaggedPlayer, partyMembers: Set<UUID>): Boolean =
    (Settings.ignoreSelf && tagged.player.name.equals(MinecraftSession.username, ignoreCase = true)) ||
        (Settings.ignoreParty && tagged.player.uuid in partyMembers)
