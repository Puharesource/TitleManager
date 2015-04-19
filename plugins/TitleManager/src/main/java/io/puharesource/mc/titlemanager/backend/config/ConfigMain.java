package io.puharesource.mc.titlemanager.backend.config;

public class ConfigMain {

    /*
     * Root Section
     * ------------
     */
    @ConfigField(path = "usingConfig")
    public boolean usingConfig = true;

    @ConfigField(path = "config-version")
    public int configVersion = 3;


    /*
     * Tabmenu section
     * ---------------
     */
    @ConfigField(path = "tabmenu.enabled")
    public boolean tabmenuEnabled = true;

    @ConfigField(path = "tabmenu.header")
    public String tabmenuHeader = "&3MyServer\\n&bSecond line!\\n&fThird line!";

    @ConfigField(path = "tabmenu.footer")
    public String tabmenuFooter = "animation:test1";

    /*
     * welcome_message section
     * -----------------------
     */
    @ConfigField(path = "welcome_message.enabled")
    public boolean welcomeMessageEnabled = true;

    @ConfigField(path = "welcome_message.title")
    public String welcomeMessageTitle = "&7Welcome back &a{PLAYER}&7!";

    @ConfigField(path = "welcome_message.subtitle")
    public String welcomeMessageSubtitle = "&7To my server";

    @ConfigField(path = "welcome_message.fadeIn")
    public int welcomeMessageFadeIn = 20;

    @ConfigField(path = "welcome_message.stay")
    public int welcomeMessageStay = 40;

    @ConfigField(path = "welcome_message.fadeOut")
    public int welcomeMessageFadeOut = 20;

    /*
     * New player welcome_message section
     * ----------------------------------
     */
    @ConfigField(path = "welcome_message.first-join.title")
    public String firstJoinTitle = "&7Welcome to MyServer &a{PLAYER}";

    @ConfigField(path = "welcome_message.first-join.subtitle")
    public String firstJoinSubtitle = "&7Hope you enjoy your stay!";

    /*
     * New player actionbar-welcome section
     * ----------------------------------
     */
    @ConfigField(path = "actionbar-welcome.enabled")
    public boolean actionbarWelcomeEnabled = true;

    @ConfigField(path = "actionbar-welcome.message")
    public String actionbarWelcomeMessage = "&a&lWelcome!";

    @ConfigField(path = "actionbar-welcome.first-join.message")
    public String actionbarFirstWelcomeMessage = "&2&lWelcome It's your first time!";

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
}
