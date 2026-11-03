package gg.urchin.client.tags

import gg.urchin.api.Tag
import gg.urchin.api.TagType
import gg.urchin.client.TaggedPlayer
import java.util.UUID

object TaggedPlayers {
    private val tagsByUuid = mutableMapOf<UUID, List<Tag>>()

    fun add(tagged: List<TaggedPlayer>) = tagged.forEach { (player, tags) -> tagsByUuid[player.uuid] = tags }

    fun clear() = tagsByUuid.clear()

    fun topTag(uuid: UUID): TagType? = tagsByUuid[uuid]?.map { it.type }?.minByOrNull { it.priority }
}
