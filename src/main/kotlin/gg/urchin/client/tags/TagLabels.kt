package gg.urchin.client.tags

import gg.urchin.api.TagIcon
import gg.urchin.api.TagType
import gg.urchin.client.StarfishPlugin
import gg.urchin.client.chat.containsTagBadge
import gg.urchin.client.settings.Settings
import net.kyori.adventure.text.format.NamedTextColor
import java.util.UUID
//? if = 1.8.9 {
/*import gg.urchin.client.chat.insertAfterName
import gg.urchin.client.chat.labelNameOnce
import net.minecraft.client.network.PlayerInfo
import net.minecraft.entity.Entity
import net.minecraft.entity.living.player.PlayerEntity as Player
import net.minecraft.text.Formatting
import net.minecraft.text.LiteralText
import net.minecraft.text.Style
import net.minecraft.text.Text
*///?} else {
import gg.urchin.UrchinConstants
import gg.urchin.client.chat.activeFormatting
import gg.urchin.client.chat.nameEnd
import net.minecraft.ChatFormatting as Formatting
import net.minecraft.client.multiplayer.PlayerInfo
import net.minecraft.network.chat.Component as Text
import net.minecraft.network.chat.FontDescription
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Player
import java.util.Optional

private val ICON_FONT = FontDescription.Resource(Identifier.fromNamespaceAndPath(UrchinConstants.ID, "icons"))
//?}

object TagLabels {
    //? if = 1.8.9 {
    /*@JvmStatic
    fun tabName(player: PlayerInfo, name: String): String =
        labelFor(player.profile.id, player.profile.name, Settings.tabLabels && !shownByStarfish(name))
            ?.let { insertAfterName(name, player.profile.name, it.formattedString) } ?: name

    @JvmStatic
    fun nametag(player: Player, name: Text): Text =
        labelFor(player.uuid, player.gameProfile.name, Settings.nametagLabels)
            ?.let { LiteralText(insertAfterName(name.formattedString, player.gameProfile.name, it.formattedString)) } ?: name

    // For name tag text that other mods built themselves, without calling getDisplayName.
    @JvmStatic
    fun nametagText(entity: Entity, text: String): String {
        val player = entity as? Player ?: return text
        val name = player.gameProfile.name
        val label = labelFor(player.uuid, name, Settings.nametagLabels)?.formattedString ?: return text
        return labelNameOnce(text, name, label)
    }

    private fun colored(text: String, color: Formatting): Text = LiteralText(text).setStyle(Style().setColor(color))

    // Drawn as the icon by TextRendererMixin.
    private fun glyph(icon: TagIcon, color: Formatting): Text = colored(icon.glyph.toString(), color)

    // The thin gap that goes before an icon, drawn as blank space by TextRendererMixin.
    private fun gap(): Text = colored(TagIcon.GAP.toString(), Formatting.WHITE)
    *///?} else {
    @JvmStatic
    fun tabName(player: PlayerInfo, name: Text): Text =
        labelFor(player.profile.id(), player.profile.name(), Settings.tabLabels && !shownByStarfish(name.string))
            ?.let { insertAfterName(name, player.profile.name(), it) } ?: name

    @JvmStatic
    fun nametag(player: Player, name: Text): Text =
        labelFor(player.uuid, player.gameProfile.name(), Settings.nametagLabels)?.let { insertAfterName(name, player.gameProfile.name(), it) } ?: name

    private fun colored(text: String, color: Formatting): MutableComponent = Text.literal(text).withStyle(color)

    private fun gap(): MutableComponent = Text.literal(TagIcon.GAP.toString()).withStyle(Style.EMPTY.withFont(ICON_FONT))

    // The urchin:icons font maps each glyph to its icon texture.
    private fun glyph(icon: TagIcon, color: Formatting): MutableComponent =
        Text.literal(icon.glyph.toString()).withStyle(Style.EMPTY.withColor(color).withFont(ICON_FONT))

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

    // By UUID, then by name: the entry in a list can carry a different UUID than the one the player was
    // looked up under (a reconnect, a proxy), while the name still matches.
    private fun labelFor(player: UUID, name: String, enabled: Boolean): Text? =
        (TaggedPlayers.topTag(player) ?: TaggedPlayers.topTagNamed(name))?.takeIf { enabled && Settings.enabled }?.let(::label)

    private fun label(type: TagType): Text {
        val icon = type.mdi
        if (Settings.tagIcons && icon != null) return gap().append(glyph(icon, formatting(type.mdiColor)))
        return colored(" [", Formatting.DARK_GRAY)
            .append(colored(type.icon, formatting(type.color)))
            .append(colored("]", Formatting.DARK_GRAY))
    }

    private fun formatting(color: NamedTextColor): Formatting =
        Formatting.valueOf(NamedTextColor.NAMES.keyOrThrow(color).uppercase())
}
