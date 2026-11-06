//? if = 1.8.9 {
/*package gg.urchin.client.standalone

import gg.urchin.UrchinConstants
import gg.urchin.client.chat.Chat
import gg.urchin.client.commands.Debug
import gg.urchin.client.commands.TagActions
import gg.urchin.client.settings.Settings

// Client-side /urchin for installs without OneConfig's command system. Messages starting with it are handled
// here and never sent to the server.
object StandaloneCommands {
    private const val URCHIN = "/" + UrchinConstants.ID

    @JvmStatic
    fun handle(message: String): Boolean {
        val words = message.trim().split(' ').filter(String::isNotEmpty)
        if (words.firstOrNull()?.lowercase() != URCHIN) return false
        // Turned off, only the settings stay reachable, since that is where it is turned back on.
        if (words.size > 1 && !Settings.enabled) return false
        urchin(words.drop(1))
        return true
    }

    private fun urchin(args: List<String>) {
        val sub = args.firstOrNull()?.lowercase() ?: return SettingsScreen.openNextTick()
        val rest = args.drop(1)
        when (sub) {
            "check" -> if (rest.isEmpty()) usage("check <players>") else TagActions.check(rest)
            "tag" -> when (rest.size) {
                0 -> usage("tag <player> [type] [reason]")
                1 -> TagActions.openPanel(rest[0])
                else -> TagActions.tag(rest[0], rest[1], rest.drop(2).joinToString(" "))
            }
            "untag" -> if (rest.size < 2) usage("untag <player> <type>") else TagActions.untag(rest[0], rest[1])
            "confirm" -> if (rest.isEmpty()) usage("confirm <player>") else TagActions.confirm(rest[0])
            "debug" -> {
                Debug.report()
                Chat.send(Chat.infoText("Hypixel Mod API: hello=${HypixelBridge.helloReceived} locations=${HypixelBridge.locationsReceived}"))
            }
            "help" -> help()
            else -> TagActions.check(args)
        }
    }

    private fun usage(syntax: String) = Chat.send(Chat.warningText("Usage: $URCHIN $syntax"))

    private fun help() {
        Chat.send(Chat.infoText("$URCHIN - open the settings"))
        Chat.send(Chat.infoText("$URCHIN <players> - show tags"))
        Chat.send(Chat.infoText("$URCHIN tag <player> <type> <reason> - add a tag"))
        Chat.send(Chat.infoText("$URCHIN untag <player> <type> - remove a tag"))
        Chat.send(Chat.infoText("$URCHIN debug - show what Urchin knows about this server"))
    }
}
*///?}
