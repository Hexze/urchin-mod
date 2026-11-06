package gg.urchin.client

import net.fabricmc.loader.api.FabricLoader
//? if = 1.8.9
//import gg.urchin.client.standalone.Standalone

/** OneConfig's API mod id. Present on OneClient; absent on a plain Ornithe install. */
const val ONECONFIG_MOD_ID = "oneconfigv1"

object UrchinClient {
    /** True when OneConfig is installed: settings live on its page and it provides events and commands. */
    @JvmStatic
    val usesOneConfig: Boolean by lazy { FabricLoader.getInstance().isModLoaded(ONECONFIG_MOD_ID) }

    fun initialize() {
        //? if = 1.8.9 {
        /*if (!usesOneConfig) return Standalone.initialize()
        *///?}
        OneConfigBridge.initialize()
    }
}
