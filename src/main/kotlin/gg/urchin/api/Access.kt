package gg.urchin.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Access(val member: MemberAccess? = null)

@Serializable
data class MemberAccess(
    @SerialName("discord_id") val discordId: String? = null,
    @SerialName("discord_username") val discordUsername: String? = null,
    @SerialName("minecraft_linked") val minecraftLinked: Boolean = true,
    @SerialName("in_urchin_server") val inUrchinServer: Boolean? = null,
    val suspended: Boolean = false,
    @SerialName("tagging_disabled") val taggingDisabled: Boolean = false,
    @SerialName("addable_tags") val addableTags: List<String> = emptyList(),
    @SerialName("removable_tags") val removableTags: List<String> = emptyList(),
    @SerialName("reveals_hidden_taggers") val revealsHiddenTaggers: Boolean = false,
) {
    val addableTypes: List<TagType>
        get() = addableTags.map(TagType::byId).filter(TagType::addable)

    val removableTypes: List<TagType>
        get() = removableTags.map(TagType::byId).filter { it != TagType.UNKNOWN }

    val limited: Boolean
        get() = !minecraftLinked || inUrchinServer == false

    val restricted: Boolean
        get() = suspended || taggingDisabled
}
