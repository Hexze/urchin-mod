package gg.urchin.client.alerts

import gg.urchin.client.Game
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.packet.impl.clientbound.ClientboundPartyInfoPacket
import net.hypixel.modapi.packet.impl.serverbound.ServerboundPartyInfoPacket
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

private const val REPLY_TIMEOUT_MS = 3000L

object Party {
    var members: Set<UUID> = emptySet()
        private set

    @Volatile
    private var reply: CompletableFuture<Set<UUID>>? = null

    fun register() =
        HypixelModAPI.getInstance()
            .createHandler(ClientboundPartyInfoPacket::class.java) { reply?.complete(it.members) }
            .onError { reply?.complete(emptySet()) }

    fun refresh(): CompletableFuture<Set<UUID>> {
        val reply = CompletableFuture<Set<UUID>>().completeOnTimeout(emptySet(), REPLY_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        this.reply = reply
        if (!HypixelModAPI.getInstance().sendPacket(ServerboundPartyInfoPacket())) reply.complete(emptySet())
        return reply.thenApplyAsync({ it.also { members = it } }, Game.thread)
    }
}
