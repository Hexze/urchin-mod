package gg.urchin.client.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.greedyString
import com.mojang.brigadier.arguments.StringArgumentType.word
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import gg.urchin.UrchinConstants
import gg.urchin.api.TagType
import gg.urchin.client.Account
import gg.urchin.client.Game
import gg.urchin.client.UrchinConfig
import gg.urchin.client.tags.accountName
import net.hypixel.data.type.GameType
import org.polyfrost.oneconfig.api.commands.v1.CommandManager
import org.polyfrost.oneconfig.api.commands.v1.CommandManager.literal
import org.polyfrost.oneconfig.api.hypixel.v1.HypixelUtils
import org.polyfrost.oneconfig.utils.v1.dsl.addDefaultCommand
import java.util.concurrent.CompletableFuture

object UrchinCommand {
    fun register() = CommandManager.register(
        UrchinConfig.addDefaultCommand(UrchinConstants.ID)
            .requires { UrchinConfig.enabled }
            .then(checkPlayers())
            .then(literal("check").then(checkPlayers()))
            .then(
                literal("tag").then(
                    playerArgument()
                        .runs { TagActions.openPanel(getString(it, "player")) }
                        .then(
                            tagTypeArgument { Account.addableTypes }
                                .runs { TagActions.tag(getString(it, "player"), getString(it, "type"), "") }
                                .then(
                                    CommandManager.argument("reason", greedyString())
                                        .runs { TagActions.tag(getString(it, "player"), getString(it, "type"), getString(it, "reason")) },
                                ),
                        ),
                ),
            )
            .then(
                literal("untag").then(
                    playerArgument().then(
                        tagTypeArgument { Account.removableTypes }
                            .runs { TagActions.untag(getString(it, "player"), getString(it, "type")) },
                    ),
                ),
            )
            .then(
                literal("confirm").then(
                    CommandManager.argument("player", word()).runs { TagActions.confirm(getString(it, "player")) },
                ),
            ),
    )
}

private fun playerArgument() =
    CommandManager.argument("player", word()).suggests { _, builder -> builder.suggestMatching(onlineAccountNames()) }

private fun tagTypeArgument(types: () -> List<TagType>) =
    CommandManager.argument("type", word()).suggests { _, builder -> builder.suggestMatching(types().map(TagType::alias)) }

private fun checkPlayers() =
    CommandManager.argument("players", greedyString())
        .suggests { _, builder ->
            val lastName = builder.createOffset(builder.start + builder.remaining.lastIndexOf(' ') + 1)
            lastName.suggestMatching(onlineAccountNames())
        }
        .runs { TagActions.check(getString(it, "players").split(' ').filter(String::isNotBlank)) }

private fun onlineAccountNames(): List<String> {
    val inReplay = HypixelUtils.getLocation().serverType.orElse(null) == GameType.REPLAY
    return Game.onlinePlayers().mapNotNull { accountName(it, inReplay) }
}

private fun SuggestionsBuilder.suggestMatching(options: List<String>): CompletableFuture<Suggestions> {
    options.filter { it.lowercase().startsWith(remainingLowerCase) }.forEach(::suggest)
    return buildFuture()
}

private fun <S, T : ArgumentBuilder<S, T>> T.runs(action: (CommandContext<S>) -> Unit): T =
    executes { context ->
        action(context)
        Command.SINGLE_SUCCESS
    }
