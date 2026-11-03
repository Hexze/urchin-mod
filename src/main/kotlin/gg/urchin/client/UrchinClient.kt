package gg.urchin.client

import gg.urchin.client.commands.UrchinCommand
import gg.urchin.client.tags.TagLookups

object UrchinClient {
    fun initialize() {
        UrchinConfig.preload()
        UrchinCommand.register()
        TagLookups.register()
    }
}
