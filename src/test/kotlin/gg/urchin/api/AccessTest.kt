package gg.urchin.api

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AccessTest {
    @Test
    fun `unlinked or outside the server is limited`() {
        assertTrue(MemberAccess(minecraftLinked = false).limited)
        assertTrue(MemberAccess(inUrchinServer = false).limited)
        assertFalse(MemberAccess(inUrchinServer = null).limited)
    }
}
