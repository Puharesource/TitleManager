package io.puharesource.mc.titlemanager.internal.placeholder

import com.earth2me.essentials.Essentials
import de.myzelyam.api.vanish.VanishAPI
import io.puharesource.mc.titlemanager.internal.debug
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.kitteh.vanish.VanishPlugin
import be.maximvdw.placeholderapi.PlaceholderAPI as MvdwPlaceholderApi
import me.clip.placeholderapi.PlaceholderAPI as ClipsPlaceholderApi

object PlaceholderAPIHook : PluginHook("PlaceholderAPI") {
    fun replacePlaceholders(player: Player, text: String): String {
        return try {
            ClipsPlaceholderApi.setPlaceholders(player, text)
        } catch (e: Exception) {
            e.printStackTrace()
            text
        }
    }
}

object MvdwPlaceholderAPIHook : PluginHook("MVdWPlaceholderAPI") {
    fun replacePlaceholders(player: Player, text: String): String {
        return try {
            MvdwPlaceholderApi.replacePlaceholders(player, text)
        } catch (e: Exception) {
            e.printStackTrace()
            text
        }
    }

    fun canReplace(): Boolean {
        return isEnabled() && MvdwPlaceholderApi.getLoadedPlaceholderCount() > 0
    }
}

object EssentialsHook : PluginHook("Essentials") {
    fun isPlayerVanished(player: Player): Boolean {
        return isEnabled() && (getPlugin() as Essentials).getUser(player).isVanished
    }
}

object SuperVanishHook : PluginHook("SuperVanish") {
    fun isPlayerVanished(player: Player): Boolean {
        return isEnabled() && VanishAPI.isInvisible(player)
    }
}

object PremiumVanishHook : PluginHook("PremiumVanish") {
    fun isPlayerVanished(player: Player): Boolean {
        return isEnabled() && VanishAPI.isInvisible(player)
    }
}

object VanishNoPacketHook : PluginHook("VanishNoPacket") {
    fun isPlayerVanished(player: Player): Boolean {
        return isEnabled() && (getPlugin() as VanishPlugin).manager.isVanished(player)
    }
}

object VaultHook : PluginHook("Vault") {
    var economy: Economy? = null
        private set
    var permissions: Permission? = null
        private set

    var isEconomySupported: Boolean = false
        private set
    var isPermissionsSupported: Boolean = false
        private set
    val hasGroupSupport: Boolean
        get() = permissions?.hasGroupSupport() ?: false

    init {
        if (isEnabled()) {
            if (!setupEconomy()) {
                debug("There's no economy plugin hooked into vault! Disabling economy based variables.")
            } else {
                isEconomySupported = true
            }

            if (!setupPermissions()) {
                debug("There's no permissions plugin hooked into vault! Disabling permissions based variables!")
            } else {
                isPermissionsSupported = true
            }
        }
    }

    private fun setupEconomy(): Boolean {
        val rsp = Bukkit.getServer().servicesManager.getRegistration(Economy::class.java) ?: return false
        economy = rsp.provider
        return economy != null
    }

    private fun setupPermissions(): Boolean {
        val rsp = Bukkit.getServer().servicesManager.getRegistration(Permission::class.java) ?: return false
        permissions = rsp.provider
        return permissions != null
    }
}

object CombatLogXHook : PluginHook("CombatLogX") {
    fun isCorrectVersion() = try {
        Class.forName("com.SirBlobman.combatlogx.api.event.PlayerTagEvent")

        true
    } catch (e: ClassNotFoundException) {
        false
    }
}
