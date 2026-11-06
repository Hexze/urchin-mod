package gg.urchin.client.chat

import gg.urchin.UrchinConstants
import gg.urchin.api.Tag
import gg.urchin.api.TagIcon
import gg.urchin.api.TagType
import gg.urchin.client.settings.Settings
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

private const val MILLIS_PER_SECOND = 1000L
private val ICON_FONT = Key.key(UrchinConstants.ID, "icons")
private val TIME_UNITS = listOf(
    "year" to 31_536_000L,
    "month" to 2_592_000L,
    "day" to 86_400L,
    "hour" to 3_600L,
    "minute" to 60L,
)

object TagText {
    /** What goes between a name and its badges: a normal space, or a thin gap when badges are icons. */
    fun gap(): Component =
        if (Settings.iconsOn) Component.text(TagIcon.GAP.toString()).font(ICON_FONT) else Component.space()

    /** `[CCC]`, or the tag's Material Design icon when "MDI Icons" is on. */
    fun badge(type: TagType): Component {
        val icon = type.mdi
        if (Settings.tagIcons && icon != null) return Component.text(icon.glyph.toString(), type.mdiColor).font(ICON_FONT)
        return bracketed(type.icon, type.color)
    }

    fun badges(tags: List<Tag>, name: String): List<Component> {
        val hover = hover(tags)
        return tags.map { badge(it.type).withInfo(hover, name, it) }
    }

    fun labeled(tag: Tag, name: String): Component = title(tag.type).withInfo(hover(listOf(tag)), name, tag)

    fun details(tag: Tag, name: String): List<Component> = listOf(labeled(tag, name)) + facts(tag)

    fun proposedDetails(tag: Tag): List<Component> = listOf(title(tag.type).hoverEvent(lines(card(listOf(tag))))) + facts(tag)

    fun hoverDetails(tag: Tag): List<Component> = listOf(title(tag.type)) + facts(tag)

    fun stacked(tags: List<Tag>, render: (Tag) -> List<Component>): List<Component> =
        tags.flatMapIndexed { index, tag -> if (index == 0) render(tag) else listOf(Component.empty()) + render(tag) }

    private fun title(type: TagType): Component =
        Component.textOfChildren(badge(type), Component.text(" ${type.display}", NamedTextColor.WHITE))

    private fun facts(tag: Tag): List<Component> = listOfNotNull(
        Component.textOfChildren(
            Component.text("> ", NamedTextColor.DARK_GRAY),
            Component.text(tag.reason ?: "No reason", NamedTextColor.GRAY),
        ),
        tag.addedOn?.let { addedOn ->
            Component.textOfChildren(
                Component.text("- Added", NamedTextColor.DARK_GRAY),
                tag.addedBy?.let { Component.text(" by @$it", NamedTextColor.GRAY) } ?: Component.empty(),
                Component.text(" ${timeAgo(addedOn)}", NamedTextColor.DARK_GRAY),
            )
        },
        tag.expiresAt?.let { Component.text("- ${expiry(it)}", NamedTextColor.DARK_GRAY) },
    )

    private fun hover(tags: List<Tag>): Component =
        lines(card(tags) + Component.text("Click to paste info in chat", NamedTextColor.DARK_GRAY))

    private fun card(tags: List<Tag>): List<Component> =
        listOf(Component.textOfChildren(UrchinLogo.component, Component.text(" Urchin Blacklist Tags", NamedTextColor.WHITE)), SEPARATOR) +
            stacked(tags, ::hoverDetails) +
            SEPARATOR

    private fun Component.withInfo(hover: Component, name: String, tag: Tag): Component =
        hoverEvent(hover).suggesting(pasteLine(name, tag))

    private fun pasteLine(name: String, tag: Tag): String =
        "⚠ $name [${tag.type.display}] | \"${tag.reason.orEmpty()}\" - Added ${tag.addedOn?.let(::timeAgo) ?: "unknown"}"
}

internal fun timeAgo(millis: Long, now: Long = System.currentTimeMillis()): String =
    duration((now - millis) / MILLIS_PER_SECOND)?.let { "$it ago" } ?: "just now"

internal fun expiry(millis: Long, now: Long = System.currentTimeMillis()): String {
    val seconds = (millis - now) / MILLIS_PER_SECOND
    if (seconds <= 0) return "Expired"
    return "Expires in ${duration(seconds) ?: "under a minute"}"
}

private fun duration(seconds: Long): String? =
    TIME_UNITS.firstNotNullOfOrNull { (unit, length) ->
        (seconds / length).takeIf { it >= 1 }?.let { count -> "$count $unit${if (count == 1L) "" else "s"}" }
    }
