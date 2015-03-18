package io.puharesource.mc.titlemanager.backend.hooks

import io.puharesource.mc.titlemanager.TitleManager
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.plugin.RegisteredServiceProvider

final class VaultHook extends PluginHook {
    static Economy economy
    static Permission permissions
    static boolean economySupported
    static boolean permissionsSupported

    VaultHook() {
        super("Vault")

        def warn = {String warning -> TitleManager.getInstance().getLogger().warning(warning)}

        if (isEnabled()) {
            if (!setupEconomy())
                warn("There's no economy plugin hooked into vault! Disabling economy based variables.")
            else economySupported = true
            if (!setupPermissions())
                warn("There's no permissions plugin hooked into vault! Disabling permissions based variables!")
            else permissionsSupported = true
        } else warn("Vault is not enabled! Disabling permissions and economy based variables!")
    }

    private static boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class)
        if (rsp == null) return false
        economy = rsp.getProvider()
        return economy != null
    }

    private static boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class)
        if (rsp == null) return false
        permissions = rsp.getProvider()
        return permissions != null
    }
}
