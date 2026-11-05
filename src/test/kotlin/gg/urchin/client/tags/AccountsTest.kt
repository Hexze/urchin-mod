package gg.urchin.client.tags

import gg.urchin.client.ListedPlayer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.Base64
import java.util.UUID

private val ACCOUNT: UUID = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5")
private val PLACEHOLDER: UUID = UUID.fromString("3f1c2a4e-9b7d-2c11-8e5f-0a6b4c3d2e1f")
private val LIVE_NICK: UUID = UUID.fromString("0a1b2c3d-0000-1000-8000-000000000001")
private const val OWN_SKIN = "0123456789abcdef"
private val POOL_SKIN = NICK_POOL_SKINS.first()

private fun textures(name: String, id: UUID = ACCOUNT, skin: String? = null): String {
    val skinTextures = skin?.let { """"SKIN":{"url":"http://textures.minecraft.net/texture/$it"}""" }.orEmpty()
    return Base64.getEncoder().encodeToString(
        """{"timestamp":1,"profileId":"${id.toString().replace("-", "")}","profileName":"$name","textures":{$skinTextures}}""".toByteArray(),
    )
}

class AccountsTest {
    @Test
    fun `a real player is looked up by their own UUID`() {
        assertEquals(ACCOUNT, accountOf(ListedPlayer(ACCOUNT, "Notch", null), inReplay = false))
    }

    @Test
    fun `a replay player is looked up by the account their skin was recorded from`() {
        assertEquals(ACCOUNT, accountOf(ListedPlayer(PLACEHOLDER, "Notch", textures("Notch")), inReplay = true))
    }

    @Test
    fun `a skin recorded from someone else is not trusted`() {
        assertNull(accountOf(ListedPlayer(PLACEHOLDER, "Notch", textures("Jeb")), inReplay = true))
    }

    @Test
    fun `placeholders outside replays are NPCs`() {
        assertNull(accountOf(ListedPlayer(PLACEHOLDER, "Notch", textures("Notch")), inReplay = false))
    }

    @Test
    fun `a nick wearing their own skin is looked up by the skin's account`() {
        assertEquals(ACCOUNT, accountOf(ListedPlayer(LIVE_NICK, "Nicky", textures("Notch", skin = OWN_SKIN)), inReplay = false))
    }

    @Test
    fun `a nick wearing a pool skin can't be resolved`() {
        assertNull(accountOf(ListedPlayer(LIVE_NICK, "Nicky", textures("Notch", skin = POOL_SKIN)), inReplay = false))
    }

    @Test
    fun `a nick whose skin names the nick itself is not resolved`() {
        assertNull(accountOf(ListedPlayer(LIVE_NICK, "Nicky", textures("Nicky", skin = OWN_SKIN)), inReplay = false))
    }

    @Test
    fun `unreadable skin data is ignored`() {
        assertNull(accountOf(ListedPlayer(PLACEHOLDER, "Notch", "not base64!"), inReplay = true))
    }
}
