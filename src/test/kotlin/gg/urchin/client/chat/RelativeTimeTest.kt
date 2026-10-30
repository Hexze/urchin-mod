package gg.urchin.client.chat

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private const val NOW = 1_700_000_000_000L
private const val MINUTE = 60_000L
private const val DAY = 86_400_000L

class RelativeTimeTest {
    @Test
    fun `describes how long ago a tag was added`() {
        assertEquals("just now", timeAgo(NOW - 30_000L, NOW))
        assertEquals("1 minute ago", timeAgo(NOW - MINUTE, NOW))
        assertEquals("3 days ago", timeAgo(NOW - 3 * DAY, NOW))
        assertEquals("1 year ago", timeAgo(NOW - 400 * DAY, NOW))
    }

    @Test
    fun `describes when a tag expires`() {
        assertEquals("Expired", expiry(NOW, NOW))
        assertEquals("Expires in under a minute", expiry(NOW + 30_000L, NOW))
        assertEquals("Expires in 2 hours", expiry(NOW + 2 * 60 * MINUTE, NOW))
    }
}
