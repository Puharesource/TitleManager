package io.puharesource.mc.titlemanager.commands

import io.puharesource.mc.titlemanager.APIProvider

object CommandBroadcast : TMSubCommand("broadcast",
        aliases = setOf("bc"),
        cmdExecutor = { cmd, sender, args, parameters -> commandExecutor(cmd, sender, args, parameters) {
            if (args.isEmpty()) {
                syntaxError()
                return@commandExecutor
            }

            if (message.contains(APIProvider.commandSplitPattern)) {
                val parts = message.split(APIProvider.commandSplitPattern, limit = 2)

                val title = parts[0]
                val subtitle = parts[1]

                if (subtitle.isNotBlank()) {
                    if (title.isBlank()) {
                        sendConfigMessage("subtitle-sent", "subtitle" to subtitle)
                        broadcastSubtitle(subtitle)
                    } else {
                        sendConfigMessage("both-sent", "title" to title, "subtitle" to subtitle)
                        broadcastTitles(title, subtitle)
                    }

                    return@commandExecutor
                }
            }

            sendConfigMessage("title-sent", "title" to message)
            broadcastTitle(message)
        }})