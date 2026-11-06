package gg.urchin.client.chat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.polyfrost.oneconfig.api.platform.v1.Platform

private const val SEPARATOR_WIDTH = 36
private const val LOGO_SEPARATOR_LEAD = 1
private const val LOGO_WIDTH_IN_DASHES = 2

private val LOGO = Component.textOfChildren(
    Component.text("[", NamedTextColor.DARK_GRAY),
    UrchinLogo.component,
    Component.text("]", NamedTextColor.DARK_GRAY),
)
private val SEPARATOR = dashes(SEPARATOR_WIDTH)
private val LOGO_SEPARATOR = Component.textOfChildren(
    dashes(LOGO_SEPARATOR_LEAD),
    LOGO,
    dashes(SEPARATOR_WIDTH - LOGO_SEPARATOR_LEAD - LOGO_WIDTH_IN_DASHES),
)

class Framed(private val heading: Component, private val body: List<Component>, private val footer: Component? = null) : ComponentLike {
    override fun asComponent(): Component {
        val closing = if (footer == null) listOf(SEPARATOR) else listOf(Component.empty(), footer, SEPARATOR)
        return lines(listOf(LOGO_SEPARATOR, heading, Component.empty()) + body + closing)
    }
}

object Chat {
    private val prefix = Component.textOfChildren(LOGO, Component.space())

    private val ownStarts = listOf(prefix, LOGO_SEPARATOR).map(::plainText)

    fun send(message: ComponentLike): ChatEntry = ChatHistory.add(prefixed(message))

    fun replace(previous: ChatEntry?, message: ComponentLike): ChatEntry = ChatHistory.replace(previous, prefixed(message))

    fun infoText(message: String): Component = Component.text(message, NamedTextColor.GRAY)

    fun warningText(message: String): Component = Component.text(message, NamedTextColor.YELLOW)

    fun errorText(message: String): Component = Component.text(message, NamedTextColor.RED)

    fun isOwn(message: Component): Boolean = plainText(message).let { text -> ownStarts.any(text::startsWith) }

    private fun plainText(message: Component): String = Platform.i18n().getUnformattedText(message)

    private fun prefixed(message: ComponentLike) =
        NativeText.toNative(if (message is Framed) message.asComponent() else Component.textOfChildren(prefix, message))
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

private fun dashes(count: Int): Component = Component.text("-".repeat(count), NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH)
