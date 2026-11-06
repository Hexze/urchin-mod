//? if = 1.8.9 {
/*package gg.urchin.client.standalone

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SmoothScrollTest {
    private fun settle(scroll: SmoothScroll, max: Float) {
        repeat(80) {
            Thread.sleep(5)
            scroll.update(max)
        }
    }

    @Test
    fun `a wheel notch eases to its distance instead of jumping`() {
        val scroll = SmoothScroll(100f)
        scroll.wheel(-120f)
        assertEquals(0f, scroll.offset)
        assertTrue(scroll.moving)
        settle(scroll, 1000f)
        assertEquals(100f, scroll.offset, 0.5f)
        assertFalse(scroll.moving)
    }

    @Test
    fun `scrolling the other way drops what was still pending`() {
        val scroll = SmoothScroll(100f)
        scroll.wheel(-120f)
        scroll.wheel(-120f)
        scroll.wheel(120f)
        settle(scroll, 1000f)
        assertEquals(0f, scroll.offset, 0.5f)
    }

    @Test
    fun `it stops at both ends`() {
        val scroll = SmoothScroll(500f)
        scroll.wheel(-120f)
        settle(scroll, 200f)
        assertEquals(200f, scroll.offset, 0.001f)
        scroll.wheel(120f)
        scroll.wheel(120f)
        settle(scroll, 200f)
        assertEquals(0f, scroll.offset, 0.001f)
    }

    @Test
    fun `a page that does not overflow never moves`() {
        val scroll = SmoothScroll(100f)
        scroll.wheel(-120f)
        settle(scroll, 0f)
        assertEquals(0f, scroll.offset)
    }
}
*///?}
