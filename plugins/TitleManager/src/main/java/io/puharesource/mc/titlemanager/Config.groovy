package io.puharesource.mc.titlemanager
import io.puharesource.mc.titlemanager.api.TitleObject
import io.puharesource.mc.titlemanager.api.animations.AnimationFrame
import io.puharesource.mc.titlemanager.api.animations.FrameSequence
import io.puharesource.mc.titlemanager.api.animations.TabTitleAnimation
import io.puharesource.mc.titlemanager.api.animations.TitleAnimation
import io.puharesource.mc.titlemanager.api.iface.ITabObject
import io.puharesource.mc.titlemanager.api.iface.ITitleObject
import io.puharesource.mc.titlemanager.backend.config.ConfigFile
import io.puharesource.mc.titlemanager.backend.config.ConfigUpdater
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration

class Config {

    private ConfigFile configFile
    private ConfigFile animationConfigFile

    private Map<String, FrameSequence> animations = new HashMap<>();

    private ITabObject tabmenu
    private ITitleObject welcomeTitle
    private ITitleObject firstWelcomeTitle

    Config() {
        TitleManager plugin = TitleManager.getInstance()

        configFile = new ConfigFile(plugin, plugin.getDataFolder(), "config", true)
        animationConfigFile = new ConfigFile(plugin, plugin.getDataFolder(), "animations", true)

        ConfigUpdater.update(plugin, configFile)

        plugin.reloadConfig()
        reload()
    }

    void reload() {
        configFile.reload()
        animationConfigFile.reload()

        animations.clear()

        for (String str : animationConfigFile.getConfig().getKeys(false)) {
            ConfigurationSection section = animationConfigFile.getConfig().getConfigurationSection(str);
            List<AnimationFrame> frames = new ArrayList<>();
            for (String frame : section.getStringList("frames")) {
                int fadeIn = -1
                int stay = -1
                int fadeOut = -1

                frame = MiscellaneousUtils.format(frame)
                if (frame.startsWith("[") && frame.length() > 1) {
                    char[] chars = frame.toCharArray()
                    String timesString = ""
                    for (int i = 1; frame.length() > i; i++) {
                        char c = chars[i]
                        if (c == ']') {
                            frame = frame.substring(i + 1)
                            break
                        }
                        timesString += chars[i]
                    }

                    try {
                        String[] times = timesString.split(";", 3)
                        fadeIn = Integer.valueOf(times[0])
                        stay = Integer.valueOf(times[1])
                        fadeOut = Integer.parseInt(times[2])
                    } catch (NumberFormatException ignored) {
                    }

                    frames.add(new AnimationFrame(frame, fadeIn, stay, fadeOut))
                }
            }
            animations.put(str.toUpperCase().trim(), new FrameSequence(frames))
        }

        if (!isUsingConfig()) return

        if (isTabmenuEnabled()) {
            String headerString = getConfig().getString("tabmenu.header")
            String footerString = getConfig().getString("tabmenu.footer")

            def header = MiscellaneousUtils.isValidAnimationString(headerString) ?: MiscellaneousUtils.format(headerString).replace("\\n", "\n")
            def footer = MiscellaneousUtils.isValidAnimationString(footerString) ?: MiscellaneousUtils.format(footerString).replace("\\n", "\n")

            if (header instanceof FrameSequence || footer instanceof FrameSequence) {
                tabmenu = new TabTitleAnimation(header, footer)
            } else if (header instanceof String && footer instanceof String) {
                tabmenu = new TabTitleAnimation(new FrameSequence(Arrays.asList(new AnimationFrame(header, 0, 5, 0))), new FrameSequence(Arrays.asList(new AnimationFrame(footer, 0, 5, 0))))
            }
            tabmenu.broadcast()
        }

        if (isWelcomeMessageEnabled()) {
            ConfigurationSection section = config.getConfigurationSection("welcome_message")

            String titleString = section.getString("title")
            String subtitleString = section.getString("subtitle")

            int fadeIn = section.getInt("fadeIn")
            int stay = section.getInt("stay")
            int fadeOut = section.getInt("fadeOut")

            def title = MiscellaneousUtils.isValidAnimationString(titleString) ?: MiscellaneousUtils.format(titleString)
            def subtitle = MiscellaneousUtils.isValidAnimationString(subtitleString) ?: MiscellaneousUtils.format(subtitleString)

            if (title instanceof FrameSequence || subtitle instanceof FrameSequence) {
                welcomeTitle = new TitleAnimation(title, subtitle)
            } else if (title instanceof String && subtitle instanceof String) {
                welcomeTitle = new TitleObject(title, subtitle)
                        .setFadeIn(fadeIn).setStay(stay).setFadeOut(fadeOut)
            }

            titleString = section.getString("first-join.title")
            subtitleString = section.getString("first-join.subtitle")

            title = MiscellaneousUtils.isValidAnimationString(titleString) ?: MiscellaneousUtils.format(titleString)
            subtitle = MiscellaneousUtils.isValidAnimationString(subtitleString) ?: MiscellaneousUtils.format(subtitleString)

            if (title instanceof FrameSequence || subtitle instanceof FrameSequence) {
                firstWelcomeTitle = new TitleAnimation(title, subtitle)
            } else if (title instanceof String && subtitle instanceof String) {
                firstWelcomeTitle = new TitleObject(title, subtitle)
                        .setFadeIn(fadeIn).setStay(stay).setFadeOut(fadeOut)
            }
        }
    }

    //Config
    FileConfiguration getConfig() { configFile.config }

    //Animations (Static due to backwards compatibility)
    static FrameSequence getAnimation(String animation) { TitleManager.getInstance().getConfigManager().animations.get(animation.toUpperCase().trim()) }
    static Map<String, FrameSequence> getAnimations() { TitleManager.getInstance().getConfigManager().animations }

    //Enabled config sections
    boolean isUsingConfig() { getConfig().getBoolean("usingConfig") }
    boolean isTabmenuEnabled() { getConfig().getBoolean("tabmenu.enabled") }
    boolean isWelcomeMessageEnabled() { getConfig().getBoolean("welcome_message.enabled") }
    boolean isNumberFormatEnabled() { getConfig().getBoolean("number-format.enabled") }

    ITitleObject getWelcomeObject() { welcomeTitle }
    ITitleObject getFirstWelcomeObject() { firstWelcomeTitle }
    ITabObject getTabTitleObject() { tabmenu }

    String getNumberFormat() { configFile.config.getString("number-format.format") }
}
