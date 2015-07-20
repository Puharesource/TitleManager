package io.puharesource.mc.titlemanager.backend.hooks.vault;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.backend.hooks.PluginHook;
import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Logger;

public final class VaultHook extends PluginHook {

    private @Getter @Setter static Economy economy;
    private @Getter @Setter static Permission permissions;
    private @Getter @Setter static boolean economySupported;
    private @Getter @Setter static boolean permissionsSupported;

    public VaultHook() {
        super("Vault");

        Logger logger = TitleManager.getInstance().getLogger();

        if (isEnabled()) {
            if (!setupEconomy())
                logger.warning("There's no economy plugin hooked into vault! Disabling economy based variables.");
            else economySupported = true;
            if (!setupPermissions())
                logger.warning("There's no permissions plugin hooked into vault! Disabling permissions based variables!");
            else permissionsSupported = true;
        } else logger.warning("Vault is not enabled! Disabling permissions and economy based variables!");
    }

    private static boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    private static boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) return false;
        permissions = rsp.getProvider();
        return permissions != null;
    }

    public static boolean hasGroupSupport() {
        return permissions != null && permissions.hasGroupSupport();
    }
}
