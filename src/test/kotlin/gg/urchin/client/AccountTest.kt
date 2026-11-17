package gg.urchin.client

import gg.urchin.api.MemberAccess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private fun adding(vararg tags: String) = Account.permissions(MemberAccess(addableTags = tags.toList()))

class AccountTest {
    @Test
    fun `lists the tag types a member can add`() {
        assertEquals("You can view all tag types.", adding())
        assertEquals("You can view all tag types and add Sniper tags.", adding("sniper"))
        assertEquals("You can view all tag types and add Sniper and Caution tags.", adding("sniper", "caution"))
        assertEquals(
            "You can view all tag types and add Sniper, Blatant Cheater, and Closet Cheater tags.",
            adding("sniper", "blatant_cheater", "closet_cheater"),
        )
    }

    @Test
    fun `staff can do everything`() {
        assertEquals(
            "You can view, add, and remove all tag types.",
            Account.permissions(MemberAccess(addableTags = listOf("sniper"), removableTags = listOf("sniper", "caution"))),
        )
    }
}
