package gg.urchin.client.chat

import gg.urchin.client.Game
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

    private val ownPrefix = plainText(prefix)

    fun send(message: ComponentLike) = display(Component.textOfChildren(prefix, message))

    fun info(message: String) = send(Component.text(message, NamedTextColor.GRAY))

    fun warning(message: String) = send(Component.text(message, NamedTextColor.YELLOW))

    fun error(message: String) = send(Component.text(message, NamedTextColor.RED))

    fun isOwn(message: Component): Boolean = plainText(message).startsWith(ownPrefix)

    private fun plainText(message: Component): String = Platform.i18n().getUnformattedText(message)

    //? if = 1.8.9 {
    /*private fun display(message: Component) = Game.thread.execute { ChatHistory.chat().addMessage(NativeText.toNative(message)) }
    *///?} else {
    private fun display(message: Component) = Platform.compatibility().displayChatMessage(message)
    //?}
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
