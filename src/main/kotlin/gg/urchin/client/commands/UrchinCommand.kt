package gg.urchin.client.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.greedyString
import com.mojang.brigadier.arguments.StringArgumentType.word
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import gg.urchin.UrchinConstants
import gg.urchin.client.UrchinConfig
import org.polyfrost.oneconfig.api.commands.v1.CommandManager
import org.polyfrost.oneconfig.api.commands.v1.CommandManager.literal
import org.polyfrost.oneconfig.utils.v1.dsl.addDefaultCommand

object UrchinCommand {
    fun register() = CommandManager.register(
        UrchinConfig.addDefaultCommand(UrchinConstants.ID)
            .then(
                literal("check").then(
                    CommandManager.argument("players", greedyString())
                        .runs { TagActions.check(getString(it, "players").split(' ').filter(String::isNotBlank)) },
                ),
            )
            .then(
                literal("tag").then(
                    CommandManager.argument("player", word())
                        .runs { TagActions.openPanel(getString(it, "player")) }
                        .then(
                            CommandManager.argument("type", word())
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
                    CommandManager.argument("player", word()).then(
                        CommandManager.argument("type", word())
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

private fun <S, T : ArgumentBuilder<S, T>> T.runs(action: (CommandContext<S>) -> Unit): T =
    executes { context ->
        action(context)
        Command.SINGLE_SUCCESS
    }
