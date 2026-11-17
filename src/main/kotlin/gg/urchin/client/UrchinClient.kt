package gg.urchin.client

import gg.urchin.client.commands.UrchinCommand
import gg.urchin.client.tags.ChatLabels
import gg.urchin.client.tags.TagLookups

object UrchinClient {
    fun initialize() {
        UrchinConfig.preload()
        Account.register()
        UrchinCommand.register()
        TagLookups.register()
        //? if > 1.8.9
        ChatLabels.register()
        //? if = 1.8.9
        /*StarfishPlugin.register()*/
    }
}
