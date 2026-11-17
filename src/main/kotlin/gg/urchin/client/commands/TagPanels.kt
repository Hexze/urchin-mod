package gg.urchin.client.commands

import gg.urchin.api.Tag
import gg.urchin.api.TagType
import gg.urchin.client.Account
import gg.urchin.client.chat.Framed
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
    fun panel(target: Target): Framed = Framed(ign(target), tagList(target), addButtons(target))

    fun addPrompt(target: Target, proposed: Tag): Framed = card(
        "Add Tag",
        target,
        TagText.proposedDetails(proposed),
        question(
            "Proceed?",
            confirmButton(target, "Yes", NamedTextColor.GREEN, tagAction("Add the ", proposed.type, NamedTextColor.GREEN)),
        ),
    )

    fun overwritePrompt(target: Target, current: Tag, replacement: Tag): Framed = card(
        "Overwrite Tag",
        target,
        listOf(Component.text("Current", NamedTextColor.DARK_GRAY)) + TagText.details(current, target.name),
        question(
            "This player already has an incompatible tag. Proceed and overwrite with yours?",
            confirmButton(
                target,
                "Yes",
                NamedTextColor.RED,
                lines(listOf(Component.text("Overwrite with:", NamedTextColor.GRAY)) + TagText.hoverDetails(replacement)),
            ),
        ),
    )

    fun removePrompt(target: Target, tag: Tag): Framed = card(
        "Remove Tag",
        target,
        TagText.details(tag, target.name),
        confirmButton(target, "Confirm Remove", NamedTextColor.RED, tagAction("Remove the ", tag.type, NamedTextColor.RED)),
    )

    fun added(target: Target, tag: Tag): Framed = card(
        "New Tag Applied",
        target,
        TagText.details(tag, target.name),
        bracketed("Undo", NamedTextColor.RED)
            .hoverEvent(Component.text("Remove this tag", NamedTextColor.GRAY))
            .running("/urchin confirm ${target.name}"),
    )

    fun overwritten(target: Target, tag: Tag): Framed = card("New Tag Applied", target, TagText.details(tag, target.name))

    fun removed(target: Target, tag: Tag): Framed = card("Tag Removed", target, TagText.removedDetails(tag, target.name))

    fun tagTypes(): Component = Component.textOfChildren(
        Component.text("Tag types: ", NamedTextColor.GRAY),
        Component.join(
            JoinConfiguration.separator(Component.text(", ", NamedTextColor.GRAY)),
            Account.addableTypes.map { type ->
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
            listOf(withRemoveButton(target, tag, details.first())) + details.drop(1)
        }
    }

    private fun withRemoveButton(target: Target, tag: Tag, title: Component): Component =
        if (Account.canRemove(tag)) Component.textOfChildren(title, Component.space(), removeButton(target, tag)) else title

    private fun removeButton(target: Target, tag: Tag): Component =
        bracketed("X", NamedTextColor.RED)
            .hoverEvent(Component.text("Click to remove this tag", NamedTextColor.RED))
            .running("/urchin untag ${target.name} ${tag.type.alias}")

    private fun addButtons(target: Target): Component? {
        val types = Account.addableTypes.takeIf { it.isNotEmpty() } ?: return null
        return Component.textOfChildren(
            Component.text("Add Tag:", NamedTextColor.GRAY),
            *types.map { type -> Component.textOfChildren(Component.space(), addButton(target, type)) }.toTypedArray(),
        )
    }

    private fun addButton(target: Target, type: TagType): Component =
        TagText.badge(type)
            .hoverEvent(
                Component.textOfChildren(
                    tagAction("Add ", type, NamedTextColor.GRAY),
                    Component.text(" to ${target.name}", NamedTextColor.GRAY),
                    Component.text("\nClick, then type a reason and press enter", NamedTextColor.DARK_GRAY),
                ),
            )
            .suggesting("/urchin tag ${target.name} ${type.alias} ")

    private fun card(title: String, target: Target, body: List<Component>, footer: Component? = null): Framed =
        Framed(Component.text(title, NamedTextColor.WHITE), listOf(ign(target)) + body, footer)

    private fun question(text: String, confirm: Component): Component =
        Component.textOfChildren(Component.text("$text ", NamedTextColor.GRAY), confirm)

    private fun ign(target: Target): Component = Component.textOfChildren(Component.text("IGN - ", NamedTextColor.GRAY), header(target))

    private fun confirmButton(target: Target, label: String, color: NamedTextColor, hover: Component): Component =
        bracketed(label, color).hoverEvent(hover).running("/urchin confirm ${target.name}")

    private fun tagAction(verb: String, type: TagType, color: NamedTextColor): Component =
        Component.textOfChildren(Component.text(verb, color), Component.text(type.display, type.color), Component.text(" tag", color))
}
