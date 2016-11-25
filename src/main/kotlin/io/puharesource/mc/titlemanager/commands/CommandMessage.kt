package io.puharesource.mc.titlemanager.commands

object CommandMessage : TMSubCommand("message",
        aliases = setOf("msg"),
        cmdExecutor = { cmd, sender, args, parameters -> commandExecutor(cmd, sender, args, parameters) {
            if (args.size <= 1) {
                syntaxError()
                return@commandExecutor
            }

            val player = getPlayerAt(0)

            if (player == null) {
                sendConfigMessage("invalid-player", Pair("player", args.first()))
                return@commandExecutor
            }

            val message = getMessageFrom(1)

            if (message.contains("\\n")) {
                val parts = message.split("\\n", limit = 2)

                if (parts[1].isNotBlank()) {
                    if (parts[0].isBlank()) {
                        sendConfigMessage("subtitle-sent", Pair("player", player.name), Pair("subtitle", parts[1]))
                        player.sendSubtitle(parts[1], true)
                    } else {
                        sendConfigMessage("both-sent", Pair("player", player.name), Pair("title", parts[0]), Pair("subtitle", parts[1]))
                        player.sendTitles(parts[0], parts[1], true)
                    }

                    return@commandExecutor
                }
            }

            sendConfigMessage("title-sent", Pair("player", player.name), Pair("title", message))
            player.sendTitle(message, true)
        }})