package io.puharesource.mc.titlemanager.commands

object CommandABroadcast : TMSubCommand("abroadcast",
        aliases = setOf("abc"),
        cmdExecutor = { cmd, sender, args, parameters -> commandExecutor(cmd, sender, args, parameters) {
            if (args.isEmpty()) {
                syntaxError()
                return@commandExecutor
            }

            sendConfigMessage("sent", Pair("title", message))
            broadcastActionbar(message)
        }})