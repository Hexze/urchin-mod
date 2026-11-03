package gg.urchin.client.tags

import gg.urchin.client.ListedPlayer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.Base64
import java.util.UUID

private val ACCOUNT: UUID = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5")
private val PLACEHOLDER: UUID = UUID.fromString("3f1c2a4e-9b7d-2c11-8e5f-0a6b4c3d2e1f")

private fun textures(name: String, id: UUID = ACCOUNT): String =
    Base64.getEncoder().encodeToString(
        """{"timestamp":1,"profileId":"${id.toString().replace("-", "")}","profileName":"$name","textures":{}}""".toByteArray(),
    )

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
    fun `unreadable skin data is ignored`() {
        assertNull(accountOf(ListedPlayer(PLACEHOLDER, "Notch", "not base64!"), inReplay = true))
    }
}
