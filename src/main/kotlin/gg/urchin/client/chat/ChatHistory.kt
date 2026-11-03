package gg.urchin.client.chat

import gg.urchin.mixin.ChatHistoryAccessor
import net.kyori.adventure.text.Component
import net.minecraft.client.Minecraft
//? if = 1.8.9 {
/*import net.minecraft.client.gui.ChatMessage
import net.minecraft.text.Text
*///?} else {
import net.minecraft.client.multiplayer.chat.GuiMessage
import net.minecraft.network.chat.Component as Text
//?}

object ChatHistory {
    fun rewriteSince(since: Int, mentioning: Regex, rewrite: (Component) -> Component) {
        var rewrote = false
        (chat() as ChatHistoryAccessor).`urchin$messages`().replaceAll { message ->
            val content = contentOf(message)
            if (addedAt(message) < since || !mentioning.containsMatchIn(NativeText.searchableText(content))) return@replaceAll message
            val original = NativeText.toAdventure(content) ?: return@replaceAll message
            val rewritten = rewrite(original)
            if (rewritten == original) return@replaceAll message
            rewrote = true
            withContent(message, NativeText.toNative(rewritten))
        }
        if (rewrote) redraw()
    }

    //? if = 1.8.9 {
    /*fun currentTick(): Int = Minecraft.getInstance().gui.ticks

    fun chat() = Minecraft.getInstance().gui.chat

    private fun redraw() = chat().reset()

    private fun addedAt(message: ChatMessage) = message.timeOfCreation

    private fun contentOf(message: ChatMessage) = message.text

    private fun withContent(message: ChatMessage, content: Text) =
        ChatMessage(message.timeOfCreation, content, message.id)
    *///?} else {
    fun currentTick(): Int = Minecraft.getInstance().gui.hud.guiTicks

    fun chat() = Minecraft.getInstance().gui.hud.chat

    private fun redraw() = chat().rescaleChat()

    private fun addedAt(message: GuiMessage) = message.addedTime()

    private fun contentOf(message: GuiMessage) = message.content()

    private fun withContent(message: GuiMessage, content: Text) =
        GuiMessage(message.addedTime(), content, message.signature(), message.source(), message.tag())
    //?}
}
