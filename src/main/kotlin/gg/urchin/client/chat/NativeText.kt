package gg.urchin.client.chat

import net.kyori.adventure.text.Component
//? if = 1.8.9 {
/*import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.text.Text
import org.apache.logging.log4j.LogManager
*///?} else {
import net.kyori.adventure.platform.modcommon.MinecraftClientAudiences
import net.minecraft.network.chat.Component as Text
//?}

//? if = 1.8.9 {
/*private val log = LogManager.getLogger("Urchin/Chat")
*///?}

object NativeText {
    //? if = 1.8.9 {
    /*private val legacyJson = GsonComponentSerializer.colorDownsamplingGson()

    fun toAdventure(text: Text): Component? =
        try {
            legacyJson.deserialize(Text.Serializer.toJson(text))
        } catch (e: UnsupportedOperationException) {
            log.warn("Leaving a chat message unlabeled because its hover can't be converted", e)
            null
        }

    fun toNative(component: Component): Text = Text.Serializer.fromJson(legacyJson.serialize(component))

    fun searchableText(text: Text): String = text.formattedString
    *///?} else {
    fun toAdventure(text: Text): Component? = MinecraftClientAudiences.of().asAdventure(text)

    fun toNative(component: Component): Text = MinecraftClientAudiences.of().asNative(component)

    fun searchableText(text: Text): String = text.string
    //?}
}
