package gg.urchin.client

import gg.urchin.api.Tag
import java.util.UUID

data class ListedPlayer(val uuid: UUID, val name: String, val textures: String?)

data class TaggedPlayer(val player: ListedPlayer, val tags: List<Tag>, val displayName: String?)
