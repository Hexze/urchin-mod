package gg.urchin

import gg.urchin.client.UrchinClient
import net.fabricmc.api.ClientModInitializer

class UrchinEntrypoint : ClientModInitializer {
    override fun onInitializeClient() {
        UrchinClient.initialize()
    }
}
