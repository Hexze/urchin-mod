package gg.urchin.api

import gg.urchin.client.chat.TagText
import net.kyori.adventure.text.TextComponent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TagIconTest {
    @Test
    fun `every addable and cheater tag type has an icon`() {
        for (type in listOf(TagType.SNIPER, TagType.BLATANT_CHEATER, TagType.CLOSET_CHEATER, TagType.CONFIRMED_CHEATER, TagType.REPLAYS_NEEDED, TagType.CAUTION)) {
            assertNotNull(type.mdi, type.display)
        }
        assertNull(TagType.UNKNOWN.mdi)
    }

    @Test
    fun `icon glyphs are unique, private use, and never the gap`() {
        val glyphs = TagIcon.entries.map { it.glyph }
        assertEquals(glyphs.size, glyphs.toSet().size)
        for (glyph in glyphs) {
            assertEquals(glyph, TagIcon.byGlyph(glyph)?.glyph)
            assert(glyph in ''..'')
            assert(glyph != TagIcon.GAP)
        }
        assertNull(TagIcon.byGlyph(TagIcon.GAP))
    }

    @Test
    fun `the gap is narrower than a space and the icon narrower than before`() {
        assert(TagIcon.GAP_ADVANCE < 4)
        assertEquals(9, TagIcon.ICON_ADVANCE)
    }

    @Test
    fun `badges are separated by a plain space until icons are switched on`() {
        val gap = TagText.gap() as TextComponent
        assertEquals(" ", gap.content())
    }
}
