package gg.urchin.client.chat

import org.junit.jupiter.api.Assertions.assertEquals
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
    }
}
