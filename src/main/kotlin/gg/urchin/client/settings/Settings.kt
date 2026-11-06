package gg.urchin.client.settings

/**
 * Every setting the mod reads. OneClient/OneConfig installs back it with [gg.urchin.client.UrchinConfig];
 * standalone installs back it with [StandaloneSettings], edited through the /urchin screen.
 */
interface SettingsStore {
    var enabled: Boolean
    var alerts: Boolean
    var alertSound: Boolean
    var alertDelay: Float
    var compactAlerts: Boolean
    var tabLabels: Boolean
    var nametagLabels: Boolean
    var chatLabels: Boolean
    var tagIcons: Boolean
    var ignoreSelf: Boolean
    var ignoreParty: Boolean

    fun save() {}
}

object Settings {
    lateinit var store: SettingsStore

    val enabled: Boolean get() = store.enabled
    val alerts: Boolean get() = store.alerts
    val alertSound: Boolean get() = store.alertSound
    val alertDelay: Float get() = store.alertDelay
    val compactAlerts: Boolean get() = store.compactAlerts
    val tabLabels: Boolean get() = store.tabLabels
    val nametagLabels: Boolean get() = store.nametagLabels
    val chatLabels: Boolean get() = store.chatLabels
    val tagIcons: Boolean get() = store.tagIcons

    // Safe to read before a store exists (unit tests, early start-up).
    val iconsOn: Boolean get() = ::store.isInitialized && store.tagIcons
    val ignoreSelf: Boolean get() = store.ignoreSelf
    val ignoreParty: Boolean get() = store.ignoreParty
}
