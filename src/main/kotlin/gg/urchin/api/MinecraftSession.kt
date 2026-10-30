package gg.urchin.api

import net.minecraft.client.Minecraft

object MinecraftSession {
    val username: String
        get() = Minecraft.getInstance().user.name

    fun joinServer(serverId: String) {
        val minecraft = Minecraft.getInstance()
        val user = minecraft.user
        //? if = 1.8.9 {
        /*minecraft.sessionService.joinServer(user.profile, user.accessToken, serverId)
        *///?} else {
        minecraft.services().sessionService().joinServer(user.profileId, user.accessToken, serverId)
        //?}
    }
}
