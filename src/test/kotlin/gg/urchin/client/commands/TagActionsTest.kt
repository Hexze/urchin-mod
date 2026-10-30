package gg.urchin.client.commands

import gg.urchin.api.ApiException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.IOException
import java.util.concurrent.CompletionException

class TagActionsTest {
    @Test
    fun `reads the plain name out of a ranked display name`() {
        assertEquals("Notch", plainName("§b[MVP§c+§b] Notch"))
        assertEquals("jeb_", plainName("§7jeb_"))
    }

    @Test
    fun `explains api failures in player terms`() {
        assertEquals("This player's tags are locked", describe(CompletionException(ApiException(403, "player is locked"))))
        assertEquals("Conflicts with an existing tag", describe(ApiException(409, "conflicts with sniper")))
        assertEquals("Rate limited - slow down and try again.", describe(ApiException(429, null)))
        assertEquals("Request failed (500).", describe(ApiException(500, null)))
        assertEquals("Couldn't reach Urchin: timed out", describe(CompletionException(IOException("timed out"))))
    }
}
