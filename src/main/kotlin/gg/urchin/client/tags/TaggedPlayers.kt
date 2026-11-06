package gg.urchin.client.tags

import gg.urchin.api.Tag
import gg.urchin.api.TagType
import gg.urchin.client.Audience
import gg.urchin.client.TaggedPlayer
import java.util.UUID

object TaggedPlayers {
    private val byUuid = mutableMapOf<UUID, TaggedPlayer>()
    private val byName = mutableMapOf<String, TaggedPlayer>()

    fun add(tagged: List<TaggedPlayer>) =
        tagged.forEach {
            byUuid[it.player.uuid] = it
            byName[it.player.name] = it
        }

    fun clear() {
        byUuid.clear()
        byName.clear()
    }

    fun namesFor(audience: Audience): Set<String> = byName.filterValues { audience.includes(it.account) }.keys

    fun topTagFor(uuid: UUID, audience: Audience): TagType? =
        byUuid[uuid]?.takeIf { audience.includes(it.account) }?.tags?.map { it.type }?.minByOrNull { it.priority }

    fun tagsNamed(name: String): List<Tag> = byName[name]?.tags.orEmpty()
}
