package io.puharesource.mc.titlemanager.backend.reflections;

import io.puharesource.mc.titlemanager.backend.reflections.managers.*;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

public abstract class ReflectionManager {
    private static final Pattern VERSION_PATTERN = Pattern.compile("(v|)[0-9][_.][0-9]+[_.][R0-9]*");
    private static String version;

    private static List<Class<? extends ReflectionManager>> managers = new ArrayList<Class<? extends ReflectionManager>>() {{
        add(ReflectionManagerProtocolHack1718.class);
        add(ReflectionManager18.class);
        add(ReflectionManager183.class);
        add(ReflectionManager110.class);
        add(ReflectionManager111.class);
    }};

    private static Map<String, Integer> versionIndex = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER) {{
        put("v1_7_R4", 0);
        put("v1_8_R1", 1);
        put("v1_8_R2", 2);
        put("v1_8_R3", 2);
        put("v1_9_R1", 2);
        put("v1_9_R2", 2);
        put("v1_10_R1", 3);
        put("v1_11_R1", 4);
    }};

    /**
     * This will create a ReflectionManager for the current version of Spigot / CraftBukkit.
     * @return Returns the reflection manager suited for the server's version.
     */
    @SneakyThrows
    public static ReflectionManager createManager() {
        return managers.get(getServerVersionIndex()).newInstance();
    }

    /**
     * v1_11_R1 = 1.11
     * v1_10_R1 = 1.10
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

    public static int getServerVersionIndex() {
        return getVersionIndex(getServerVersion());
    }

    public static int getVersionIndex(String version) {
        if (version.endsWith(".")) {
            version = version.substring(0, version.length() - 1);
        }

        if (versionIndex.containsKey(version)) {
            return versionIndex.get(version);
        }

        return managers.size() - 1;
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
