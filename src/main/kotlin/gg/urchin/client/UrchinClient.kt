package gg.urchin.client

import gg.urchin.client.alerts.Alerts
import gg.urchin.client.commands.UrchinCommand

object UrchinClient {
    fun initialize() {
        UrchinConfig.preload()
        UrchinCommand.register()
        Alerts.register()
    }
}
