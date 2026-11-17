package gg.urchin.client

import gg.urchin.api.Access
import gg.urchin.api.ApiException
import gg.urchin.api.MemberAccess
import gg.urchin.api.Tag
import gg.urchin.api.TagType
import gg.urchin.api.UrchinApi
import org.polyfrost.oneconfig.api.event.v1.eventHandler
import org.polyfrost.oneconfig.api.event.v1.events.InitializationEvent
import org.polyfrost.oneconfig.utils.v1.Multithreading
import java.util.concurrent.CompletionException
import java.util.concurrent.TimeUnit

private const val FORBIDDEN = 403
private const val KEY_SETTLE_SECONDS = 1L

class AccountCard(val title: String, val description: String, val type: String)

object Account {
    private val signingIn = AccountCard("Signing In", "Checking your access.", "info")

    @Volatile
    private var keyEdits = 0

    @Volatile
    private var access: Access? = null

    val addableTypes: List<TagType>
        get() = access?.let { it.member?.addableTypes.orEmpty() } ?: TagType.addable

    val removableTypes: List<TagType>
        get() = access?.let { it.member?.removableTypes.orEmpty() } ?: TagType.known

    val revealsHiddenTaggers: Boolean
        get() = access?.member?.revealsHiddenTaggers == true

    fun canRemove(tag: Tag): Boolean =
        tag.type in removableTypes || access?.member?.addedWithinEditWindow(tag, System.currentTimeMillis()) == true

    fun register() {
        UrchinApi.apiKey = UrchinConfig.apiKey.trim()
        eventHandler { _: InitializationEvent -> refresh() }
    }

    fun keyChanged(key: String) {
        UrchinApi.apiKey = key.trim()
        val edit = ++keyEdits
        Multithreading.schedule({ if (edit == keyEdits) refresh() }, KEY_SETTLE_SECONDS, TimeUnit.SECONDS)
    }

    private fun refresh() {
        UrchinConfig.showAccount(signingIn)
        UrchinApi.access().whenCompleteAsync({ access, error ->
            this.access = access
            UrchinConfig.showAccount(card(access, error))
        }, Game.thread)
    }

    private fun card(access: Access?, error: Throwable?): AccountCard {
        val failure = (error as? CompletionException)?.cause ?: error
        return when {
            failure is ApiException && failure.status == FORBIDDEN -> locked()
            failure != null -> AccountCard("Couldn't Reach Urchin", "Your access will show once Urchin is back.", "warning")
            UrchinApi.keyRejected -> AccountCard(
                "Invalid API Key",
                "Using your Minecraft account instead. Get a new key with /dashboard in the Urchin Discord.",
                "error",
            )
            else -> access?.member?.let(::memberCard) ?: AccountCard("Limited Access", "You can view all tag types.", "info")
        }
    }

    private fun locked(): AccountCard =
        if (UrchinApi.apiKey.isEmpty()) {
            AccountCard("Account Locked", "Your account has been locked. Ask staff in the Urchin Discord.", "error")
        } else {
            AccountCard("API Key Locked", "This key has been locked. Ask staff in the Urchin Discord.", "error")
        }

    private fun memberCard(member: MemberAccess): AccountCard =
        if (member.limited) {
            AccountCard("Limited Access", permissions(member), "info")
        } else {
            AccountCard(
                member.discordUsername?.let { "Authenticated as @$it" } ?: "Authenticated",
                permissions(member),
                if (member.restricted) "warning" else "success",
            )
        }

    internal fun permissions(member: MemberAccess): String {
        if (member.removableTypes.isNotEmpty()) return "You can view, add, and remove all tag types."
        val addable = member.addableTypes.map { it.display }
        if (addable.isEmpty()) return "You can view all tag types."
        return "You can view all tag types and add ${listed(addable)} tags."
    }

    private fun listed(items: List<String>): String = when (items.size) {
        1 -> items.single()
        2 -> items.joinToString(" and ")
        else -> items.dropLast(1).joinToString(", ") + ", and " + items.last()
    }
}
