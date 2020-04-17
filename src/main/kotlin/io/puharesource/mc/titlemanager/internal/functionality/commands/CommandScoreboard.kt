package io.puharesource.mc.titlemanager.internal.functionality.commands

import io.puharesource.mc.titlemanager.internal.APIProvider.toAnimationParts
import io.puharesource.mc.titlemanager.internal.APIProvider.toScoreboardTitleAnimation
import io.puharesource.mc.titlemanager.internal.APIProvider.toScoreboardValueAnimation
import io.puharesource.mc.titlemanager.internal.extensions.giveScoreboard
import io.puharesource.mc.titlemanager.internal.extensions.hasScoreboard
import io.puharesource.mc.titlemanager.internal.extensions.removeScoreboard
import io.puharesource.mc.titlemanager.internal.pluginConfig
import io.puharesource.mc.titlemanager.internal.pluginInstance
import org.bukkit.entity.Player

object CommandScoreboard : TMSubCommand("scoreboard",
        aliases = setOf("sb"),
        allowedSender = AllowedCommandSender.PLAYER,
        cmdExecutor = { cmd, sender, args, parameters -> commandExecutor(cmd, sender, args, parameters) {
            if (args.isEmpty() || !args.first().equals("toggle", ignoreCase = true)) {
                syntaxError()
                return@commandExecutor
            }

            val player = sender as Player

            pluginInstance.playerInfoDB!!.setScoreboardToggled(player, !player.hasScoreboard())

            if (player.hasScoreboard()) {
                player.removeScoreboard()

                sendConfigMessage("toggled-off")
            } else {
                val title = toAnimationParts(pluginConfig.scoreboard.title)
                val lines = pluginConfig.scoreboard.lines.map { toAnimationParts(it) }

                player.giveScoreboard()
                toScoreboardTitleAnimation(title, player, true).start()

                lines.forEachIndexed { index, parts ->
                    toScoreboardValueAnimation(parts, player, index + 1, true).start()
                }

                sendConfigMessage("toggled-on")
            }
        }})