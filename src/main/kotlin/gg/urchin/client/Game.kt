package gg.urchin.client

import com.mojang.authlib.GameProfile
import gg.urchin.client.chat.NativeText
import net.kyori.adventure.text.Component
import net.minecraft.client.Minecraft
import java.util.UUID
import java.util.concurrent.Executor
//? if = 1.8.9 {
/*import net.minecraft.client.sound.instance.SimpleSoundInstance
import net.minecraft.network.packet.s2c.play.PlayerInfoS2CPacket
import net.minecraft.resource.Identifier
import net.minecraft.scoreboard.team.Team
import net.minecraft.text.LiteralText
*///?} else {
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component as Text
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.scores.PlayerTeam
//?}

private const val TEXTURES = "textures"

object Game {
    val thread = Executor { task ->
        //? if = 1.8.9 {
        /*Minecraft.getInstance().executeTask(task)
        *///?} else {
        Minecraft.getInstance().execute(task)
        //?}
    }

    fun playAlertSound() {
        //? if = 1.8.9 {
        /*Minecraft.getInstance().soundManager.play(SimpleSoundInstance.of(Identifier("note.pling"), 1f))
        *///?} else {
        Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, 1f))
        //?}
    }

    fun tabName(uuid: UUID): Component? {
        //? if = 1.8.9 {
        /*val info = Minecraft.getInstance().networkHandler?.getOnlinePlayer(uuid) ?: return null
        val name = info.displayName ?: LiteralText(Team.getMemberDisplayName(info.team, info.profile.name))
        *///?} else {
        val info = Minecraft.getInstance().connection?.getPlayerInfo(uuid) ?: return null
        val name = info.tabListDisplayName ?: PlayerTeam.formatNameForTeam(info.team, Text.literal(info.profile.name()))
        //?}
        return NativeText.toAdventure(name)
    }

    // Whether the server being played on is Hypixel, by its address. Only the 1.8.9 build needs it: it is the
    // fallback for when Hypixel's location packet does not arrive.
    fun onHypixel(): Boolean =
        //? if = 1.8.9 {
        /*Minecraft.getInstance().currentServerEntry?.ip?.substringBefore(':')?.lowercase()
            ?.let { it == "hypixel.net" || it.endsWith(".hypixel.net") } == true
        *///?} else {
        false
        //?}

    fun playersAddedBy(packet: Any): List<ListedPlayer> =
        //? if = 1.8.9 {
        /*(packet as? PlayerInfoS2CPacket)
            ?.takeIf { it.action == PlayerInfoS2CPacket.Action.ADD_PLAYER }
            ?.entries.orEmpty()
            .map { ListedPlayer(it.profile.id, it.profile.name, texturesOf(it.profile)) }
        *///?} else {
        (packet as? ClientboundPlayerInfoUpdatePacket)?.newEntries().orEmpty()
            .mapNotNull { entry -> entry.profile()?.let { ListedPlayer(it.id(), it.name(), texturesOf(it)) } }
        //?}

    private fun texturesOf(profile: GameProfile): String? =
        //? if = 1.8.9 {
        /*profile.properties.get(TEXTURES).firstOrNull()?.value
        *///?} else {
        profile.properties().get(TEXTURES).firstOrNull()?.value()
        //?}
}
