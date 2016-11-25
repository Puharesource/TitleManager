package io.puharesource.mc.titlemanager.commands

object CommandBroadcast : TMSubCommand("broadcast",
        aliases = setOf("bc"),
        cmdExecutor = { cmd, sender, args, parameters -> commandExecutor(cmd, sender, args, parameters) {
            if (args.isEmpty()) {
                syntaxError()
                return@commandExecutor
            }

            if (message.contains("\\n")) {
                val parts = message.split("\\n", limit = 2)

                val title = parts[0]
                val subtitle = parts[1]

                if (subtitle.isNotBlank()) {
                    if (title.isBlank()) {
                        sendConfigMessage("subtitle-sent", Pair("subtitle", subtitle))
                        broadcastSubtitle(subtitle)
                    } else {
                        sendConfigMessage("both-sent", Pair("title", title), Pair("subtitle", subtitle))
                        broadcastTitles(title, subtitle)
                    }

                    return@commandExecutor
                }
            }

            sendConfigMessage("title-sent", Pair("title", message))
            broadcastTitle(message)
        }})