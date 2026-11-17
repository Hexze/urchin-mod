package gg.urchin.client.commands

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

private const val HOUR = 3_600_000L
private const val DAY = 24 * HOUR

class WatchLengthTest {
    @Test
    fun `reads durations like the watch command`() {
        assertEquals("336h", WatchLength.parse("")?.apiValue)
        assertEquals("240h", WatchLength.parse("10")?.apiValue)
        assertEquals("240h", WatchLength.parse("10d")?.apiValue)
        assertEquals("48h", WatchLength.parse("48h")?.apiValue)
        assertEquals("336h", WatchLength.parse("2W")?.apiValue)
    }

    @Test
    fun `zero and permanent never expire`() {
        assertEquals(WatchLength.Permanent, WatchLength.parse("0"))
        assertEquals(WatchLength.Permanent, WatchLength.parse("permanent"))
        assertNull(WatchLength.Permanent.expiresAt(0))
    }

    @Test
    fun `expiry counts from now`() {
        assertEquals(3 * DAY, WatchLength.parse("3d")?.expiresAt(0))
    }

    @Test
    fun `rejects anything else`() {
        assertNull(WatchLength.parse("soon"))
        assertNull(WatchLength.parse("10m"))
        assertNull(WatchLength.parse("-1d"))
    }
}
