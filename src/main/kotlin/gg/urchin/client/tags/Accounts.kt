package gg.urchin.client.tags

import gg.urchin.api.apiJson
import gg.urchin.client.ListedPlayer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Base64
import java.util.Collections
import java.util.UUID

private const val PLAYER_UUID_VERSION = 4
private const val LIVE_NICK_UUID_VERSION = 1
private const val UUID_HEX_LENGTH = 32
private const val HALF_UUID_HEX_LENGTH = 16
private const val HEX_RADIX = 16
private const val MAX_CACHED_PROFILES = 512
private const val REPLAY_NAME_LENGTH = 14

@Serializable
private class SkinProfile(val profileId: String, val profileName: String, val textures: SkinTextures = SkinTextures()) {
    val account: UUID? get() = undashedUuid(profileId)
    val skinHash: String? get() = textures.skin?.url?.substringAfterLast('/')
}

@Serializable
private class SkinTextures(@SerialName("SKIN") val skin: Skin? = null)

@Serializable
private class Skin(val url: String)

internal fun accountOf(player: ListedPlayer, inReplay: Boolean): UUID? = when {
    player.uuid.isPlayerAccount() -> player.uuid
    player.uuid.isLiveNick() -> nickWearer(player)
    inReplay -> recordedProfile(player)?.account
    else -> null
}

internal fun accountName(player: ListedPlayer, inReplay: Boolean): String? = when {
    player.uuid.isPlayerAccount() -> player.name
    inReplay && !player.uuid.isLiveNick() -> recordedProfile(player)?.profileName
    else -> null
}

private fun nickWearer(player: ListedPlayer): UUID? {
    val profile = player.textures?.let(::skinProfile) ?: return null
    val ownSkin = profile.profileName != player.name && profile.skinHash?.let { it !in NICK_POOL_SKINS } == true
    return if (ownSkin) profile.account?.takeIf { it.isPlayerAccount() } else null
}

private fun recordedProfile(player: ListedPlayer): SkinProfile? =
    player.textures?.let(::skinProfile)?.takeIf { it.profileName.isRecordedAs(player.name) && it.account?.isPlayerAccount() == true }

private fun String.isRecordedAs(name: String): Boolean = this == name || (name.length == REPLAY_NAME_LENGTH && startsWith(name))

private val skinProfiles: MutableMap<String, SkinProfile?> = Collections.synchronizedMap(
    object : LinkedHashMap<String, SkinProfile?>(MAX_CACHED_PROFILES, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, SkinProfile?>) = size > MAX_CACHED_PROFILES
    },
)

private fun skinProfile(textures: String): SkinProfile? =
    if (textures in skinProfiles) skinProfiles[textures] else decodeSkinProfile(textures).also { skinProfiles[textures] = it }

private fun decodeSkinProfile(textures: String): SkinProfile? =
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
