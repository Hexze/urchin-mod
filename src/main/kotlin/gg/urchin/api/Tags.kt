package gg.urchin.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.format.NamedTextColor

/**
 * Material Design Icons used when "MDI Icons" is on. Each icon is drawn in place of a private-use glyph,
 * so it works anywhere Minecraft draws text (chat, tab list, nametags).
 */
enum class TagIcon(val glyph: Char, val texture: String) {
    ALERT_DECAGRAM_OUTLINE('\uE101', "alert-decagram-outline"),
    ALERT_OCTAGRAM_OUTLINE('\uE102', "alert-octagram-outline"),
    ALERT_DECAGRAM('\uE103', "alert-decagram"),
    ALERT_RHOMBUS_OUTLINE('\uE104', "alert-rhombus-outline"),
    ARCHIVE_ALERT_OUTLINE('\uE105', "archive-alert-outline");

    companion object {
        // A blank glyph 2 px wide, put before an icon instead of a full 4 px space.
        const val GAP: Char = '\uE106'
        // How wide an icon and the gap are, in GUI pixels (the icon itself is drawn 9 px wide).
        const val ICON_ADVANCE = 9
        const val GAP_ADVANCE = 2

        private val byGlyph = entries.associateBy { it.glyph }

        @JvmStatic
        fun byGlyph(glyph: Char): TagIcon? = byGlyph[glyph]
    }
}

enum class TagType(
    val id: String,
    val display: String,
    val alias: String,
    val icon: String,
    val color: NamedTextColor,
    val priority: Int,
    val addable: Boolean,
    val mdi: TagIcon? = null,
    val mdiColor: NamedTextColor = color,
) {
    SNIPER("sniper", "Sniper", "s", "S", NamedTextColor.RED, 1, true, TagIcon.ALERT_DECAGRAM),
    BLATANT_CHEATER("blatant_cheater", "Blatant Cheater", "bc", "BC", NamedTextColor.GOLD, 1, true, TagIcon.ALERT_DECAGRAM),
    CLOSET_CHEATER("closet_cheater", "Closet Cheater", "cc", "CC", NamedTextColor.GOLD, 1, true, TagIcon.ALERT_OCTAGRAM_OUTLINE),
    CONFIRMED_CHEATER(
        "confirmed_cheater", "Confirmed Cheater", "ccc", "CCC", NamedTextColor.DARK_PURPLE, 1, false, TagIcon.ALERT_DECAGRAM_OUTLINE,
    ),
    REPLAYS_NEEDED("replays_needed", "Replays Needed", "rn", "RN", NamedTextColor.GRAY, 2, false, TagIcon.ARCHIVE_ALERT_OUTLINE),
    CAUTION("caution", "Caution", "c", "C", NamedTextColor.GRAY, 3, true, TagIcon.ALERT_RHOMBUS_OUTLINE, NamedTextColor.GOLD),
    UNKNOWN("unknown", "Unknown", "?", "?", NamedTextColor.WHITE, 99, false);

    companion object {
        val addable: List<TagType> = entries.filter { it.addable }

        fun byId(id: String): TagType = entries.firstOrNull { it.id == id } ?: UNKNOWN

        fun byInput(input: String): TagType? {
            val key = input.lowercase()
            return entries.firstOrNull { it != UNKNOWN && (it.alias == key || it.id == key) }
        }
    }
}

@Serializable
data class Tag(
    @SerialName("tag_type") val typeId: String,
    val reason: String? = null,
    @SerialName("added_on") val addedOn: Long? = null,
    @SerialName("added_by_username") val addedBy: String? = null,
    @SerialName("expires_at") val expiresAt: Long? = null,
) {
    val type: TagType
        get() = TagType.byId(typeId)
}

@Serializable
data class PlayerTags(
    val uuid: String,
    @SerialName("displayname") val displayName: String? = null,
    val tags: List<Tag> = emptyList(),
)

@Serializable
data class TagBatch(
    val players: Map<String, List<Tag>> = emptyMap(),
    @SerialName("displaynames") val displayNames: Map<String, String> = emptyMap(),
)
