# Changelog

## 0.4.0

### Fixed

- Tags missing from the tab list and nametags when other mods are installed. Both are fixed for any mod combination, not only AUtils. Reproduced with the real AUtils-ornithe 3.6.2 jar and confirmed in the merged game classes:
  - Tab list: AUtils' Hytils and Level Head hooks set the name's return value, which makes the method return early, so Urchin's hook after them never ran. It hit every Hypixel tab row, because those names come from scoreboard teams. Urchin now wraps the whole method, outside every other mod.
  - Nametags: Sarcio (bundled with AUtils, also on OneClient) builds the nametag text itself without asking for the player's display name, so Urchin's label was never used. Urchin now adds the label where the text is stored and again where every nametag is drawn, before AUtils' Nametags or Argentum's batching see it. This also covers the sneaking nametag.
  - The score under a name (for example `20 hp`) is not labelled, and a label is never added twice.

### Changed

- `/urchin` now opens the settings. `/starfish` is removed on both standalone and OneClient. `/urchin <players>`, `check`, `tag`, `untag`, `confirm` and `debug` are unchanged.
- Settings screen: every rounded corner now uses the same 6 px radius. Before, they ranged from 8 px to 16 px.
  - The panel, cards, header and sidebar went from 12 px to 6 px, so slightly sharper.
  - Switches went from fully round pills to 6 px, so much sharper.
  - The ENABLED button, sidebar entries, buttons and close button use the same radius.
  - The slider knob is now a rounded square instead of a circle.
  - Shapes too small for 6 px (the scrollbar thumb, the active page marker) are as round as their size allows.

### Removed

- The "Urchin · Standalone" text at the bottom of the settings sidebar.

## 0.3.0

### Fixed

- Tags missing on the standalone (no OneConfig) 1.8.9 build. Lookups waited for Hypixel's location packet with no fallback and nothing in the log, so if it never arrived no tag was ever fetched and the tab list, nametags, chat and alerts all stayed empty. Now:
  - After 5 seconds on Hypixel without a location, players are looked up anyway (labels only; alerts still need a location).
  - Tag lookups, Hypixel's `hello` and location packets, and failures are logged.
  - `/urchin debug` prints what Urchin knows about the current server (location, players listed and looked up, tagged count, Mod API state).
- The standalone Hypixel Mod API connection now behaves exactly like the official Ornithe one: it no longer sends a vanilla `REGISTER` plugin message (the official mod never does), and it only subscribes to location events after Hypixel's `hello`, as the library expects.
- Tab list and nametag tags also match a player by name when the list entry carries a different UUID than the one the player was looked up under.

### Changed

- The `/starfish` screen is now the same size as the AUtils menu: a 1018 x 574 pixel panel at its normal size, laid out in window pixels so Minecraft's GUI scale no longer enlarges it. It shrinks only when the window is too small. Fonts use Badlion's px sizes (Roboto 18 for titles, 14 for descriptions, 22 for the header); rows, switches, sliders and buttons were resized to match.
- MDI icons sit closer to the name: a 2 px thin gap replaces the 4 px space before an icon, and the icon is 9 px wide with no extra slack after it. Applies to chat, alerts, the tab list and nametags. (On 26.3 only the gap before the icon changed.)

### Improved

- Smooth scrolling in the `/starfish` screen, using the same ease-out as the AUtils menu: a wheel notch scrolls 144 px, scrolling the other way cancels what is left of the previous notch, and the scrollbar thumb follows the cursor smoothly.
- Much smoother animations in the `/starfish` screen (switches, hover fades, slider). The menu now repaints and re-uploads only the area that changed instead of the whole picture every frame, outlines are drawn as strokes instead of expensive shape subtraction, and text measurement and layout are cached. Measured on a 1018 x 574 menu: a hover or switch frame repaints about a quarter of the image in ~3 ms, a scroll frame takes ~2.5 ms, and a full repaint (page change) ~15 ms.

## 0.2.0

### Added

- MDI Icons setting (Labels): show tags as Material Design icons instead of letters like `[CCC]` in chat, alerts, the tab list and nametags. Off by default.
  - Confirmed Cheater: alert-decagram-outline (purple)
  - Closet Cheater: alert-octagram-outline (gold)
  - Blatant Cheater: alert-decagram (gold)
  - Caution: alert-rhombus-outline (gold)
  - Sniper: alert-decagram (red)
  - Replays Needed: archive-alert-outline (gray)
- Standalone mode for Minecraft 1.8.9: the mod now runs on a plain Ornithe install without OneConfig/OneClient.
  - New settings screen opened with `/starfish`, laid out like Seraph's settings (category sidebar, setting cards with descriptions) and styled like the Lunar Client menu.
  - Settings are saved to `config/urchin.json`.
  - `/urchin check`, `/urchin tag`, `/urchin untag` and the chat buttons work without OneConfig.
  - Hypixel location and party info come from Hypixel's Mod API, connected by Urchin itself when no other mod does.
  - Adventure, the Hypixel Mod API core, hypixel-data and Fabric Language Kotlin are bundled in the jar.
- `/starfish` also opens the settings on OneClient (the OneConfig page).

### Changed

- OneConfig is no longer required on 1.8.9; it is still used whenever it is installed. 26.3 still requires it.

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
- While Starfish's Urchin plugin is running, chat and tab labels skip names it has already badged, so its Bedwars tab tags show without doubling up.
- Works alongside Starfish's Urchin plugin on 1.8.9: the plugin turns off the alerts and labels the mod is showing, and works as usual without the mod.
