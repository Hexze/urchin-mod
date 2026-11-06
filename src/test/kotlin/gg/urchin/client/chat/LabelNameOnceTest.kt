package gg.urchin.client.chat

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LabelNameOnceTest {
    private val label = "\u00a78 [\u00a7r\u00a75CCC\u00a7r\u00a78]\u00a7r"

    @Test
    fun `labels a name built by another mod`() {
        assertEquals("\u00a7bSteve$label\u00a7b\u00a7r", labelNameOnce("\u00a7bSteve\u00a7r", "Steve", label))
    }

    @Test
    fun `does not label the score under a name tag`() {
        assertEquals("20 \u00a7chp", labelNameOnce("20 \u00a7chp", "Steve", label))
    }

    @Test
    fun `does not label text that already has the label`() {
        val labeled = labelNameOnce("Steve", "Steve", label)
        assertEquals(labeled, labelNameOnce(labeled, "Steve", label))
    }

    @Test
    fun `does not match a longer name`() {
        assertEquals("Steve_2", labelNameOnce("Steve_2", "Steve", label))
    }
}
