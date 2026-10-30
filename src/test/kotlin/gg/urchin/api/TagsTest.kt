package gg.urchin.api

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TagsTest {
    @Test
    fun `decodes a player lookup from the api`() {
        val body = """
            {
              "uuid": "069a79f444e94726a5befca90e38aaf5",
              "displayname": "§a[VIP] Notch",
              "tags": [
                { "tag_type": "blatant_cheater", "reason": "scaffold", "added_on": 1700000000000, "added_by_username": "mod", "extra": 1 },
                { "tag_type": "something_new" }
              ]
            }
        """.trimIndent()

        val player = apiJson.decodeFromString<PlayerTags>(body)

        assertEquals("§a[VIP] Notch", player.displayName)
        assertEquals(TagType.BLATANT_CHEATER, player.tags[0].type)
        assertEquals("mod", player.tags[0].addedBy)
        assertEquals(TagType.UNKNOWN, player.tags[1].type)
        assertNull(player.tags[1].reason)
    }

    @Test
    fun `resolves tag types from aliases and ids`() {
        assertEquals(TagType.BLATANT_CHEATER, TagType.byInput("BC"))
        assertEquals(TagType.CAUTION, TagType.byInput("caution"))
        assertNull(TagType.byInput("?"))
        assertNull(TagType.byInput("nonsense"))
    }
}
