package gg.urchin.api

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private const val NOW = 10_000_000L
private const val MINUTE = 60_000L

private val member = MemberAccess(discordId = "42")

private fun tag(addedBy: Long?, minutesAgo: Long) = Tag("sniper", addedOn = NOW - minutesAgo * MINUTE, addedById = addedBy)

class AccessTest {
    @Test
    fun `members can remove their own tags for 30 minutes`() {
        assertTrue(member.addedWithinEditWindow(tag(42, 5), NOW))
        assertTrue(member.addedWithinEditWindow(tag(42, 30), NOW))
        assertFalse(member.addedWithinEditWindow(tag(42, 31), NOW))
    }

    @Test
    fun `other members' and hidden tags are not their own`() {
        assertFalse(member.addedWithinEditWindow(tag(7, 5), NOW))
        assertFalse(member.addedWithinEditWindow(tag(null, 5), NOW))
    }

    @Test
    fun `unlinked or outside the server is limited`() {
        assertTrue(MemberAccess(minecraftLinked = false).limited)
        assertTrue(MemberAccess(inUrchinServer = false).limited)
        assertFalse(MemberAccess(inUrchinServer = null).limited)
    }
}
