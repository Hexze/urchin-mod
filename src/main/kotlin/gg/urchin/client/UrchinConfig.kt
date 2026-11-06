package gg.urchin.client

import gg.urchin.UrchinConstants
import org.polyfrost.oneconfig.api.config.v1.KtConfig
import org.polyfrost.oneconfig.api.platform.v1.DesktopHelper
import java.net.URI

private const val ALERTS = "Alerts"
private const val LABELS = "Labels"
private const val ACCOUNT = "Account"
private val DISCORD_INVITE = URI("https://discord.gg/urchin")

object UrchinConfig : KtConfig(
    id = "${UrchinConstants.ID}.json",
    title = UrchinConstants.NAME,
    category = Category.HYPIXEL,
    icon = "/assets/${UrchinConstants.ID}/icon.png",
) {
    var enabled by masterToggle()

    var alertsFor by audience(
        name = "Alert For",
        def = Audience.EVERYONE_BUT_ME,
        description = "Who sends a chat alert when they join your game tagged.",
        subcategory = ALERTS,
    )

    var alertSound by switch(
        name = "Sound",
        def = true,
        description = "Play a ping with each alert.",
        subcategory = ALERTS,
    )

    var alertDelay by slider(
        name = "Delay",
        def = 0f,
        description = "Wait this long after a player joins before alerting.",
        subcategory = ALERTS,
        unit = "ms",
        min = 0f,
        max = 1000f,
        step = 250f,
    )

    var compactAlerts by switch(
        name = "Compact",
        def = false,
        description = "Show each tagged player as one line of badges. Hover a badge for the details.",
        subcategory = ALERTS,
    )

    var tabLabelsFor by audience(
        name = "Tab List",
        def = Audience.EVERYONE,
        description = "Whose tag shows next to their name in the tab list.",
        subcategory = LABELS,
    )

    var nametagLabelsFor by audience(
        name = "Nametags",
        def = Audience.EVERYONE,
        description = "Whose tag shows next to the name above their head.",
        subcategory = LABELS,
    )

    var chatLabelsFor by audience(
        name = "Chat",
        def = Audience.EVERYONE,
        description = "Whose tags show next to their name in chat. Hover a tag for the details.",
        subcategory = LABELS,
    )

    val limitedAccess by info(
        name = "Limited Access",
        description = "You can request and view blacklist tags. To use other features, provide an API key or " +
            "link this Minecraft account to your Discord account in the Urchin Discord.",
        subcategory = ACCOUNT,
    )

    val urchinDiscord by button(
        name = "Urchin Discord",
        description = "discord.gg/urchin",
        subcategory = ACCOUNT,
        text = "Join",
    ) { DesktopHelper.browse(DISCORD_INVITE) }

    init {
        addAliases(*arrayOf("blacklist", "tags"))
        disableAllUnless(::enabled.name)
    }

    val audiences: List<Audience>
        get() = listOf(alertsFor, tabLabelsFor, nametagLabelsFor, chatLabelsFor)

    private fun audience(name: String, def: Audience, description: String, subcategory: String) = dropdown(
        name = name,
        defaultOption = def,
        options = Audience.entries.toTypedArray(),
        description = description,
        subcategory = subcategory,
        stringTransformer = Audience::label,
    )
}
