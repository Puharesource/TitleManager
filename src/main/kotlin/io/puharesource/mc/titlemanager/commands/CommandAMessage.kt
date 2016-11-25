package io.puharesource.mc.titlemanager.commands

object CommandAMessage : TMSubCommand("amessage",
        aliases = setOf("amsg"),
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

            sendConfigMessage("sent", Pair("player", player.name), Pair("title", message))
            player.sendActionbar(message, checkForAnimations = true)
        }})