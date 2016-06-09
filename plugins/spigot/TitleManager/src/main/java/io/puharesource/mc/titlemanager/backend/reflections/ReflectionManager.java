package io.puharesource.mc.titlemanager.backend.reflections;

import org.bukkit.Bukkit;

import java.util.Map;
import java.util.regex.Pattern;

import io.puharesource.mc.titlemanager.backend.reflections.managers.ReflectionManager110;
import io.puharesource.mc.titlemanager.backend.reflections.managers.ReflectionManager18;
import io.puharesource.mc.titlemanager.backend.reflections.managers.ReflectionManager183;
import io.puharesource.mc.titlemanager.backend.reflections.managers.ReflectionManagerProtocolHack1718;
import lombok.SneakyThrows;

public abstract class ReflectionManager {
    private static final Pattern VERSION_PATTERN = Pattern.compile("(v|)[0-9][_.][0-9]+[_.][R0-9]*");
    private static String version;

    /**
     * This will create a ReflectionManager for the current version of Spigot / CraftBukkit.
     * @return Returns the reflection manager suited for the server's version.
     */
    public static ReflectionManager createManager() {
        final String version = getServerVersion();

        if (version.equalsIgnoreCase("v1_10_R1")) return new ReflectionManager110();
        else if (version.equalsIgnoreCase("v1_9_R2")) return new ReflectionManager183();
        else if (version.equalsIgnoreCase("v1_9_R1")) return new ReflectionManager183();
        else if (version.equalsIgnoreCase("v1_8_R3")) return new ReflectionManager183();
        else if (version.equalsIgnoreCase("v1_8_R2.")) return new ReflectionManager183();
        else if (version.equalsIgnoreCase("v1_8_R1.")) return new ReflectionManager18();
        else if (version.equalsIgnoreCase("v1_7_R4.")) return new ReflectionManagerProtocolHack1718();
        return new ReflectionManager110();
    }

    /**
     * v1_9_R2 = 1.9.4
     * v1_9_R1 = 1.9
     * v1_8_R3 = 1.8.8
     * v1_8_R2 = 1.8.3
     * v1_8_R1 = 1.8
     * v1_7_R4 = 1.7 / 1.8 ProtocolHack version.
     *
     * @return The server version number.
     */
    public static String getServerVersion() {
        if (version != null) return version;
        String version;
        String pkg = Bukkit.getServer().getClass().getPackage().getName();
        String version0 = pkg.substring(pkg.lastIndexOf(".") + 1);
        if (!VERSION_PATTERN.matcher(version0).matches()) version0 = "";
        version = version0;
        return ReflectionManager.version = (!version.isEmpty() ? version + "." : "");
    }

    public abstract Map<String, ReflectionClass> getClasses();

    public abstract Object getIChatBaseComponent(String text);

    public enum ReflectionType {
        NET_MINECRAFT_SERVER("net.minecraft.server"),
        ORG_BUKKIT_CRAFTBUKKIT("org.bukkit.craftbukkit"),
        ORG_SPIGOTMC("org.spigotmc");

        ReflectionType(String path) {
            this.path = path;
        }

        @SneakyThrows(ClassNotFoundException.class)
        public ReflectionClass getReflectionClass(final String path) {
            if (this != ORG_SPIGOTMC) {
                final String version = ReflectionManager.getServerVersion();
                return new ReflectionClass(this.path + "." + version + path);
            }

            return new ReflectionClass(this.path + "." + path);
        }

        private String path;
    }
}
