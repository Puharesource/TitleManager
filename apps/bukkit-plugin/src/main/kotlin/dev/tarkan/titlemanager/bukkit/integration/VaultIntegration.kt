package dev.tarkan.titlemanager.bukkit.integration

import dev.tarkan.titlemanager.bukkit.configuration.PlaceholderConfiguration
import dev.tarkan.titlemanager.bukkit.extensions.color
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.permission.Permission
import org.bukkit.entity.Player
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class VaultIntegration(
    private val plugin: JavaPlugin,
    placeholderConfiguration: PlaceholderConfiguration
) {
    private val decimalFormat = DecimalFormat(
        placeholderConfiguration.numberFormat.format,
        DecimalFormatSymbols(Locale.forLanguageTag(placeholderConfiguration.locale))
    )
    private val isNumberFormatEnabled = placeholderConfiguration.numberFormat.enabled

    fun balance(player: Player): String {
        val economy = serviceProvider(Economy::class.java)?.provider ?: return "no-econ"

        return economy.getBalance(player).formatBalance()
    }

    fun group(player: Player): String {
        val permission = serviceProvider(Permission::class.java)?.provider ?: return "no-perms"
        if (!permission.hasGroupSupport()) {
            return "no-perms"
        }

        return permission.getPrimaryGroup(player).color()
    }

    private fun Double.formatBalance(): String {
        if (!isNumberFormatEnabled) {
            return toString()
        }

        return decimalFormat.format(BigDecimal(this))
    }

    private fun <T> serviceProvider(service: Class<T>): RegisteredServiceProvider<T>? {
        return plugin.server.servicesManager.getRegistration(service)
    }
}
