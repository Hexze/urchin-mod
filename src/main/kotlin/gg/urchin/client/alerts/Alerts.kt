package gg.urchin.client.alerts

import gg.urchin.client.Game
import gg.urchin.client.TaggedPlayer
import gg.urchin.client.UrchinConfig
import gg.urchin.client.chat.Chat
import gg.urchin.client.chat.TagText
import gg.urchin.client.chat.Framed
import gg.urchin.client.chat.suggesting
import gg.urchin.client.tags.isLiveNick
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.NamedTextColor

object Alerts {
    fun show(tagged: List<TaggedPlayer>) {
        val alerted = tagged.filter { UrchinConfig.alertsFor.includes(it.account) }
        if (alerted.isEmpty()) return
        alerted.forEach { Chat.send(message(it)) }
        if (UrchinConfig.alertSound) Game.playAlertSound()
    }
}

private fun message(tagged: TaggedPlayer): ComponentLike {
    val name = tagged.player.name
    val line = Component.textOfChildren(
        shownName(tagged)
            .hoverEvent(Component.text("Click to copy name", NamedTextColor.DARK_GRAY))
            .suggesting(name),
        Component.space(),
        *TagText.badges(tagged.tags, name).toTypedArray(),
    )
    if (UrchinConfig.compactAlerts) return line
    return Framed(line, TagText.stacked(tagged.tags) { TagText.details(it, name) })
}

private fun shownName(tagged: TaggedPlayer): Component {
    val formatted = Game.teamName(tagged.player.uuid)
    val real = tagged.displayName?.let(Component::text)
    val plain = Component.text(tagged.player.name, NamedTextColor.WHITE)
    if (!tagged.player.uuid.isLiveNick()) return formatted ?: real ?: plain
    val nick = formatted ?: plain
    if (real == null) return nick
    return Component.textOfChildren(nick, Component.text(" (", NamedTextColor.GRAY), real, Component.text(")", NamedTextColor.GRAY))
}
