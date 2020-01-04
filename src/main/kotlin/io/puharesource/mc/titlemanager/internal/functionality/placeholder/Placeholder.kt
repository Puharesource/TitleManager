package io.puharesource.mc.titlemanager.internal.functionality.placeholder

import org.bukkit.entity.Player

abstract class Placeholder(vararg val aliases: String) {
    open val isEnabled
        get() = true

    abstract fun getText(player: Player, value: String?): String
}

fun createPlaceholder(vararg aliases: String, body: (Player) -> Any): Placeholder {
    return object : Placeholder(*aliases) {
        override fun getText(player: Player, value: String?): String {
            val output = body(player)

            if (output is String) {
                return output
            }

            return output.toString()
        }
    }
}

fun createPlaceholder(vararg aliases: String, enabled: () -> Boolean, body: (Player) -> Any): Placeholder {
    return object : Placeholder(*aliases) {
        override val isEnabled: Boolean
            get() = enabled()

        override fun getText(player: Player, value: String?): String {
            val output = body(player)

            if (output is String) {
                return output
            }

            return output.toString()
        }
    }
}

fun createPlaceholder(vararg aliases: String, body: (Player, String?) -> Any): Placeholder {
    return object : Placeholder(*aliases) {
        override fun getText(player: Player, value: String?): String {
            val output = body(player, value)

            if (output is String) {
                return output
            }

            return output.toString()
        }
    }
}

fun createPlaceholder(vararg aliases: String, enabled: () -> Boolean, body: (Player, String?) -> Any): Placeholder {
    return object : Placeholder(*aliases) {
        override val isEnabled: Boolean
            get() = enabled()

        override fun getText(player: Player, value: String?): String {
            val output = body(player, value)

            if (output is String) {
                return output
            }

            return output.toString()
        }
    }
}
