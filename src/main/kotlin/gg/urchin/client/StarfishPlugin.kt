//? if = 1.8.9 {
/*package gg.urchin.client

import gg.urchin.api.apiJson
import io.netty.buffer.Unpooled
import kotlinx.serialization.encodeToString
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket
import org.polyfrost.oneconfig.api.event.v1.eventHandler
import org.polyfrost.oneconfig.api.event.v1.events.PacketEvent
import org.polyfrost.oneconfig.api.event.v1.events.ServerJoinEvent

private const val PLUGIN_CHANNEL = "urchin:plugin"
private const val MOD_CHANNEL = "urchin:mod"
private const val UNLOAD_CHANNEL = "starfish:unload"
private const val LISTENING: Byte = 1

object StarfishPlugin {
    var listening = false
        private set
    private var reported: List<String>? = null

    fun register() {
        eventHandler { _: ServerJoinEvent -> listenTo(false) }
        eventHandler { event: PacketEvent.Receive ->
            val packet = event.getPacket<Any>() as? CustomPayloadS2CPacket ?: return@eventHandler
            when (packet.channel) {
                PLUGIN_CHANNEL -> {
                    val announced = packet.data.isReadable && packet.data.getByte(packet.data.readerIndex()) == LISTENING
                    Game.thread.execute { listenTo(announced) }
                }
                UNLOAD_CHANNEL -> Game.thread.execute { listenTo(false) }
            }
        }
        listOf(UrchinConfig::enabled, UrchinConfig::alertsFor, UrchinConfig::tabLabelsFor, UrchinConfig::nametagLabelsFor).forEach {
            UrchinConfig.addCallback(it) {
                reportChanges()
                false
            }
        }
    }

    private fun listenTo(pluginListening: Boolean) {
        listening = pluginListening
        reported = null
        reportChanges()
    }

    private fun reportChanges() {
        if (!listening) return
        val handled = handledFeatures()
        if (handled == reported) return
        reported = handled
        val payload = PacketByteBuf(Unpooled.wrappedBuffer(apiJson.encodeToString(handled).toByteArray()))
        Minecraft.getInstance().networkHandler?.sendPacket(CustomPayloadC2SPacket(MOD_CHANNEL, payload))
    }
}

private fun handledFeatures(): List<String> =
    if (!UrchinConfig.enabled) {
        emptyList()
    } else {
        listOfNotNull(
            "alerts".takeIf { UrchinConfig.alertsFor != Audience.NOBODY },
            "tab".takeIf { UrchinConfig.tabLabelsFor != Audience.NOBODY },
            "nametags".takeIf { UrchinConfig.nametagLabelsFor != Audience.NOBODY },
        )
    }
*///?} else {
package gg.urchin.client

object StarfishPlugin {
    val listening = false
}
//?}
