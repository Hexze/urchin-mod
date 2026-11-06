package gg.urchin.client.commands

import gg.urchin.client.chat.Chat
import gg.urchin.client.settings.Settings
import gg.urchin.client.tags.TagLookups

// `/urchin debug`: one line of state in chat, for working out why tags are not showing.
internal object Debug {
    fun report() {
        Chat.send(Chat.infoText("Urchin: ${TagLookups.describe()}"))
        Chat.send(Chat.infoText(
            "Settings: enabled=${Settings.enabled} tab=${Settings.tabLabels} nametags=${Settings.nametagLabels} " +
                "chat=${Settings.chatLabels} icons=${Settings.tagIcons} alerts=${Settings.alerts}",
        ))
    }
}
