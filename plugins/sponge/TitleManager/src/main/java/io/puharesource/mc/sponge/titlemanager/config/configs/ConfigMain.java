package io.puharesource.mc.sponge.titlemanager.config.configs;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.config.Config;
import io.puharesource.mc.sponge.titlemanager.config.ConfigField;
import org.spongepowered.api.config.DefaultConfig;

import java.nio.file.Path;
import java.util.Set;

public final class ConfigMain implements Config {
    @Inject @DefaultConfig(sharedRoot = false) private Path mainConfigPath;
    @Override public Path getConfigPath() { return mainConfigPath; }

    /*
     * Root section
     * ------------
     */
    @ConfigField(path = "usingConfig")
    public boolean usingConfig = true;

    @ConfigField(path = "config-version")
    public int configVersion = 3;

    @ConfigField(path = "using-bungeecord")
    public boolean usingBungeecord = false;

    @ConfigField(path = "legacy-client-support")
    public boolean legacyClientSupport = false;

    /*
     * Updater section
     * ---------------
     */
    @ConfigField(path = "updater.check-automatically")
    public boolean updaterAutoCheck = true;

    /*
     * Tablist section
     * ---------------
     */
    @ConfigField(path = "tablist.enabled")
    public boolean tablistEnabled = true;

    @ConfigField(path = "tablist.header")
    public String tablistHeader = "&3MyServer\\n&bSecond line!\\n&fThird line!";

    @ConfigField(path = "tablist.footer")
    public String tablistFooter = "animation:left-to-right";

    /*
     * welcome-message section
     * -----------------------
     */
    @ConfigField(path = "welcome-title.enabled")
    public boolean welcomeMessageEnabled = true;

    @ConfigField(path = "welcome-title.title")
    public Object welcomeMessageTitle = "&7Welcome back &a{PLAYER}&7!";

    @ConfigField(path = "welcome-title.subtitle")
    public String welcomeMessageSubtitle = "&7To my server";

    @ConfigField(path = "welcome-title.mode")
    public String welcomeMessageMode = "NONE";

    @ConfigField(path = "welcome-title.fadeIn")
    public int welcomeMessageFadeIn = 20;

    @ConfigField(path = "welcome-title.stay")
    public int welcomeMessageStay = 40;

    @ConfigField(path = "welcome-title.fadeOut")
    public int welcomeMessageFadeOut = 20;

    /*
     * New player welcome-message section
     * ----------------------------------
     */
    @ConfigField(path = "welcome-title.first-join.title")
    public Object firstJoinTitle = "&7Welcome to MyServer &a{PLAYER}";

    @ConfigField(path = "welcome-title.first-join.subtitle")
    public String firstJoinSubtitle = "&7Hope you enjoy your stay!";

    /*
     * New player actionbar-welcome section
     * ------------------------------------
     */
    @ConfigField(path = "welcome-actionbar.enabled")
    public boolean actionbarWelcomeEnabled = true;

    @ConfigField(path = "welcome-actionbar.message")
    public Object actionbarWelcomeMessage = "&a&lWelcome!";

    @ConfigField(path = "welcome-actionbar.first-join.message")
    public Object actionbarFirstWelcomeMessage = "&2&lWelcome It's your first time!";

    /*
     * Number format
     * -------------
     */
    @ConfigField(path = "number-format.enabled")
    public boolean numberFormatEnabled = true;

    @ConfigField(path = "number-format.format")
    public String numberFormat = "#,###.##";

    /*
     * Date format
     * -----------
     */
    @ConfigField(path = "date-format.format")
    public String dateFormat = "EEE, dd MMM yyyy HH:mm:ss z";

    /*
     * Disabled variables
     * ------------------
     */
    @ConfigField(path = "disabled-variables")
    public Set<String> disabledVariables = Sets.newHashSet("my-disabled-variable", "also-called-placeholders");
    
    /*
     * Per World Messages
     * ------------------
     */
    @ConfigField(path = "world-message.enabled")
    public boolean worldMessageEnabled = true;

    @ConfigField(path = "world-message.title")
    public Object worldMessageTitle = "&7You have joined";

    @ConfigField(path = "world-message.subtitle")
    public String worldMessageSubtitle = "&7the {WORLD} world!";

    @ConfigField(path = "world-message.mode")
    public String worldMessageMode = "NONE";
    
    @ConfigField(path = "world-message.actionbar")
    public Object worldMessageActionBar = "Enjoy your stay!";

    @ConfigField(path = "world-message.fadeIn")
    public int worldMessageFadeIn = 20;

    @ConfigField(path = "world-message.stay")
    public int worldMessageStay = 40;

    @ConfigField(path = "world-message.fadeOut")
    public int worldMessageFadeOut = 20;
}
