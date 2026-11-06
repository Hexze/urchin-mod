package gg.urchin.client.tags

import gg.urchin.api.TagType
import gg.urchin.client.Audience
import gg.urchin.client.StarfishPlugin
import gg.urchin.client.UrchinConfig
import gg.urchin.client.chat.containsTagBadge
import net.kyori.adventure.text.format.NamedTextColor
import java.util.UUID
//? if = 1.8.9 {
/*import gg.urchin.client.chat.insertAfterName
import net.minecraft.client.network.PlayerInfo
import net.minecraft.entity.living.player.PlayerEntity as Player
import net.minecraft.text.Formatting
import net.minecraft.text.LiteralText
import net.minecraft.text.Style
import net.minecraft.text.Text
*///?} else {
import gg.urchin.client.chat.activeFormatting
import gg.urchin.client.chat.nameEnd
import net.minecraft.ChatFormatting as Formatting
import net.minecraft.client.multiplayer.PlayerInfo
import net.minecraft.network.chat.Component as Text
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.world.entity.player.Player
import java.util.Optional
//?}

object TagLabels {
    //? if = 1.8.9 {
    /*@JvmStatic
    fun tabName(player: PlayerInfo, name: String): String =
        labelFor(player.profile.id, UrchinConfig.tabLabelsFor)
            ?.takeUnless { shownByStarfish(name) }
            ?.let { insertAfterName(name, player.profile.name, it.formattedString) } ?: name

    @JvmStatic
    fun nametag(player: Player, name: Text): Text =
        labelFor(player.uuid, UrchinConfig.nametagLabelsFor)
            ?.let { LiteralText(insertAfterName(name.formattedString, player.gameProfile.name, it.formattedString)) } ?: name

    private fun colored(text: String, color: Formatting): Text = LiteralText(text).setStyle(Style().setColor(color))
    *///?} else {
    @JvmStatic
    fun tabName(player: PlayerInfo, name: Text): Text =
        labelFor(player.profile.id(), UrchinConfig.tabLabelsFor)
            ?.takeUnless { shownByStarfish(name.string) }
            ?.let { insertAfterName(name, player.profile.name(), it) } ?: name

    @JvmStatic
    fun nametag(player: Player, name: Text): Text =
        labelFor(player.uuid, UrchinConfig.nametagLabelsFor)?.let { insertAfterName(name, player.gameProfile.name(), it) } ?: name

    private fun colored(text: String, color: Formatting): MutableComponent = Text.literal(text).withStyle(color)

    private fun insertAfterName(text: Text, playerName: String, label: Text): Text {
        val labeled = Text.empty()
        var inserted = false
        text.visit(FormattedText.StyledContentConsumer<Unit> { style, content ->
            val end = if (inserted) null else nameEnd(content, playerName)
            if (end == null) {
                labeled.append(Text.literal(content).withStyle(style))
            } else {
                labeled.append(Text.literal(content.substring(0, end)).withStyle(style))
                    .append(label)
                    .append(Text.literal(activeFormatting(content.substring(0, end)) + content.substring(end)).withStyle(style))
                inserted = true
            }
            Optional.empty()
        }, Style.EMPTY)
        return if (inserted) labeled else text.copy().append(label)
    }
    //?}

    private fun shownByStarfish(name: String): Boolean = StarfishPlugin.listening && containsTagBadge(name)

    private fun labelFor(player: UUID, audience: Audience): Text? =
        TaggedPlayers.topTagFor(player, audience)?.takeIf { UrchinConfig.enabled }?.let(::label)

    private fun label(type: TagType): Text =
        colored(" [", Formatting.DARK_GRAY)
            .append(colored(type.icon, formatting(type.color)))
            .append(colored("]", Formatting.DARK_GRAY))

    private fun formatting(color: NamedTextColor): Formatting =
        Formatting.valueOf(NamedTextColor.NAMES.keyOrThrow(color).uppercase())
}
