//? if = 1.8.9 {
/*package gg.urchin.client.standalone

import gg.urchin.client.chat.Chat
import gg.urchin.client.settings.Settings
import gg.urchin.client.settings.StandaloneSettings
import gg.urchin.client.tags.TagLookups
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.util.Locale

// Start-up for installs without OneConfig. The standalone mixins (urchin.standalone.mixins.json) feed
// world, packet, chat-command and tick events into the hooks below.
object Standalone {
    fun initialize() {
        Settings.store = StandaloneSettings.load()
        HypixelBridge.initialize()
        TagLookups.register()
    }

    @JvmStatic
    fun worldLoaded() = TagLookups.worldLoaded()

    @JvmStatic
    fun received(packet: Any) = TagLookups.received(packet)

    @JvmStatic
    fun tick() = SettingsScreen.openIfPending()
}

internal object Links {
    private val log = LogManager.getLogger("Urchin/Links")

    fun open(url: String) {
        val os = System.getProperty("os.name").lowercase(Locale.ROOT)
        val command = when {
            "win" in os -> arrayOf("rundll32", "url.dll,FileProtocolHandler", url)
            "mac" in os -> arrayOf("open", url)
            else -> arrayOf("xdg-open", url)
        }
        try {
            ProcessBuilder(*command).start()
        } catch (e: IOException) {
            log.warn("Couldn't open {}", url, e)
            Chat.send(Chat.infoText("Open $url in your browser."))
        }
    }
}
*///?}
