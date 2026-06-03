package dev.tarkan.titlemanager.bukkit.command

import de.comahe.i18n4k.messages.MessageBundleLocalizedString
import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

abstract class BroadcastSubCommand(
    private val plugin: TitleManagerPlugin,
    name: String,
    vararg aliases: String,
    description: MessageBundleLocalizedString,
    permission: String? = null
) : TitleManagerSubCommand(name, *aliases, description = description, permission = permission) {

    protected fun getRecipients(sender: CommandSender, parameters: CommandParameters): Collection<Player> {
        val radius = parameters.getValue("radius")?.toDoubleOrNull()
        if (radius == null) {
            val worldParameter = parameters["world"] ?: return plugin.server.onlinePlayers

            if (worldParameter.value == null) {
                if (sender !is Player) {
                    return plugin.server.onlinePlayers
                }

                return sender.world.players
            }

            val world = plugin.server.getWorld(worldParameter.value) ?: return emptyList()

            return world.players
        }

        if (sender !is Player) {
            return plugin.server.onlinePlayers
        }

        return getRecipientsWithinRadius(sender, radius)
    }

    private fun getRecipientsWithinRadius(sender: Player, radius: Double): Collection<Player> {
        return sender.getNearbyEntities(radius, radius, radius)
            .filterIsInstance<Player>()
            .toMutableSet()
            .also { it.add(sender) }
    }
}