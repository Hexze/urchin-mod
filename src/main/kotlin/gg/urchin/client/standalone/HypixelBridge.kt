//? if = 1.8.9 {
/*package gg.urchin.client.standalone

import gg.urchin.client.tags.Location
import gg.urchin.client.tags.TagLookups
import io.netty.buffer.Unpooled
import net.fabricmc.loader.api.FabricLoader
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.HypixelModAPIImplementation
import net.hypixel.modapi.packet.HypixelPacket
import net.hypixel.modapi.packet.impl.clientbound.ClientboundHelloPacket
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import net.hypixel.modapi.serializer.PacketSerializer
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket
import org.apache.logging.log4j.LogManager

private const val HYPIXEL_MOD_API_ID = "hypixel-mod-api"
private val log = LogManager.getLogger("Urchin/HypixelModAPI")

// Connects Hypixel's Mod API (the bundled core library) to the 1.8.9 network handler when nothing else does.
// OneConfig, or the standalone "Hypixel Mod API" mod, normally do this; without them Urchin carries the
// plugin-channel packets itself so it still learns where the player is and who is in their party.
//
// It follows the official Ornithe implementation (the mod that ships inside OneConfig) exactly: Hypixel sends
// `hypixel:hello` unprompted right after joining, only then does the library send `hypixel:register` to
// subscribe to the location event, and no vanilla `REGISTER` plugin message is ever sent. Checked against a
// real Hypixel ReplayMod recording, which contains `hypixel:hello` and no other Mod API packet until a client
// subscribes.
object HypixelBridge : HypixelModAPIImplementation {
    @Volatile
    private var attached = false

    @Volatile
    private var onHypixel = false

    @Volatile
    var locationsReceived = 0
        private set

    val helloReceived: Boolean
        get() = onHypixel

    fun initialize() {
        val api = HypixelModAPI.getInstance()
        if (FabricLoader.getInstance().isModLoaded(HYPIXEL_MOD_API_ID)) {
            log.info("Another mod connects the Hypixel Mod API; Urchin will use its location events")
        } else {
            try {
                api.setModImplementation(this)
            } catch (e: IllegalStateException) {
                log.info("Another mod already connects the Hypixel Mod API: {}", e.message)
            }
        }
        api.subscribeToEventPacket(ClientboundLocationPacket::class.java)
        api.createHandler(ClientboundLocationPacket::class.java) { packet ->
            locationsReceived++
            log.info(
                "Hypixel location: type={} lobby={} mode={} map={}",
                packet.serverType.orElse(null), packet.lobbyName.orElse(null), packet.mode.orElse(null), packet.map.orElse(null),
            )
            TagLookups.located(Location.of(packet.serverType, packet.lobbyName, packet.mode))
        }.onError { log.warn("Hypixel location request failed: {}", it) }
    }

    // Called by the library from setModImplementation, before it registers its own hello handler, so this one
    // runs first and flips onHypixel before the library tries to register for events.
    override fun onInit() {
        attached = true
        HypixelModAPI.getInstance().createHandler(ClientboundHelloPacket::class.java) {
            onHypixel = true
            log.info("Hypixel Mod API hello received; subscribing to location events")
        }
    }

    override fun isConnectedToHypixel(): Boolean {
        if (Minecraft.getInstance().networkHandler == null) onHypixel = false
        return onHypixel
    }

    override fun sendPacket(packet: HypixelPacket): Boolean {
        if (!isConnectedToHypixel()) return false
        val handler = Minecraft.getInstance().networkHandler ?: return false
        val data = PacketByteBuf(Unpooled.buffer())
        packet.write(PacketSerializer(data))
        handler.sendPacket(CustomPayloadC2SPacket(packet.identifier, data))
        return true
    }

    // Hands a Hypixel plugin-channel payload to the Mod API. Returns true when it was consumed.
    @JvmStatic
    fun receive(channel: String, data: PacketByteBuf): Boolean {
        if (!attached) return false
        val api = HypixelModAPI.getInstance()
        if (!api.registry.isRegistered(channel)) return false
        try {
            api.handle(channel, PacketSerializer(data))
        } catch (e: RuntimeException) {
            log.warn("Couldn't read Hypixel packet {}", channel, e)
        } finally {
            if (data.refCnt() > 0) data.release()
        }
        return true
    }
}
*///?}
