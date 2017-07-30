package io.puharesource.mc.titlemanager.commands

import io.puharesource.mc.titlemanager.APIProvider.toAnimationParts
import io.puharesource.mc.titlemanager.APIProvider.toScoreboardTitleAnimation
import io.puharesource.mc.titlemanager.APIProvider.toScoreboardValueAnimation
import io.puharesource.mc.titlemanager.extensions.color
import io.puharesource.mc.titlemanager.extensions.giveScoreboard
import io.puharesource.mc.titlemanager.extensions.hasScoreboard
import io.puharesource.mc.titlemanager.extensions.removeScoreboard
import io.puharesource.mc.titlemanager.pluginInstance
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
                val scoreboardSection = pluginInstance.config.getConfigurationSection("scoreboard")
                val title = toAnimationParts(scoreboardSection.getString("title").color())
                val lines = scoreboardSection.getStringList("lines").take(15).map { toAnimationParts(it.color()) }

                player.giveScoreboard()
                toScoreboardTitleAnimation(title, player, true).start()

                lines.forEachIndexed { index, parts ->
                    toScoreboardValueAnimation(parts, player, index + 1, true).start()
                }

                sendConfigMessage("toggled-on")
            }
        }})