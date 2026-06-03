<p align="center">
  <img src="docs/site/static/img/titlemanager_logo.svg" alt="TitleManager" width="160" />
</p>

<h1 align="center">TitleManager</h1>

<p align="center">
  Animated titles, actionbars, tab lists, scoreboards, gradients, and placeholders for Bukkit, Spigot, and Paper servers.
</p>

<p align="center">
  <a href="https://titlemanager.tarkan.dev"><img alt="Documentation" src="https://img.shields.io/badge/docs-titlemanager.tarkan.dev-2f81f7?style=for-the-badge" /></a>
  <a href="https://github.com/Puharesource/TitleManager/actions/workflows/ci.yml"><img alt="CI" src="https://img.shields.io/github/actions/workflow/status/Puharesource/TitleManager/ci.yml?branch=main&amp;style=for-the-badge&amp;label=CI" /></a>
  <a href="https://github.com/Puharesource/TitleManager/releases"><img alt="Releases" src="https://img.shields.io/github/v/release/Puharesource/TitleManager?include_prereleases&amp;style=for-the-badge" /></a>
  <a href="LICENSE.md"><img alt="License" src="https://img.shields.io/badge/license-MIT-green?style=for-the-badge" /></a>
</p>

---

TitleManager is the presentation layer for Minecraft servers that care about polish. Welcome players with animated titles, keep the tab list alive with gradients and placeholders, publish announcements without command-block gymnastics, and show a clean sidebar that updates without spamming duplicate packets.

The 3.x line is a modern rebuild of the classic TitleManager plugin: a safer configuration model, a shared animation engine for the plugin and web previews, explicit runtime support, generated API docs, and CI/CD-ready releases.

## Why server owners use it

- **First impressions that move**: configurable join titles, subtitles, actionbars, timings, and first-join variants.
- **A richer TAB screen**: animated player-list headers and footers with online counts, world time, server time, gradients, and custom animation files.
- **Clean sidebars**: up to 15 animated scoreboard lines backed by Bukkit scoreboard output.
- **Broadcasts and announcements**: send titles or actionbars on demand, or schedule rotating announcements from config.
- **Placeholder-friendly**: use TitleManager placeholders, PlaceholderAPI, Vault values, and BungeeCord-aware data when those integrations are installed.
- **Safe reloads**: `/tm reload` is transactional; if the new config is broken, the previous running config stays active.
- **Diagnostics when it matters**: `/tm diagnostics` reports runtime selection, integrations, animation health, and safe-mode failures.

## Supported servers

TitleManager currently targets Minecraft **1.8 through 1.21.11**.

- Minecraft 1.17+ prefers stable Bukkit/Paper APIs.
- Minecraft 1.8 through 1.16 uses explicit per-version legacy NMS modules only where the API cannot cover the feature.
- Minecraft 1.7 is intentionally no longer supported in the 3.x line.

See the [runtime support matrix](https://titlemanager.tarkan.dev/runtime-support) for the exact behavior by server version.

## Install

1. Download the plugin jar from [GitHub Releases](https://github.com/Puharesource/TitleManager/releases), Hangar, or the existing [SpigotMC resource page](https://www.spigotmc.org/resources/titlemanager.1049/).
2. Place the jar in your server's `plugins/` directory.
3. Restart the server.
4. Edit the generated files in `plugins/TitleManager/`.
5. Run `/tm reload`.

Fresh installs include default configs and animation files, so the default player-list footer works immediately.

## Configuration preview

```yaml
# plugins/TitleManager/player-list.yml
enabled: true
updateIntervalMilliseconds: 50
header: |-

  ${shine:[0;2;0][0;25;0][0;25;0][&3;&b]My Server}
  &r
footer: |-

  &7World time: &b%{12h-world-time}
  &7Server time: &b%{server-time}


  ${right-to-left} &b%{online}&7/&b%{max} &7Online Players ${left-to-right}
```

The docs site includes local animation previews and a playground, so you can tune animation timing before putting a config on a live server:

- [Animation playground](https://titlemanager.tarkan.dev/animation-playground)
- [Configuration guide](https://titlemanager.tarkan.dev/configuration)
- [Built-in animation examples](https://titlemanager.tarkan.dev/built-in-animation-examples)

## Commands

| Command | Purpose |
| --- | --- |
| `/tm version` | Show the running TitleManager version and update status. |
| `/tm reload` | Reload configuration transactionally. |
| `/tm diagnostics` | Inspect runtime modules, integrations, and safe-mode details. |
| `/tm animations` | List registered animation placeholders. |
| `/tm scripts` | Check legacy script support. |
| `/tm title ...` | Send or broadcast title messages. |
| `/tm actionbar ...` | Send or broadcast actionbar messages. |
| `/tm scoreboard toggle` | Toggle your sidebar. |
| `/tm playerlist toggle` | Toggle your player-list customization. |

## Developer API

TitleManager publishes a small Bukkit-facing API plus the shared animation core.

```kotlin
repositories {
    maven("https://repo.tarkan.dev")
}

dependencies {
    compileOnly("dev.tarkan.titlemanager:titlemanager-bukkit-api:3.0.0-SNAPSHOT")
}
```

Use `TitleManagerServices.get(plugin)` for optional integrations, or `TitleManagerServices.require(plugin)` when TitleManager is a hard dependency.

API references are generated with both JavaDoc and Dokka:

- [API overview](https://titlemanager.tarkan.dev/api-reference)
- [JavaDoc](https://titlemanager.tarkan.dev/javadoc)
- [Dokka Kotlin docs](https://titlemanager.tarkan.dev/dokka)

For migration details, see the [2.x to 3.0 migration guide](https://titlemanager.tarkan.dev/migration-2-to-3).

## Build locally

```bash
./gradlew check buildDocsSite :apps:bukkit-plugin:shadowJar prepareReleaseAssets
```

Useful outputs:

- Plugin jar: `apps/bukkit-plugin/build/libs/bukkit-plugin-<version>-all.jar`
- Documentation site: `docs/site/build/`
- Browser preview bundle: `docs/site/vite-dist/`
- Local public Maven repository bundle: `build/maven-repository/`

## Project layout

```text
apps/bukkit-plugin          Bukkit/Paper plugin
apps/web-viewer             Local animation web viewer
modules/core                Shared animation parser and timeline engine
modules/bukkit/api          Public Bukkit API
modules/bukkit/defaults     Default config and animation resources
modules/bukkit/runtime-*    Runtime contracts and Bukkit API adapter
modules/nms                 Versioned NMS adapters for legacy gaps
docs/site                   Docusaurus documentation site
```

## Release channels

- `main` publishes `3.0.0-SNAPSHOT` artifacts when repository publishing is enabled.
- `vX.Y.Z-alpha.N` publishes an Alpha release.
- `vX.Y.Z-beta.N` publishes a Beta release.
- `vX.Y.Z` publishes a stable release.

Publishing is intentionally gated by repository configuration so the imported `main` branch can be validated before public deploys are switched on.

## License

TitleManager is licensed under the terms in [LICENSE.md](LICENSE.md). The documentation preview font is bundled from [IdreesInc/Minecraft-Font](https://github.com/IdreesInc/Minecraft-Font) under the SIL Open Font License 1.1.
