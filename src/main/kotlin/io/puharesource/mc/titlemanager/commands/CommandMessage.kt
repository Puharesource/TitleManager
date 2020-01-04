package io.puharesource.mc.titlemanager.commands

import io.puharesource.mc.titlemanager.APIProvider

object CommandMessage : TMSubCommand("message",
        aliases = setOf("msg"),
        cmdExecutor = { cmd, sender, args, parameters -> commandExecutor(cmd, sender, args, parameters) {
            if (args.size <= 1) {
                syntaxError()
                return@commandExecutor
            }

            val player = getPlayerAt(0)

            if (player == null) {
                sendConfigMessage("invalid-player", "player" to args.first())
                return@commandExecutor
            }

            val message = getMessageFrom(1)

            if (message.contains(APIProvider.commandSplitPattern)) {
                val parts = message.split(APIProvider.commandSplitPattern, limit = 2)

                if (parts[1].isNotBlank()) {
                    if (parts[0].isBlank()) {
                        sendConfigMessage("subtitle-sent", "player" to player.name, "subtitle" to parts[1])
                        player.sendSubtitle(parts[1], true)
                    } else {
                        sendConfigMessage("both-sent", "player" to player.name, "title" to parts[0], "subtitle" to parts[1])
                        player.sendTitleAndSubtitle(parts[0], parts[1], true)
                    }

                    return@commandExecutor
                }
            }

            sendConfigMessage("title-sent", "player" to player.name, "title" to message)
            player.sendTitle(message, true)
        }})