# TitleManager

TitleManager is a Bukkit, Spigot, and Paper plugin for customizing the player-facing presentation of your Minecraft server. It supports animated titles, actionbars, tab-list headers and footers, sidebars, announcements, placeholders, gradients, and file-backed animations.

## Links

- Documentation: <https://titlemanager.tarkan.dev>
- Maven repository: <https://repo.tarkan.dev>
- GitHub releases: <https://github.com/Puharesource/TitleManager/releases>
- Hangar: configured by release automation
- SpigotMC resource: <https://www.spigotmc.org/resources/titlemanager.1049/>

## Supported Minecraft versions

TitleManager targets Minecraft `1.8` through the latest configured Paper API version. The current build metadata validates that marketplace metadata includes the configured Paper API version from `gradle.properties`.

## Installation

1. Download the plugin jar from GitHub Releases, Hangar, or SpigotMC.
2. Place the jar in your server's `plugins` directory.
3. Restart the server.
4. Edit the generated files in `plugins/TitleManager/`.
5. Run `/tm reload` after configuration changes.

## Developer API

Add the public Maven repository and compile against the API module:

```kotlin
repositories {
    maven("https://repo.tarkan.dev")
}

dependencies {
    compileOnly("dev.tarkan.titlemanager:modules:bukkit:api:3.0.0-SNAPSHOT")
}
```

The API artifact depends on `dev.tarkan.titlemanager:modules:core`, which is published to the same repository.

Use `TitleManagerServices.get(plugin)` for optional integration or `TitleManagerServices.require(plugin)` when TitleManager is a hard dependency.

See the [2.x to 3.0 migration guide](docs/site/docs/migration-2-to-3.mdx) for package, Maven, configuration, and Minecraft support changes.

## Building locally

```bash
./gradlew check :apps:bukkit-plugin:shadowJar buildDocsSite
```

Useful build outputs:

- Plugin jar: `apps/bukkit-plugin/build/libs/bukkit-plugin-<version>-all.jar`
- Docusaurus docs: `docs/site/build/`
- Vite/Rolldown/Oxc preview entry: `docs/site/vite-dist/`
- Local public Maven repository bundle: `build/maven-repository/`

## Release automation

GitHub Actions is the source of truth for CI/CD:

- PRs run full non-publishing gates.
- `main` publishes `3.0.0-SNAPSHOT` artifacts and a `snapshot-<shortSha>` prerelease.
- Tags publish strict SemVer channels:
  - `vX.Y.Z` → Release
  - `vX.Y.Z-beta.N` → Beta
  - `vX.Y.Z-alpha.N` → Alpha
- Docs deploy to Cloudflare Pages at `titlemanager.tarkan.dev` from `main` after `ENABLE_PUBLISHING=true` is set.
- Maven artifacts deploy to Cloudflare R2 at `repo.tarkan.dev` after `ENABLE_PUBLISHING=true` is set.
- Plugin releases publish to Hangar after `ENABLE_PUBLISHING=true` is set.
- Spigot publishing is represented by a manual guarded workflow path, but upload remains disabled until the SpigotMC upload API/contract is confirmed.

The first alpha should be tagged manually after secrets and Cloudflare/Hangar resources are ready:

```bash
git tag v3.0.0-alpha.1
git push origin v3.0.0-alpha.1
```

## License

TitleManager is licensed under the terms in [LICENSE.md](LICENSE.md). The documentation preview font is bundled from [IdreesInc/Minecraft-Font](https://github.com/IdreesInc/Minecraft-Font) under the SIL Open Font License 1.1.
