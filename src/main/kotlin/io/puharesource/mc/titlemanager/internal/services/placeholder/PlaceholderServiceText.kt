package io.puharesource.mc.titlemanager.internal.services.placeholder

import io.puharesource.mc.titlemanager.TitleManagerPlugin
import io.puharesource.mc.titlemanager.internal.config.TMConfigMain
import io.puharesource.mc.titlemanager.internal.extensions.color
import io.puharesource.mc.titlemanager.internal.extensions.format
import io.puharesource.mc.titlemanager.internal.extensions.getFormattedTime
import io.puharesource.mc.titlemanager.internal.extensions.isInt
import io.puharesource.mc.titlemanager.internal.extensions.stripColor
import io.puharesource.mc.titlemanager.internal.placeholder.MvdwPlaceholderAPIHook
import io.puharesource.mc.titlemanager.internal.placeholder.Placeholder
import io.puharesource.mc.titlemanager.internal.placeholder.PlaceholderAPIHook
import io.puharesource.mc.titlemanager.internal.placeholder.PlaceholderTps
import io.puharesource.mc.titlemanager.internal.placeholder.VanishHookReplacer
import io.puharesource.mc.titlemanager.internal.placeholder.VaultHook
import io.puharesource.mc.titlemanager.internal.reflections.getPing
import io.puharesource.mc.titlemanager.internal.services.bungeecord.BungeeCordService
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.Date
import java.util.concurrent.ConcurrentSkipListMap
import javax.inject.Inject

class PlaceholderServiceText @Inject constructor(
    private val plugin: TitleManagerPlugin,
    private val config: TMConfigMain,
    private val bungeeCordService: BungeeCordService
) : PlaceholderService {
    private val variablePattern = """[%][{](([^}:]+\b)(?:[:]((?:(?>[^}\\]+)|\\.)+))?)[}]""".toRegex()
    private val placeholderReplacers: MutableMap<String, Placeholder> = ConcurrentSkipListMap(String.CASE_INSENSITIVE_ORDER)

    override fun loadBuiltinPlaceholders() {
        addPlaceholder(createPlaceholder("player", "username", "name") { player -> player.name })
        addPlaceholder(createPlaceholder("displayname", "display-name", "nickname", "nick") { player -> player.displayName })
        addPlaceholder(createPlaceholder("strippeddisplayname", "stripped-displayname", "stripped-nickname", "stripped-nick") { player -> player.displayName.stripColor() })
        addPlaceholder(createPlaceholder("world", "world-name") { player -> player.world.name })
        addPlaceholder(createPlaceholder("world-time") { player -> player.world.time })
        addPlaceholder(createPlaceholder("24h-world-time") { player -> player.world.getFormattedTime(true) })
        addPlaceholder(createPlaceholder("12h-world-time") { player -> player.world.getFormattedTime(false) })
        addPlaceholder(createPlaceholder("online", "online-players") { _, value ->
            if (value == null || !config.usingBungeecord) {
                return@createPlaceholder plugin.server.onlinePlayers.size
            }

            if (value.contains(",")) {
                return@createPlaceholder value.split(",").asSequence().mapNotNull { bungeeCordService.servers[value]?.playerCount }.sum().toString()
            }

            return@createPlaceholder bungeeCordService.servers[value]?.playerCount?.toString() ?: ""
        })
        addPlaceholder(createPlaceholder("max", "max-players") { _ -> Bukkit.getServer().maxPlayers })
        addPlaceholder(createPlaceholder("world-players", "world-online") { player -> player.world.players.size })
        addPlaceholder(createPlaceholder("ping") { player -> player.getPing() })
        addPlaceholder(createPlaceholder("tps") { _, value ->
            if (value == null) {
                return@createPlaceholder PlaceholderTps.getTps(1)
            }

            if (value.isInt()) {
                return@createPlaceholder PlaceholderTps.getTps(value.toInt())
            }

            return@createPlaceholder PlaceholderTps.getTps(value)
        }.cached(30))
        addPlaceholder(createPlaceholder("server-time") { _ -> config.placeholders.dateFormat.format(Date(System.currentTimeMillis())) })
        addPlaceholder(createPlaceholder("bungeecord-online", "bungeecord-online-players", enabled = { config.usingBungeecord }) { _ -> bungeeCordService.onlinePlayers }.cached(5))
        addPlaceholder(createPlaceholder("server", "server-name", enabled = { config.usingBungeecord }) { _ -> bungeeCordService.currentServer.orEmpty() })
        addPlaceholder(createPlaceholder("safe-online", "safe-online-players", enabled = { VanishHookReplacer.isValid() }) { player -> VanishHookReplacer.value(player) })
        addPlaceholder(createPlaceholder("balance", "money", enabled = { VaultHook.isEnabled() && VaultHook.isEconomySupported }) { player -> VaultHook.economy!!.getBalance(player).format() })
        addPlaceholder(createPlaceholder("group", "group-name", enabled = { VaultHook.isEnabled() && VaultHook.hasGroupSupport }) { player -> VaultHook.permissions!!.getPrimaryGroup(player).color() })
    }

    override fun addPlaceholder(placeholder: Placeholder) {
        placeholder.aliases.forEach { placeholderReplacers[it] = placeholder }
    }

    override fun replaceText(player: Player, text: String): String {
        var replacedText = text

        if (containsPlaceholders(text)) {
            val matcher = variablePattern.toPattern().matcher(text)

            while (matcher.find()) {
                val placeholderName = matcher.group(2)
                val parameter: String? = if (matcher.groupCount() == 3) matcher.group(3)?.replace("\\}", "}") else null
                val placeholder = placeholderReplacers[placeholderName]

                placeholder?.getText(player, parameter)?.let {
                    replacedText = replacedText.replace(matcher.group(), it)
                }
            }
        }

        replacedText = replaceTextFromHooks(player, replacedText)

        return replacedText
    }

    override fun containsPlaceholders(text: String) = text.contains(variablePattern)

    override fun containsPlaceholder(text: String, placeholder: String) = text.contains("%{$placeholder}", ignoreCase = true)

    private fun replaceTextFromHooks(player: Player, text: String): String {
        var replacedText = text

        if (PlaceholderAPIHook.isEnabled()) {
            replacedText = PlaceholderAPIHook.replacePlaceholders(player, replacedText)
        }

        if (MvdwPlaceholderAPIHook.canReplace()) {
            replacedText = MvdwPlaceholderAPIHook.replacePlaceholders(player, replacedText)
        }

        return replacedText
    }

    private fun createPlaceholder(vararg aliases: String, body: (Player) -> Any): Placeholder {
        return object : Placeholder(*aliases) {
            override fun getText(player: Player, value: String?) = processOutput(body(player))
        }
    }

    private fun createPlaceholder(vararg aliases: String, enabled: () -> Boolean, body: (Player) -> Any): Placeholder {
        return object : Placeholder(*aliases) {
            override val isEnabled: Boolean
                get() = enabled()

            override fun getText(player: Player, value: String?) = processOutput(body(player))
        }
    }

    private fun createPlaceholder(vararg aliases: String, body: (Player, String?) -> Any): Placeholder {
        return object : Placeholder(*aliases) {
            override fun getText(player: Player, value: String?) = processOutput(body(player, value))
        }
    }

    private fun createPlaceholder(vararg aliases: String, enabled: () -> Boolean, body: (Player, String?) -> Any): Placeholder {
        return object : Placeholder(*aliases) {
            override val isEnabled: Boolean
                get() = enabled()

            override fun getText(player: Player, value: String?) = processOutput(body(player, value))
        }
    }
}
