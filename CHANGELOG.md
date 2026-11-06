# Changelog

## 0.1.0

- Sign in to the Urchin API with the player's Minecraft account.
- Chat alerts for tagged players when joining a Hypixel game, with sound, delay and compact settings.
- Alerts and tab, nametag and chat labels each choose who they cover: everyone, everyone but you, everyone but your party, or nobody.
- Every tag shown in chat has a hover card with its details; clicking it pastes a summary into the chat box.
- The Urchin logo in the mod list and OneConfig, and an Urchin gradient chat prefix.
- `/urchin <players>` shows each player's tags with buttons to add or remove them; `/urchin check <players>` does the same for players named like a subcommand.
- Each player's lookup, panel, prompt and result replace one another in chat instead of stacking up.
- `/urchin tag <player> <type> <reason>` and `/urchin untag <player> <type>` ask for confirmation, and new tags can be undone.
- Settings in OneConfig, opened with `/urchin` or from the mod's card, whose toggle turns the mod and its commands on or off.
- Nicked players wearing their own skin are looked up by that skin's account; their tags show next to the nick, and alerts add their real name in parentheses.
- Alerts and panels show a player's name as their team formats it while they're in the lobby.
- Hypixel replays look up and alert the recorded players by the account in their skin.
- While Starfish's Urchin plugin is running, chat and tab labels skip names it has already badged, so its Bedwars tab tags show without doubling up.
- Works alongside Starfish's Urchin plugin on 1.8.9: the plugin turns off the alerts and labels the mod is showing, and works as usual without the mod.
