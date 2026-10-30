package gg.urchin.client.alerts

import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.packet.impl.clientbound.ClientboundPartyInfoPacket
import net.hypixel.modapi.packet.impl.serverbound.ServerboundPartyInfoPacket
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

private const val REPLY_TIMEOUT_MS = 3000L

object Party {
    @Volatile
    private var reply: CompletableFuture<Set<UUID>>? = null

    fun register() =
        HypixelModAPI.getInstance()
            .createHandler(ClientboundPartyInfoPacket::class.java) { reply?.complete(it.members) }
            .onError { reply?.complete(emptySet()) }

    fun members(): CompletableFuture<Set<UUID>> {
        val members = CompletableFuture<Set<UUID>>().completeOnTimeout(emptySet(), REPLY_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        reply = members
        if (!HypixelModAPI.getInstance().sendPacket(ServerboundPartyInfoPacket())) members.complete(emptySet())
        return members
    }
}
