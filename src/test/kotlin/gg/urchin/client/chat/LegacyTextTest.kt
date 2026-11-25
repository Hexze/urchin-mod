package gg.urchin.client.chat

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

private const val LABEL = " §8[§6CC§8]§r"

class LegacyTextTest {
    @Test
    fun `inserts right after the name, before any suffix`() {
        assertEquals("§b[MVP+] Bob$LABEL§b §7[GUILD]", insertAfterName("§b[MVP+] Bob §7[GUILD]", "Bob", LABEL))
    }

    @Test
    fun `appends when the name is missing`() {
        assertEquals("§7Someone$LABEL", insertAfterName("§7Someone", "Bob", LABEL))
    }

    @Test
    fun `carries format codes until a reset`() {
        assertEquals("§a§l", activeFormatting("§a§lBob"))
        assertEquals("", activeFormatting("§a§l§rBob"))
        assertEquals("§c", activeFormatting("§a§l§cBob"))
        assertEquals("§a", activeFormatting("§§a§zBob§"))
    }

    @Test
    fun `finds the name only as a whole word`() {
        assertEquals(9, nameEnd("Bobby Bob", "Bob"))
        assertEquals(14, nameEnd("BobbyBob §7Bob", "Bob"))
        assertEquals(5, nameEnd("§aBob", "Bob"))
        assertEquals(3, nameEnd("Bob", "Bob"))
        assertNull(nameEnd("Bobby xBob Bob_", "Bob"))
    }
}
