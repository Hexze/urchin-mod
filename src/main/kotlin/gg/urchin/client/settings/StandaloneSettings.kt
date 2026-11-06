package gg.urchin.client.settings

import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

private const val FILE_NAME = "urchin.json"
private val log = LogManager.getLogger("Urchin/Settings")
private val json = Json {
    prettyPrint = true
    encodeDefaults = true
    ignoreUnknownKeys = true
}

/** Settings for installs without OneConfig, saved to `config/urchin.json`. Defaults match the OneConfig page. */
@Serializable
class StandaloneSettings(
    override var enabled: Boolean = true,
    override var alerts: Boolean = true,
    override var alertSound: Boolean = true,
    override var alertDelay: Float = 0f,
    override var compactAlerts: Boolean = false,
    override var tabLabels: Boolean = true,
    override var nametagLabels: Boolean = true,
    override var chatLabels: Boolean = true,
    override var tagIcons: Boolean = false,
    override var ignoreSelf: Boolean = true,
    override var ignoreParty: Boolean = false,
) : SettingsStore {
    override fun save() {
        val target = file()
        try {
            Files.createDirectories(target.parent)
            val temp = target.resolveSibling("$FILE_NAME.tmp")
            Files.writeString(temp, json.encodeToString(this))
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (e: IOException) {
            log.warn("Couldn't save {}", target, e)
        }
    }

    companion object {
        fun load(): StandaloneSettings {
            val source = file()
            if (!Files.exists(source)) return StandaloneSettings().also { it.save() }
            return try {
                json.decodeFromString<StandaloneSettings>(Files.readString(source))
            } catch (e: IOException) {
                log.warn("Couldn't read {}, using defaults", source, e)
                StandaloneSettings()
            } catch (e: SerializationException) {
                log.warn("{} is malformed, using defaults", source, e)
                StandaloneSettings()
            } catch (e: IllegalArgumentException) {
                log.warn("{} is malformed, using defaults", source, e)
                StandaloneSettings()
            }
        }

        private fun file(): Path = FabricLoader.getInstance().configDir.resolve(FILE_NAME)
    }
}
