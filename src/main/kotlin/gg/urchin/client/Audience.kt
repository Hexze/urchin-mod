package gg.urchin.client

import gg.urchin.api.MinecraftSession
import gg.urchin.client.alerts.Party
import java.util.UUID

enum class Audience(val label: String) {
    EVERYONE("Everyone"),
    EVERYONE_BUT_ME("Everyone but me"),
    EVERYONE_BUT_MY_PARTY("Everyone but my party"),
    NOBODY("Nobody");

    fun includes(account: UUID): Boolean = includes(account, MinecraftSession.uuid, Party.members)

    internal fun includes(account: UUID, me: UUID, party: Set<UUID>): Boolean = when (this) {
        EVERYONE -> true
        EVERYONE_BUT_ME -> account != me
        EVERYONE_BUT_MY_PARTY -> account != me && account !in party
        NOBODY -> false
    }
}
