package gg.urchin.client

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

private val ME = UUID.randomUUID()
private val PARTY_MEMBER = UUID.randomUUID()
private val STRANGER = UUID.randomUUID()
private val PARTY = setOf(ME, PARTY_MEMBER)

private fun Audience.covers(): List<UUID> = listOf(ME, PARTY_MEMBER, STRANGER).filter { includes(it, ME, PARTY) }

class AudienceTest {
    @Test
    fun `each audience covers the players it names`() {
        assertEquals(listOf(ME, PARTY_MEMBER, STRANGER), Audience.EVERYONE.covers())
        assertEquals(listOf(PARTY_MEMBER, STRANGER), Audience.EVERYONE_BUT_ME.covers())
        assertEquals(listOf(STRANGER), Audience.EVERYONE_BUT_MY_PARTY.covers())
        assertEquals(emptyList<UUID>(), Audience.NOBODY.covers())
    }

    @Test
    fun `leaving out the party also leaves out a player with no party`() {
        assertEquals(listOf(STRANGER), listOf(ME, STRANGER).filter { Audience.EVERYONE_BUT_MY_PARTY.includes(it, ME, emptySet()) })
    }
}
