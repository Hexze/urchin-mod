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

    fun teamName(uuid: UUID): Component? {
        //? if = 1.8.9 {
        /*val info = Minecraft.getInstance().networkHandler?.getOnlinePlayer(uuid) ?: return null
        val name = LiteralText(Team.getMemberDisplayName(info.team ?: return null, info.profile.name))
        *///?} else {
        val info = Minecraft.getInstance().connection?.getPlayerInfo(uuid) ?: return null
        val name = PlayerTeam.formatNameForTeam(info.team ?: return null, Text.literal(info.profile.name()))
        //?}
        return NativeText.toAdventure(name)
    }

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
