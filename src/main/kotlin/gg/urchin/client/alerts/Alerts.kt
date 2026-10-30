package gg.urchin.client.alerts

import gg.urchin.api.MinecraftSession
import gg.urchin.api.Tag
import gg.urchin.api.UrchinApi
import gg.urchin.client.Game
import gg.urchin.client.ListedPlayer
import gg.urchin.client.UrchinConfig
import gg.urchin.client.chat.Chat
import gg.urchin.client.chat.SEPARATOR
import gg.urchin.client.chat.TagText
import gg.urchin.client.chat.lines
import gg.urchin.client.chat.suggesting
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
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
private const val PLACEHOLDER_UUID_VERSION = 2
private val log = LogManager.getLogger("Urchin/Alerts")

private typealias TaggedPlayer = Pair<ListedPlayer, List<Tag>>

object Alerts {
    private var visit = Visit()

    fun register() {
        Party.register()
        eventHandler { _: WorldEvent.Load -> visit = Visit() }
        eventHandler { _: HypixelLocationEvent -> Game.thread.execute { visit.enter(HypixelUtils.getLocation()) } }
        eventHandler { event: PacketEvent.Receive ->
            val players = Game.playersAddedBy(event.getPacket())
            if (players.isNotEmpty()) Game.thread.execute { visit.join(players) }
        }
    }

    private class Visit {
        private val listed = mutableListOf<ListedPlayer>()
        private val lookedUp = mutableSetOf<UUID>()
        private var inGame = false
        private var lookupScheduled = false
        private var party: CompletableFuture<Set<UUID>> = CompletableFuture.completedFuture(emptySet())

        fun enter(location: HypixelUtils.Location) {
            inGame = location.lobbyName.isEmpty && location.mode.isPresent
            if (!inGame || !UrchinConfig.enabled) return
            if (UrchinConfig.ignoreParty) party = Party.members()
            lookUpListed()
        }

        fun join(players: List<ListedPlayer>) {
            listed += players
            if (!inGame || !UrchinConfig.enabled || lookupScheduled) return
            lookupScheduled = true
            CompletableFuture.delayedExecutor(JOIN_BATCH_MS, TimeUnit.MILLISECONDS, Game.thread).execute {
                lookupScheduled = false
                if (this === visit) lookUpListed()
            }
        }

        private fun lookUpListed() {
            listed
                .filter { it.uuid.version() != PLACEHOLDER_UUID_VERSION && lookedUp.add(it.uuid) }
                .chunked(MAX_BATCH_UUIDS)
                .forEach { batch ->
                    UrchinApi.lookupTags(batch.map { it.uuid.undashed() }).whenCompleteAsync({ tags, error ->
                        if (error != null) {
                            log.warn("Tag lookup failed", error)
                            lookedUp -= batch.map { it.uuid }
                        } else {
                            alert(batch.tagged(tags))
                        }
                    }, Game.thread)
                }
        }

        private fun alert(tagged: List<TaggedPlayer>) {
            if (tagged.isEmpty()) return
            party.thenAcceptAsync(
                { members -> show(tagged.filterNot { (player, _) -> ignored(player, members) }) },
                CompletableFuture.delayedExecutor(UrchinConfig.alertDelay.toLong(), TimeUnit.MILLISECONDS, Game.thread),
            )
        }

        private fun show(tagged: List<TaggedPlayer>) {
            if (this !== visit || tagged.isEmpty()) return
            tagged.forEach { (player, tags) -> Chat.send(message(player.name, tags)) }
            if (UrchinConfig.alertSound) Game.playAlertSound()
        }
    }
}

private fun message(name: String, tags: List<Tag>): Component {
    val line = Component.textOfChildren(
        Component.text(name, NamedTextColor.WHITE)
            .hoverEvent(Component.text("Click to copy name", NamedTextColor.DARK_GRAY))
            .suggesting(name),
        Component.space(),
        *TagText.badges(tags, name).toTypedArray(),
    )
    if (UrchinConfig.compactAlerts) return line
    return lines(listOf(line, SEPARATOR) + TagText.stacked(tags) { TagText.details(it, name) } + SEPARATOR)
}

private fun ignored(player: ListedPlayer, partyMembers: Set<UUID>): Boolean =
    (UrchinConfig.ignoreSelf && player.name.equals(MinecraftSession.username, ignoreCase = true)) ||
        (UrchinConfig.ignoreParty && player.uuid in partyMembers)

private fun List<ListedPlayer>.tagged(tags: Map<String, List<Tag>>): List<TaggedPlayer> =
    mapNotNull { player -> tags[player.uuid.undashed()]?.takeIf { it.isNotEmpty() }?.let { player to it } }

private fun UUID.undashed(): String = toString().replace("-", "")
