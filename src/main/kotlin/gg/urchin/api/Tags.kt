package gg.urchin.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.format.NamedTextColor

enum class TagType(
    val id: String,
    val display: String,
    val alias: String,
    val icon: String,
    val color: NamedTextColor,
    val priority: Int,
    val addable: Boolean,
) {
    SNIPER("sniper", "Sniper", "s", "S", NamedTextColor.RED, 1, true),
    BLATANT_CHEATER("blatant_cheater", "Blatant Cheater", "bc", "BC", NamedTextColor.GOLD, 1, true),
    CLOSET_CHEATER("closet_cheater", "Closet Cheater", "cc", "CC", NamedTextColor.GOLD, 1, true),
    CONFIRMED_CHEATER("confirmed_cheater", "Confirmed Cheater", "ccc", "CCC", NamedTextColor.DARK_PURPLE, 1, false),
    REPLAYS_NEEDED("replays_needed", "Replays Needed", "rn", "RN", NamedTextColor.GRAY, 2, false),
    CAUTION("caution", "Caution", "c", "C", NamedTextColor.GRAY, 3, true),
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
