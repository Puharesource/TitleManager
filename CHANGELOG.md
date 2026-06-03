# Changelog

All notable changes to TitleManager are documented in this file.

## 2.3.6 (2021-12-18)

### Fixed

- Ping placeholder for Minecraft 1.17 and 1.18

## 2.3.5 (2021-12-17)

### Added

- Minecraft 1.18 support

### Fixed

- Backwards compatibility
- Ping placeholder

## 2.3.4 (2021-06-21)

### Added

- Minecraft 1.17 support
- Delay option in the config for welcome titles and actionbar messages

### Changed

- Removed most NMS code for future Minecraft releases to improve compatibility with new versions

## 2.3.3 (2021-03-20)

### Fixed

- TPS placeholder for Paper servers

## 2.3.2 (2021-03-20)

### Added

- Configuration option to disable the CombatLogX hook (PR #302 by @SirBlobman)

### Changed

- Switched to TagEvent instead of cancellable PreTagEvent (PR #302 by @SirBlobman)
- CombatLogX hook will not enable if the Notifier expansion has disabled scoreboard (PR #302 by @SirBlobman)

### Fixed

- BungeeCord support (7e1f576 by @Puharesource)
- Method call in API (PR #301 by @jestkiytipok)

## 2.3.1 (2020-07-27)

### Fixed

- Scoreboard RGB colors
- Actionbar RGB colors

### Improved

- RGB gradients
- Removed scoreboard text length limit

## 2.3.0 (2020-07-11)

### Added

- (Minecraft 1.16+) HEX color support
- (Minecraft 1.16+) New placeholder: %{c:#ffff00}My HEX colored text
- (Minecraft 1.16+) New animation scripts:
    - `${gradient:[#ff0000,#00ff00,#0000ff,#ff0000]My HEX gradient text}` - Creates a gradient with the given text and
      HEX colors
    - `${gradient_color:[#ff0000,#00ff00,#0000ff]}My HEX colored text}` - Creates a single color that fades through the
      given colors

### Fixed

- Messages not showing when using /tm bc and /tm msg
- Compatibility with Java versions above 8

## 2.2.7 (2020-04-24)

### Fixed

- /tm message command
- Scoreboard when switching worlds
- Tab completion

## 2.2.6 (2020-04-22)

### Fixed

- CombatLogX 10.0.0.0 support

## 2.2.5 (2020-04-22)

### Fixed

- CombatLogX 10.0.0.0 support

## 2.2.4 (2020-04-22)

### Fixed

- Scoreboard placeholders not updating
- Version checker
- Vault support

## 2.2.3 (2020-04-22)

### Fixed

- Welcome message
- Scoreboards showing when feature disabled

## 2.2.2 (2020-04-22)

### Changed

- Reduced updater version polling rate from 10 minutes to 5 hours

### Fixed

- Header and footer without animations
- CombatLogX support (works with version 10.X.X.X-Beta and above)

## 2.2.1 (2020-04-21)

### Fixed

- /tm sb toggle command

## 2.2.0 (2020-04-21)

### Added

- Disabled worlds for Scoreboard
- CombatLogX support (scoreboard toggles off when user is tagged)
- New API methods for default Scoreboard and sending processed titles, scoreboards, and player list headers/footers

### Changed

- Internal rewrite and refactoring
- Added Dagger 2 for dependency injection

### Removed

- Deprecated v1 API

## 2.1.7 (2020-04-17)

### Added

- Reintroduced animation generator at https://tarkan.dev/tmgenerator

### Fixed

- Various bugs

## 2.1.6 (2020-01-07)

### Added

- Minecraft 1.15.1 support

### Changed

- Added caching for certain placeholders
- Reduced JAR size (Removed RXJava)

### Fixed

- Issues: #242, #233, #178, #244, #231

## 2.1.5 (2019-06-19)

### Fixed

- Header and footer not showing when relogging

### Improved

- Performance optimizations

## 2.1.4 (2019-06-12)

### Changed

- Updated dependencies

### Fixed

- Stability issues
- Various bugs

## 2.1.3 (2018-08-26)

### Added

- Minecraft 1.13.1 support

## 2.1.2 (2018-08-12)

### Fixed

- Memory leak

## 2.1.1 (2018-07-26)

### Changed

- Increased scoreboard title text length limit from 32 to unlimited for Minecraft 1.13+
- Performance improvements

## 2.1.0 (2018-07-22)

### Added

- Minecraft 1.13 support

### Changed

- Scoreboard numbers now start at 1

## 2.0.9 (2017-08-13)

### Added

- Repeat script for increased actionbar message stay time: `${repeat:[stay]My message}` where stay is in ticks (20
  ticks = 1 second)

### Changed

- Updated update checker to the new Spigot web API

### Fixed

- Minecraft 1.7 client support
- Announcer animations

## 2.0.8 (2017-07-30)

### Added

- `/tm sb toggle` command to toggle scoreboard (Permission: `titlemanager.command.scoreboard`)
- MVdWPlaceholderAPI support
- Player list speed options
- Scoreboard speed options
- Option to prevent duplicate packets
- Locale for time and future placeholders

### Changed

- Updated Kotlin to kotlin-stdlib-jre8 version 1.1.3-2

### Fixed

- BungeeCord online players duplication
- Scoreboard toggle command
- `removeScoreboard` method

## 2.0.7 (2017-06-01)

### Added

- Minecraft 1.12 support

### Fixed

- FactionsFly compatibility
- Various bugs

## 2.0.6 (2017-01-19)

### Fixed

- Subtitle animations

## 2.0.5 (2017-01-18)

### Fixed

- Issue with players randomly unable to join when scoreboard enabled

## 2.0.4 (2017-01-18)

### Fixed

- Removed debug messages when `using-bungeecord` enabled

## 2.0.3 (2017-01-17)

### Fixed

- Vault hook
- Scoreboards with BungeeCord
- No permission message
- `%{bungeecord-online}` placeholder

## 2.0.2 (2017-01-17)

### Fixed

- Update checker falsely reporting updates

## 2.0.1 (2017-01-17)

### Fixed

- Minecraft 1.8 support

## 2.0.0 (2017-01-17)

### Added

- Multiple animations per line
- Scoreboard sidebar (similar to Featherboard)
- `-radius` and `-world` parameters

### Changed

- Smoother animations
- New API for developers (old API still supported)
- Placeholder syntax changed from `{my-variable}` to `%{my-variable}`
- Animation syntax changed from `animation:my-animation` to `${my-animation}`
- More descriptive config with comments (auto-converts old config)

## 1.5.13 (2016-11-17)

### Added

- Minecraft 1.11 support

## 1.5.12 (2016-06-13)

### Added

- Minecraft 1.10 support

### Fixed

- Error spam when using BungeeCord

## 1.5.11 (2015-07-30)

### Added

- SuperVanish and PremiumVanish support

### Fixed

- Protocol Hack support
- EZRanksLite support

## 1.5.10 (2015-07-24)

### Added

- Update checker toggle in config
- Variable substring support (e.g., `{VARIABLE:5,10}` for substring from 5th to 10th character)
- Semi-world support for `/tm bc` and `/tm abc` with `-WORLD=worldname` parameter

### Fixed

- Animations randomly stopping or lagging during server lag

## 1.5.9 (2015-06-09)

### Fixed

- `{SERVER}` variable

## 1.5.8 (2015-06-08)

### Fixed

- `{BUNGEECORD-ONLINE}` for larger servers and servers with "ALL" global server

## 1.5.7 (2015-06-08)

### Added

- `{SERVER}` variable showing current server's BungeeCord name

### Fixed

- `{BUNGEECORD-ONLINE}` now displays actual network player count

## 1.5.6 (2015-06-07)

### Added

- `{BUNGEECORD-ONLINE}` variable for total network player count

## 1.5.5 (2015-06-07)

### Added

- BungeeCord server player count with `{ONLINE:ServerName}` variable

### Changed

- Optimized variable usage (fixes issues with plugins like iConomy)

## 1.5.4 (2015-04-25)

### Added

- `-silent` parameter for commands to prevent console spam
- `/tm version` command

## 1.5.3 (2015-04-20)

### Added

- Optional dependency for Clip's PlaceholderAPI (500+ placeholders)

## 1.5.2 (2015-04-19)

### Fixed

- Welcome message display

## 1.5.1 (2015-04-19)

### Fixed

- Config generation issue

## 1.5.0 (2015-04-19)

### Added

- Backwards compatibility (Minecraft 1.8.3, 1.8 & 1.7-1.8 Protocol Hack)
- `{PING}` variable
- `{SERVER-TIME}` variable
- EZRanksLite support with `{EZRL.*}` variables
- `{SAFE-ONLINE}` variable for non-vanished players count
- `{WORLD-ONLINE}` variable for per-world player count
- Actionbar message on login

### Changed

- Improved configuration layout

## 1.4.0 (2015-03-07)

### Changed

- Requires Minecraft 1.8.3+

### Fixed

- `{BALANCE}` variable issue

## 1.3.8 (2015-02-13)

### Added

- Developer documentation for API usage

### Changed

- Configurable number format for `{BALANCE}` variable

## 1.3.7 (2015-01-23)

### Fixed

- `/tm msg` now works with animations when first line is empty

## 1.3.6 (2015-01-16)

### Added

- Animation generator at https://puharesource.io/titlemanager-generator.html

### Fixed

- `/tm amsg` animations being sent to everyone

## 1.3.5 (2015-01-15)

### Fixed

- Default configs not included in JAR file

## 1.3.4 (2015-01-15)

### Added

- Auto-updating variables in player list without animations

### Fixed

- Variable broadcasting

## 1.3.3 (2014-12-23)

### Fixed

- Player list not displaying to new players without animations

## 1.3.2 (2014-12-19)

### Fixed

- `{nl}` not working

## 1.3.1 (2014-12-18)

### Added

- Animation support for `\n` in player list
- `{nl}` alias for `<nl>` for multi-line support

### Fixed

- Issues with multiple animations in player list

## 1.3.0 (2014-12-15)

### Added

- Advanced and simple animations
- Separate hovering welcome message for new players
- `{BALANCE}` variable
- Ability to specify fadeIn, stay, and fadeOut when sending commands

## 1.2.1 (2014-11-30)

### Fixed

- Java 7 compatibility issues

## 1.2.0 (2014-11-29)

### Changed

- Updated for Minecraft 1.8
- Architecture changes to prevent future update breakage

## 1.1.2 (2014-10-30)

### Fixed

- Various issues

## 1.1.1 (2014-10-25)

### Fixed

- `/tm abc <message>` only showing first word

## 1.1.0 (2014-10-25)

### Added

- Actionbar title API
- `/tm bc` alias for `/tm broadcast`
- Actionbar broadcast command (`/tm abc` or `/tm abroadcast`)
- Actionbar message command (`/tm amsg`)

### Changed

- Added delay when using `{displayname}` and `{strippeddisplayname}` for Essentials compatibility

## 1.0.10 (2014-10-19)

### Fixed

- `{DISPLAYNAME}` and `{STRIPPEDDISPLAYNAME}` compatibility with Essentials

## 1.0.9 (2014-10-19)

### Added

- `{displayname}` variable for player nickname
- `{strippeddisplayname}` variable for player nickname without colors

## 1.0.8 (2014-10-18)

### Added

- Multi-line support for player list header/footer using `\n`

## 1.0.7 (2014-09-09)

### Added

- Option to disable welcome message
- Option to disable player list header and footer
- Config update from v1.0.6 to v1.0.7

## 1.0.6 (2014-09-08)

### Fixed

- Welcome message showing first joined player's name instead of current joining player

## 1.0.5 (2014-09-07)

### Fixed

- API issues (rawHeader & rawFooter not saved in TabTitleObject constructor)
- `{PLAYER}` variable for subtitle & header/footer

## 1.0.4 (2014-09-07)

### Fixed

- Welcome message
- Config replacement

## 1.0.3 (2014-09-06)

### Added

- `{PLAYER}` variable replacement with player's name

## 1.0.2 (2014-09-06)

### Added

- Fade in, Stay & Fade out for welcome message
- Ability to see broadcast/message that you send

### Fixed

- Minor issues

## 1.0.1 (2014-09-04)

### Added

- `/tm` commands

### Changed

- Complete plugin rewrite

### Fixed

- Various issues
