package gg.urchin.client.tags

import gg.urchin.api.TagBatch
import gg.urchin.api.UrchinApi
import gg.urchin.client.Game
import gg.urchin.client.ListedPlayer
import gg.urchin.client.TaggedPlayer
import gg.urchin.client.UrchinConfig
import gg.urchin.client.alerts.Alerts
import gg.urchin.client.alerts.Party
import gg.urchin.client.chat.ChatHistory
import net.hypixel.data.type.GameType
import org.apache.logging.log4j.LogManager
import org.polyfrost.oneconfig.api.event.v1.eventHandler
import org.polyfrost.oneconfig.api.event.v1.events.HypixelLocationEvent
import org.polyfrost.oneconfig.api.event.v1.events.PacketEvent
import org.polyfrost.oneconfig.api.event.v1.events.WorldEvent
import org.polyfrost.oneconfig.api.hypixel.v1.HypixelUtils
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

private const val JOIN_BATCH_MS = 750L
private const val MAX_BATCH_UUIDS = 100
private val NO_PARTY: CompletableFuture<Set<UUID>> = CompletableFuture.completedFuture(emptySet())
private val log = LogManager.getLogger("Urchin/Lookups")

private data class Lookup(val player: ListedPlayer, val account: UUID)

object TagLookups {
    private class Visit {
        private val startedAt = ChatHistory.currentTick()
        private val listed = mutableListOf<ListedPlayer>()
        private val lookedUp = mutableSetOf<UUID>()
        private var located = false
        private var inGame = false
        private var inReplay = false
        private var party = NO_PARTY
        private var lookupScheduled = false

        fun enter(location: HypixelUtils.Location) {
            located = true
            inGame = location.lobbyName.isEmpty && location.mode.isPresent
            inReplay = location.serverType.orElse(null) == GameType.REPLAY
            if (inGame && !inReplay && UrchinConfig.ignoreParty) party = Party.members()
            lookUpListed()
        }

        fun join(players: List<ListedPlayer>) {
            listed += players
            if (!located || lookupScheduled) return
            lookupScheduled = true
            CompletableFuture.delayedExecutor(JOIN_BATCH_MS, TimeUnit.MILLISECONDS, Game.thread).execute {
                lookupScheduled = false
                lookUpListed()
            }
        }

        private fun lookUpListed() {
            if (this !== visit || !UrchinConfig.enabled) return
            listed
                .mapNotNull { player -> accountOf(player, inReplay)?.let { Lookup(player, it) } }
                .filter { lookedUp.add(it.player.uuid) }
                .chunked(MAX_BATCH_UUIDS)
                .forEach { batch ->
                    UrchinApi.lookupTags(batch.map { it.account.undashed() }).whenCompleteAsync({ result, error ->
                        if (error != null) log.warn("Tag lookup failed", error) else record(batch.tagged(result))
                    }, Game.thread)
                }
        }

        private fun record(tagged: List<TaggedPlayer>) {
            if (this !== visit || tagged.isEmpty()) return
            TaggedPlayers.add(tagged)
            alert(tagged)
            ChatLabels.labelEarlier(tagged.map { it.player.name }, startedAt)
        }

        private fun alert(tagged: List<TaggedPlayer>) {
            if (!inGame || !UrchinConfig.alerts) return
            party.thenAcceptAsync(
                { members -> if (this === visit) Alerts.show(tagged, members) },
                CompletableFuture.delayedExecutor(UrchinConfig.alertDelay.toLong(), TimeUnit.MILLISECONDS, Game.thread),
            )
        }
    }

    private var visit: Visit? = null

    fun register() {
        Party.register()
        eventHandler { _: WorldEvent.Load ->
            visit = Visit()
            TaggedPlayers.clear()
        }
        eventHandler { _: HypixelLocationEvent -> Game.thread.execute { visit?.enter(HypixelUtils.getLocation()) } }
        eventHandler { event: PacketEvent.Receive ->
            val players = Game.playersAddedBy(event.getPacket())
            if (players.isNotEmpty()) Game.thread.execute { visit?.join(players) }
        }
    }
}

private fun List<Lookup>.tagged(batch: TagBatch): List<TaggedPlayer> =
    mapNotNull { (player, account) ->
        val uuid = account.undashed()
        batch.players[uuid]?.takeIf { it.isNotEmpty() }?.let { TaggedPlayer(player, it, batch.displayNames[uuid]) }
    }

private fun UUID.undashed(): String = toString().replace("-", "")
