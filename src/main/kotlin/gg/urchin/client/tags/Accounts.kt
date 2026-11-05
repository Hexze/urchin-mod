package gg.urchin.client.tags

import gg.urchin.api.apiJson
import gg.urchin.client.ListedPlayer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Base64
import java.util.UUID

private const val PLAYER_UUID_VERSION = 4
private const val LIVE_NICK_UUID_VERSION = 1
private const val UUID_HEX_LENGTH = 32
private const val HALF_UUID_HEX_LENGTH = 16
private const val HEX_RADIX = 16

@Serializable
private class SkinProfile(val profileId: String, val profileName: String, val textures: SkinTextures = SkinTextures()) {
    val skinHash: String? get() = textures.skin?.url?.substringAfterLast('/')
}

@Serializable
private class SkinTextures(@SerialName("SKIN") val skin: Skin? = null)

@Serializable
private class Skin(val url: String)

internal fun accountOf(player: ListedPlayer, inReplay: Boolean): UUID? = when {
    player.uuid.isPlayerAccount() -> player.uuid
    player.uuid.isLiveNick() -> nickWearer(player)
    inReplay -> recordedAccount(player)?.takeIf { it.isPlayerAccount() }
    else -> null
}

private fun nickWearer(player: ListedPlayer): UUID? {
    val profile = player.textures?.let(::skinProfile) ?: return null
    val ownSkin = profile.profileName != player.name && profile.skinHash?.let { it !in NICK_POOL_SKINS } == true
    return if (ownSkin) undashedUuid(profile.profileId)?.takeIf { it.isPlayerAccount() } else null
}

private fun recordedAccount(player: ListedPlayer): UUID? {
    val profile = player.textures?.let(::skinProfile) ?: return null
    return if (profile.profileName == player.name) undashedUuid(profile.profileId) else null
}

private fun skinProfile(textures: String): SkinProfile? =
    try {
        apiJson.decodeFromString<SkinProfile>(Base64.getDecoder().decode(textures).decodeToString())
    } catch (_: IllegalArgumentException) {
        null
    }

internal fun undashedUuid(hex: String): UUID? {
    if (hex.length != UUID_HEX_LENGTH) return null
    val high = hex.substring(0, HALF_UUID_HEX_LENGTH).toULongOrNull(HEX_RADIX) ?: return null
    val low = hex.substring(HALF_UUID_HEX_LENGTH).toULongOrNull(HEX_RADIX) ?: return null
    return UUID(high.toLong(), low.toLong())
}

internal fun UUID.isLiveNick(): Boolean = version() == LIVE_NICK_UUID_VERSION

private fun UUID.isPlayerAccount(): Boolean = version() == PLAYER_UUID_VERSION
