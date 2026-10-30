package gg.urchin.client.chat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.polyfrost.oneconfig.api.platform.v1.Platform

private const val SEPARATOR_WIDTH = 37

val SEPARATOR: Component = Component.text("-".repeat(SEPARATOR_WIDTH), NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH)

object Chat {
    private val prefix = Component.textOfChildren(
        Component.text("[", NamedTextColor.DARK_GRAY),
        UrchinLogo.component,
        Component.text("] ", NamedTextColor.DARK_GRAY),
    )

    fun send(message: ComponentLike) = Platform.compatibility().displayChatMessage(Component.textOfChildren(prefix, message))

    fun info(message: String) = send(Component.text(message, NamedTextColor.GRAY))

    fun warning(message: String) = send(Component.text(message, NamedTextColor.YELLOW))

    fun error(message: String) = send(Component.text(message, NamedTextColor.RED))
}

fun bracketed(label: String, color: TextColor): Component =
    Component.textOfChildren(
        Component.text("[", NamedTextColor.DARK_GRAY),
        Component.text(label, color),
        Component.text("]", NamedTextColor.DARK_GRAY),
    )

fun lines(lines: List<ComponentLike>): Component = Component.join(JoinConfiguration.newlines(), lines)

fun Component.suggesting(command: String): Component = clickEvent(ClickEvent.suggestCommand(command))

fun Component.running(command: String): Component = clickEvent(ClickEvent.runCommand(command))
