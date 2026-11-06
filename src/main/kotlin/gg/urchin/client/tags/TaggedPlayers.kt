package gg.urchin.client.tags

import gg.urchin.api.Tag
import gg.urchin.api.TagType
import gg.urchin.client.TaggedPlayer
import java.util.UUID

object TaggedPlayers {
    private val tagsByUuid = mutableMapOf<UUID, List<Tag>>()
    private val tagsByName = mutableMapOf<String, List<Tag>>()

    val names: Set<String>
        get() = tagsByName.keys

    fun add(tagged: List<TaggedPlayer>) =
        tagged.forEach { (player, tags) ->
            tagsByUuid[player.uuid] = tags
            tagsByName[player.name] = tags
        }

    fun clear() {
        tagsByUuid.clear()
        tagsByName.clear()
    }

    fun topTag(uuid: UUID): TagType? = tagsByUuid[uuid]?.map { it.type }?.minByOrNull { it.priority }

    fun topTagNamed(name: String): TagType? = tagsByName[name]?.map { it.type }?.minByOrNull { it.priority }

    fun tagsNamed(name: String): List<Tag> = tagsByName[name].orEmpty()
}
