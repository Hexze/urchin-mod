package gg.urchin.client.tags

import gg.urchin.client.chat.namePattern
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private val NAMES = namePattern(listOf("Sniper", "Bob"))

private fun labeled(message: Component, skipBadged: Boolean = false): Component =
    labelNames(message, NAMES, skipBadged) { listOf(Component.text("[CC]")) }

private fun labeled(text: String, skipBadged: Boolean = false): Component = labeled(Component.text(text), skipBadged)

private fun Component.pieces(): List<String> =
    listOf((this as TextComponent).content()).filter { it.isNotEmpty() } + children().flatMap { it.pieces() }

class ChatLabelsTest {
    @Test
    fun `labels every name in a message without a body`() {
        assertEquals(
            listOf("Sniper", " ", "[CC]", " was killed by Bob", " ", "[CC]", "."),
            labeled("Sniper was killed by Bob.").pieces(),
        )
    }

    @Test
    fun `labels only the sender when the colon is white, gray or uncolored`() {
        assertEquals(listOf("Bob", " ", "[CC]", "§f: Sniper is cheating"), labeled("Bob§f: Sniper is cheating").pieces())
        assertEquals(listOf("§7Bob", " ", "[CC]", "§7§r: Sniper is cheating"), labeled("§7Bob§r: Sniper is cheating").pieces())
        assertEquals(listOf("Bob", " ", "[CC]", ": Sniper is cheating"), labeled("Bob: Sniper is cheating").pieces())
        assertEquals(listOf("§7Bob", " ", "[CC]", "§7§7: Sniper is cheating"), labeled("§7Bob§7: Sniper is cheating").pieces())
    }

    @Test
    fun `treats a colored colon as a system message`() {
        assertEquals(listOf("§7Bob", " ", "[CC]", "§7 says§a: hi Sniper", " ", "[CC]"), labeled("§7Bob says§a: hi Sniper").pieces())
        assertEquals(
            listOf("Bob", " ", "[CC]", " says: hi Sniper", " ", "[CC]"),
            labeled(Component.text("Bob").append(Component.text(" says: hi Sniper", NamedTextColor.GREEN))).pieces(),
        )
    }

    @Test
    fun `reads a white colon from component colors`() {
        val message = Component.text("[MVP+] ").append(Component.text("Bob")).append(Component.text(": Sniper", NamedTextColor.WHITE))
        val result = labeled(message)
        assertEquals(listOf("Bob", " ", "[CC]"), result.children()[0].pieces())
        assertEquals(listOf(": Sniper"), result.children()[1].pieces())
    }

    @Test
    fun `keeps the color codes active after the badge`() {
        assertEquals(
            listOf("§b[MVP§c+§b] Sniper", " ", "[CC]", "§b§f: gg"),
            labeled("§b[MVP§c+§b] Sniper§f: gg").pieces(),
        )
    }

    @Test
    fun `leaves names that already carry a badge alone when asked to`() {
        assertEquals(listOf("§7Sniper §8[§7C§8] §7FKDR"), labeled("§7Sniper §8[§7C§8] §7FKDR", skipBadged = true).pieces())
        assertEquals(
            listOf("Bob", " ", "[CC]", " and Sniper ", "[C]"),
            labeled(Component.text("Bob and Sniper ").append(Component.text("[C]", NamedTextColor.GRAY)), skipBadged = true).pieces(),
        )
    }

    @Test
    fun `labels names that already carry a badge otherwise`() {
        assertEquals(listOf("§7Sniper", " ", "[CC]", "§7 §8[§7C§8] §7FKDR"), labeled("§7Sniper §8[§7C§8] §7FKDR").pieces())
    }

    @Test
    fun `leaves names that only start with a tagged name alone`() {
        assertEquals(listOf("Bobby and SniperX"), labeled("Bobby and SniperX").pieces())
    }
}
