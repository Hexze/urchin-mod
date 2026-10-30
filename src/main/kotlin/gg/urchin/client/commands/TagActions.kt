package gg.urchin.client.commands

import gg.urchin.api.ApiException
import gg.urchin.api.PlayerTags
import gg.urchin.api.Tag
import gg.urchin.api.TagType
import gg.urchin.api.UrchinApi
import gg.urchin.client.Game
import gg.urchin.client.chat.Chat
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.util.concurrent.CompletableFuture

private val FORMATTING_CODE = Regex("§.")
private val API_ERRORS = mapOf(
    "player is locked" to "This player's tags are locked",
    "insufficient permissions" to "You don't have permission to do this",
    "invalid tag type" to "Invalid tag type",
    "player already has this tag type" to "Player already has this tag type",
    "tag not found" to "Tag not found or already removed",
    "edit window has expired" to "The 30-minute edit window has passed",
    "moderator access required" to "Only moderators can do this",
)
private val STATUS_ERRORS = mapOf(
    401 to "Couldn't sign in to Urchin with your Minecraft account.",
    403 to "You don't have permission to do this.",
    429 to "Rate limited - slow down and try again.",
)

internal class Target(input: String, player: PlayerTags) {
    val uuid = player.uuid
    val tags = player.tags
    val name = player.displayName?.let(::plainName) ?: input
    val display: Component = player.displayName?.let(Component::text) ?: Component.text(name, NamedTextColor.GRAY)
}

internal sealed class PendingAction(val target: Target) {
    abstract fun perform(): CompletableFuture<Void>

    abstract fun result(): Component

    open fun undo(): PendingAction? = null
}

internal class TagAddition(target: Target, private val type: TagType, private val reason: String) : PendingAction(target) {
    override fun perform() = UrchinApi.addTag(target.uuid, type, reason)

    override fun result() = TagPanels.added(target, addedNow(type, reason))

    override fun undo() = TagRemoval(target, addedNow(type, reason))
}

internal class TagOverwrite(
    target: Target,
    private val current: TagType,
    private val replacement: TagType,
    private val reason: String,
) : PendingAction(target) {
    override fun perform() = UrchinApi.overwriteTag(target.uuid, current, replacement, reason)

    override fun result() = TagPanels.overwritten(target, addedNow(replacement, reason))
}

internal class TagRemoval(target: Target, private val tag: Tag) : PendingAction(target) {
    override fun perform() = UrchinApi.removeTag(target.uuid, tag.type)

    override fun result() = TagPanels.removed(target, tag)
}

object TagActions {
    private val pending = mutableMapOf<String, PendingAction>()

    fun check(names: List<String>) = names.forEach(::openPanel)

    fun openPanel(name: String) {
        Chat.info("Looking up $name...")
        lookUp(name) { Chat.send(TagPanels.panel(it)) }
    }

    fun tag(name: String, input: String, reason: String) {
        val type = TagType.byInput(input)
        when {
            type == TagType.CONFIRMED_CHEATER -> Chat.error("Confirmed Cheater tags can only be applied through the review system.")
            type == TagType.REPLAYS_NEEDED -> Chat.error("Replays Needed tags must be added through the Urchin bot (/watch).")
            type == null || !type.addable -> unknownType(input)
            reason.isBlank() -> Chat.warning("Add a reason: /urchin tag $name ${type.alias} <reason>")
            else -> lookUp(name) { propose(it, type, reason.trim()) }
        }
    }

    fun untag(name: String, input: String) {
        val type = TagType.byInput(input) ?: return unknownType(input)
        lookUp(name) { target ->
            val tag = target.tags.firstOrNull { it.type == type }
            if (tag == null) {
                Chat.error("${target.name} has no ${type.display} tag.")
            } else {
                offer(TagRemoval(target, tag), TagPanels.removePrompt(target, tag))
            }
        }
    }

    fun confirm(name: String) {
        val key = name.lowercase()
        val action = pending.remove(key) ?: return Chat.error("That action expired. Reopen the panel with /urchin tag $name.")
        action.perform().whenCompleteAsync({ _, error ->
            if (error != null) {
                Chat.error(describe(error))
            } else {
                action.undo()?.let { pending[key] = it }
                Chat.send(action.result())
            }
        }, Game.thread)
    }

    private fun propose(target: Target, type: TagType, reason: String) {
        val conflict = target.tags.firstOrNull { it.type.priority == type.priority }
        val proposed = Tag(type.id, reason)
        if (conflict == null) {
            offer(TagAddition(target, type, reason), TagPanels.addPrompt(target, proposed))
        } else {
            offer(TagOverwrite(target, conflict.type, type, reason), TagPanels.overwritePrompt(target, conflict, proposed))
        }
    }

    private fun offer(action: PendingAction, prompt: Component) {
        pending[action.target.name.lowercase()] = action
        Chat.send(prompt)
    }

    private fun unknownType(input: String) {
        Chat.error("Unknown tag type \"$input\".")
        Chat.send(TagPanels.tagTypes())
    }

    private fun lookUp(name: String, then: (Target) -> Unit) {
        UrchinApi.lookupPlayer(name).whenCompleteAsync({ player, error ->
            when {
                error != null -> Chat.error(describe(error))
                player == null -> Chat.error("Player not found: $name")
                else -> then(Target(name, player))
            }
        }, Game.thread)
    }
}

internal fun plainName(displayName: String): String = displayName.replace(FORMATTING_CODE, "").substringAfterLast(' ')

internal fun describe(error: Throwable): String {
    val causes = generateSequence(error, Throwable::cause).toList()
    val failure = causes.filterIsInstance<ApiException>().firstOrNull()
        ?: return "Couldn't reach Urchin: ${causes.last().message}"
    val raw = failure.error ?: return STATUS_ERRORS[failure.status] ?: "Request failed (${failure.status})."
    return API_ERRORS[raw] ?: when {
        raw.startsWith("conflicts") -> "Conflicts with an existing tag"
        "tagging is disabled" in raw -> "Your tagging ability has been disabled"
        else -> raw
    }
}

private fun addedNow(type: TagType, reason: String) = Tag(type.id, reason, addedOn = System.currentTimeMillis())
