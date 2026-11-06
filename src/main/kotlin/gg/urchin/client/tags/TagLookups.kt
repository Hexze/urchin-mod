package gg.urchin.client.tags

import gg.urchin.api.TagBatch
import gg.urchin.api.UrchinApi
import gg.urchin.client.Game
import gg.urchin.client.ListedPlayer
import gg.urchin.client.TaggedPlayer
import gg.urchin.client.settings.Settings
import gg.urchin.client.alerts.Alerts
import gg.urchin.client.alerts.Party
import gg.urchin.client.chat.ChatHistory
import net.hypixel.data.type.GameType
import net.hypixel.data.type.ServerType
import org.apache.logging.log4j.LogManager
import java.util.Optional
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

private const val JOIN_BATCH_MS = 750L
private const val LOCATION_GRACE_MS = 5000L
private const val MAX_BATCH_UUIDS = 100
private val NO_PARTY: CompletableFuture<Set<UUID>> = CompletableFuture.completedFuture(emptySet())
private val log = LogManager.getLogger("Urchin/Lookups")

private data class Lookup(val player: ListedPlayer, val account: UUID)

/** Where Hypixel says the player is, from its Mod API location packet. */
class Location(val inGame: Boolean, val inReplay: Boolean) {
    companion object {
        fun of(serverType: Optional<out ServerType>, lobbyName: Optional<String>, mode: Optional<String>) =
            Location(inGame = lobbyName.isEmpty && mode.isPresent, inReplay = serverType.orElse(null) == GameType.REPLAY)
    }
}

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

        init {
            // Hypixel's location can fail to arrive (the Mod API being unavailable or blocked). Without a
            // fallback nothing would ever be looked up, so after a grace period tags are fetched anyway.
            // Alerts still need a location, so this only labels players.
            CompletableFuture.delayedExecutor(LOCATION_GRACE_MS, TimeUnit.MILLISECONDS, Game.thread).execute {
                if (this === visit && !located && Game.onHypixel()) {
                    log.warn("No Hypixel location after {} s; looking players up without it (alerts need a location)", LOCATION_GRACE_MS / 1000)
                    located = true
                    lookUpListed()
                }
            }
        }

        fun describe(): String =
            "located=$located inGame=$inGame inReplay=$inReplay listed=${listed.size} lookedUp=${lookedUp.size}"

        fun enter(location: Location) {
            log.info("Entering a {} (replay={})", if (location.inGame) "game" else "lobby", location.inReplay)
            located = true
            inGame = location.inGame
            inReplay = location.inReplay
            if (inGame && !inReplay && Settings.ignoreParty) party = Party.members()
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
            if (this !== visit || !Settings.enabled) return
            listed
                .mapNotNull { player -> accountOf(player, inReplay)?.let { Lookup(player, it) } }
                .filter { lookedUp.add(it.player.uuid) }
                .chunked(MAX_BATCH_UUIDS)
                .forEach { batch ->
                    log.info("Looking up {} players", batch.size)
                    UrchinApi.lookupTags(batch.map { it.account.undashed() }).whenCompleteAsync({ result, error ->
                        if (error != null) {
                            log.warn("Tag lookup failed", error)
                        } else {
                            val tagged = batch.tagged(result)
                            log.info("{} of {} players have tags", tagged.size, batch.size)
                            record(tagged)
                        }
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
            if (!inGame || !Settings.alerts) return
            party.thenAcceptAsync(
                { members -> if (this === visit) Alerts.show(tagged, members) },
                CompletableFuture.delayedExecutor(Settings.alertDelay.toLong(), TimeUnit.MILLISECONDS, Game.thread),
            )
        }
    }

    private var visit: Visit? = null

    fun register() {
        Party.register()
    }

    /** A new world was loaded (joining a server or switching instances on Hypixel). Call on the client thread. */
    fun worldLoaded() {
        visit = Visit()
        TaggedPlayers.clear()
    }

    /** Hypixel sent a new location. Safe to call from any thread. */
    fun located(location: Location) = Game.thread.execute { visit?.enter(location) }

    /** What the lookups know right now, for `/urchin debug`. */
    fun describe(): String =
        "${visit?.describe() ?: "no world yet"} tagged=${TaggedPlayers.names.size} onHypixel=${Game.onHypixel()}"

    /** Any clientbound packet; only player list additions are used. Safe to call from any thread. */
    fun received(packet: Any) {
        val players = Game.playersAddedBy(packet)
        if (players.isNotEmpty()) Game.thread.execute { visit?.join(players) }
    }
}

private fun List<Lookup>.tagged(batch: TagBatch): List<TaggedPlayer> =
    mapNotNull { (player, account) ->
        val uuid = account.undashed()
        batch.players[uuid]?.takeIf { it.isNotEmpty() }?.let { TaggedPlayer(player, it, batch.displayNames[uuid]) }
    }

private fun UUID.undashed(): String = toString().replace("-", "")
