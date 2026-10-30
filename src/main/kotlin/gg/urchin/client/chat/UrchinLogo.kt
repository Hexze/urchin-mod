package gg.urchin.client.chat

import gg.urchin.UrchinConstants
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.ShadowColor

object UrchinLogo {
    const val GLYPH = ''
    const val ADVANCE = 10

    val component: Component = Component.text(GLYPH, NamedTextColor.WHITE)
        .font(Key.key(UrchinConstants.ID, "icons"))
        .shadowColor(ShadowColor.none())
}
