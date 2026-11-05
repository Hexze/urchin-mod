# Changelog

## 0.1.0

- Sign in to the Urchin API with the player's Minecraft account.
- Chat alerts for tagged players when joining a Hypixel game, with sound, delay, compact, ignore-yourself and ignore-your-party settings.
- Every tag shown in chat has a hover card with its details; clicking it pastes a summary into the chat box.
- The Urchin logo in the mod list and OneConfig, and an Urchin gradient chat prefix.
- `/urchin <players>` shows each player's tags with buttons to add or remove them; `/urchin check <players>` does the same for players named like a subcommand.
- Each player's lookup, panel, prompt and result replace one another in chat instead of stacking up.
- `/urchin tag <player> <type> <reason>` and `/urchin untag <player> <type>` ask for confirmation, and new tags can be undone.
- Settings in OneConfig, opened with `/urchin` or from the mod's card, whose toggle turns the mod and its commands on or off.
- Nicked players wearing their own skin are looked up by that skin's account; their tags show next to the nick, and alerts add their real name in parentheses.
- Alerts and panels show a player's name as the tab list formats it while they're in the lobby.
- Works alongside Starfish's Urchin plugin on 1.8.9: the plugin turns off the alerts and labels the mod is showing, and works as usual without the mod.
