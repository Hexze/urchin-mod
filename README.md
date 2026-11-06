# Urchin

Urchin blacklist integration for OneClient, or standalone on Ornithe 1.8.9

- Get a chat alert when a tagged player joins your game
- See tags next to tagged players in the tab list, above their heads and in chat
- Hover a tag to see why it was added, by who and when, and click it to paste the info in chat
- Check anyone's tags with `/urchin <player>`
- See what Urchin knows about the current server with `/urchin debug` (useful when tags don't show)
- Add and remove tags with `/urchin tag` and `/urchin untag`
- Optionally show tags as Material Design icons instead of `[CCC]`-style letters

Adding and removing tags needs your Minecraft account linked in the [Urchin Discord](https://discord.gg/urchin).

## Supported versions

- Minecraft 1.8.9 on Ornithe.
- Minecraft 26.3 on Fabric.

On OneClient (or anywhere [OneConfig](https://modrinth.com/mod/oneconfig) is installed) the settings are on the OneConfig page.
Without OneConfig, the 1.8.9 build runs standalone: open its settings with `/urchin`. Everything it needs is bundled in the jar.

The 26.3 build needs OneConfig, Fabric Language Kotlin and Fabric API.

## Support

- Report bugs and ask questions on the [Urchin Discord](https://discord.gg/urchin).
