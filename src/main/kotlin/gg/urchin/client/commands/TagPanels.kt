package gg.urchin.client.commands

import gg.urchin.api.Tag
import gg.urchin.api.TagType
import gg.urchin.client.chat.SEPARATOR
import gg.urchin.client.chat.TagText
import gg.urchin.client.chat.bracketed
import gg.urchin.client.chat.lines
import gg.urchin.client.chat.running
import gg.urchin.client.chat.suggesting
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor

private val UUID_PARTS = Regex("(.{8})(.{4})(.{4})(.{4})(.{12})")

internal object TagPanels {
    fun panel(target: Target): Component = lines(
        listOf(
            Component.empty(),
            SEPARATOR,
            Component.textOfChildren(header(target), Component.space(), *TagText.badges(target.tags, target.name).toTypedArray()),
            SEPARATOR,
        ) +
            tagList(target) +
            listOf(Component.empty(), addButtons(target), SEPARATOR),
    )

    fun addPrompt(target: Target, proposed: Tag): Component = prompt(
        Component.text("Add Tag", NamedTextColor.GREEN),
        target,
        TagText.proposedDetails(proposed),
        "Proceed?",
        confirmButton(target, NamedTextColor.GREEN, tagAction("Add the ", proposed.type, NamedTextColor.GREEN)),
    )

    fun overwritePrompt(target: Target, current: Tag, replacement: Tag): Component = prompt(
        Component.text("Overwrite Tag", NamedTextColor.GOLD),
        target,
        listOf(Component.text("Current", NamedTextColor.DARK_GRAY)) + TagText.details(current, target.name),
        "This player already has an incompatible tag. Proceed and overwrite with yours?",
        confirmButton(
            target,
            NamedTextColor.RED,
            lines(listOf(Component.text("Overwrite with:", NamedTextColor.GRAY)) + TagText.hoverDetails(replacement)),
        ),
    )

    fun removePrompt(target: Target, tag: Tag): Component = prompt(
        Component.text("Remove Tag", NamedTextColor.RED),
        target,
        TagText.details(tag, target.name),
        "Remove?",
        confirmButton(target, NamedTextColor.RED, tagAction("Remove the ", tag.type, NamedTextColor.RED)),
    )

    fun added(target: Target, tag: Tag): Component = Component.textOfChildren(
        Component.text("Tagged ", NamedTextColor.GREEN),
        target.display,
        Component.text(" as ", NamedTextColor.GREEN),
        TagText.labeled(tag, target.name),
        Component.text(". ", NamedTextColor.GREEN),
        bracketed("Undo", NamedTextColor.RED)
            .hoverEvent(Component.text("Remove this tag", NamedTextColor.GRAY))
            .running("/urchin confirm ${target.name}"),
    )

    fun overwritten(target: Target, tag: Tag): Component = Component.textOfChildren(
        Component.text("Overwrote ", NamedTextColor.GREEN),
        target.display,
        Component.text("'s tag with ", NamedTextColor.GREEN),
        TagText.labeled(tag, target.name),
        Component.text(".", NamedTextColor.GREEN),
    )

    fun removed(target: Target, tag: Tag): Component = Component.textOfChildren(
        Component.text("Removed ", NamedTextColor.RED),
        TagText.labeled(tag, target.name),
        Component.text(" from ", NamedTextColor.RED),
        target.display,
        Component.text(".", NamedTextColor.RED),
    )

    fun tagTypes(): Component = Component.textOfChildren(
        Component.text("Tag types: ", NamedTextColor.GRAY),
        Component.join(
            JoinConfiguration.separator(Component.text(", ", NamedTextColor.GRAY)),
            TagType.addable.map { type ->
                Component.textOfChildren(
                    Component.text(type.alias, type.color),
                    Component.text(" (", NamedTextColor.DARK_GRAY),
                    Component.text(type.display, NamedTextColor.GRAY),
                    Component.text(")", NamedTextColor.DARK_GRAY),
                ).hoverEvent(
                    lines(
                        listOf(
                            Component.text(type.display, type.color),
                            Component.text("/urchin tag <player> ${type.alias} <reason>", NamedTextColor.DARK_GRAY),
                        ),
                    ),
                )
            },
        ),
    )

    private fun header(target: Target): Component =
        target.display
            .hoverEvent(Component.text("${target.uuid.replace(UUID_PARTS, "$1-$2-$3-$4-$5")}\nClick to copy name", NamedTextColor.DARK_GRAY))
            .suggesting(target.name)

    private fun tagList(target: Target): List<Component> {
        if (target.tags.isEmpty()) return listOf(Component.text("None", NamedTextColor.DARK_GRAY))
        return TagText.stacked(target.tags) { tag ->
            val details = TagText.details(tag, target.name)
            listOf(Component.textOfChildren(details.first(), Component.space(), removeButton(target, tag))) + details.drop(1)
        }
    }

    private fun removeButton(target: Target, tag: Tag): Component =
        bracketed("X", NamedTextColor.RED)
            .hoverEvent(Component.text("Click to remove this tag", NamedTextColor.RED))
            .running("/urchin untag ${target.name} ${tag.type.alias}")

    private fun addButtons(target: Target): Component = Component.textOfChildren(
        Component.text("Add Tag:", NamedTextColor.GRAY),
        *TagType.addable.map { type ->
            Component.textOfChildren(
                Component.space(),
                TagText.badge(type)
                    .hoverEvent(
                        Component.textOfChildren(
                            tagAction("Add ", type, NamedTextColor.GRAY),
                            Component.text(" to ${target.name}", NamedTextColor.GRAY),
                            Component.text("\nClick, then type a reason and press enter", NamedTextColor.DARK_GRAY),
                        ),
                    )
                    .suggesting("/urchin tag ${target.name} ${type.alias} "),
            )
        }.toTypedArray(),
    )

    private fun prompt(title: Component, target: Target, body: List<Component>, question: String, confirm: Component): Component =
        lines(
            listOf(
                Component.empty(),
                title,
                SEPARATOR,
                Component.textOfChildren(Component.text("IGN - ", NamedTextColor.GRAY), target.display),
            ) +
                body +
                listOf(SEPARATOR, Component.text(question, NamedTextColor.GRAY), confirm),
        )

    private fun confirmButton(target: Target, color: NamedTextColor, hover: Component): Component =
        bracketed("Yes", color).hoverEvent(hover).running("/urchin confirm ${target.name}")

    private fun tagAction(verb: String, type: TagType, color: NamedTextColor): Component =
        Component.textOfChildren(Component.text(verb, color), Component.text(type.display, type.color), Component.text(" tag", color))
}
