package gg.urchin.client

import gg.urchin.client.commands.UrchinCommand
import gg.urchin.client.settings.Settings
import gg.urchin.client.tags.Location
import gg.urchin.client.tags.TagLookups
import org.polyfrost.oneconfig.api.event.v1.eventHandler
import org.polyfrost.oneconfig.api.event.v1.events.HypixelLocationEvent
import org.polyfrost.oneconfig.api.event.v1.events.PacketEvent
import org.polyfrost.oneconfig.api.event.v1.events.WorldEvent
import org.polyfrost.oneconfig.api.hypixel.v1.HypixelUtils
//? if > 1.8.9
import gg.urchin.client.tags.ChatLabels

/**
 * Everything that touches OneConfig. Only loaded when OneConfig is installed (OneClient), so the
 * standalone build never resolves these classes.
 */
internal object OneConfigBridge {
    fun initialize() {
        Settings.store = OneConfigSettings
        UrchinConfig.preload()
        UrchinCommand.register()
        TagLookups.register()
        eventHandler { _: WorldEvent.Load -> TagLookups.worldLoaded() }
        eventHandler { _: HypixelLocationEvent ->
            val location = HypixelUtils.getLocation()
            TagLookups.located(Location.of(location.serverType, location.lobbyName, location.mode))
        }
        eventHandler { event: PacketEvent.Receive -> TagLookups.received(event.getPacket()) }
        //? if > 1.8.9
        ChatLabels.register()
        //? if = 1.8.9
        //StarfishPlugin.register()
    }
}
