package dev.tarkan.titlemanager.bukkit.diagnostics;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.List;

final class RuntimeModuleJavaUsage implements RuntimeVersionModuleFactory {
    @Override
    public String getId() {
        return "java-runtime-test";
    }

    @Override
    public int getPriority() {
        return -1;
    }

    @Override
    public boolean isCompatible(RuntimeServerVersion serverVersion) {
        return serverVersion.matchesNmsVersion("java_test");
    }

    @Override
    public RuntimeVersionModule create(Server server, RuntimeServerVersion serverVersion) {
        return new JavaRuntimeVersionModule(serverVersion);
    }

    private static final class JavaRuntimeVersionModule implements RuntimeVersionModule {
        private final String displayName;

        private JavaRuntimeVersionModule(RuntimeServerVersion serverVersion) {
            this.displayName = "java-runtime-test (" + serverVersion.getDisplayVersion() + ")";
        }

        @Override
        public String getId() {
            return "java-runtime-test";
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public List<DiagnosticsStatus> getCapabilities() {
            return List.of(new DiagnosticsStatus(
                RuntimeCapability.TITLES,
                RuntimeCapabilityStatus.AVAILABLE,
                "java fixture"
            ));
        }

        @Override
        public RuntimeThreadingPolicy getThreadingPolicy() {
            return RuntimeThreadingPolicy.Companion.mainThreadOnly();
        }

        @Override
        public void sendTitleTimes(Player player, Title.Times times) {
        }

        @Override
        public void sendTitle(Player player, Component title) {
        }

        @Override
        public void sendSubtitle(Player player, Component subtitle) {
        }

        @Override
        public void showTitle(Player player, Title title) {
        }

        @Override
        public void sendActionBar(Player player, Component actionBar) {
        }

        @Override
        public void sendPlayerListHeaderAndFooter(Player player, Component header, Component footer) {
        }

        @Override
        public RuntimeSidebar createSidebar(Player player) {
            return new JavaRuntimeSidebar();
        }

        @Override
        public void close() {
        }
    }

    private static final class JavaRuntimeSidebar implements RuntimeSidebar {
        private String title = "";

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public boolean isAppliedTo(Player player) {
            return false;
        }

        @Override
        public String get(int index) {
            return null;
        }

        @Override
        public void set(int index, String value) {
        }

        @Override
        public void remove(int index) {
        }

        @Override
        public void close() {
        }
    }
}
