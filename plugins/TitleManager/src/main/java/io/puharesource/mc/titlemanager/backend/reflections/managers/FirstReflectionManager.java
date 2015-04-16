package io.puharesource.mc.titlemanager.backend.reflections.managers;

import io.puharesource.mc.titlemanager.backend.reflections.ReflectionClass;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager;

import java.util.Map;

/**
 * This is for the ProtocolHack version, to the massive difference in how Titles are handled in the ProtocolHack version
 * compared the the later version, this Manager does not extend ReflectionManager.
 */
public final class FirstReflectionManager extends ReflectionManager {
    @Override
    public Map<String, ReflectionClass> getClasses() {
        return null;
    }

    @Override
    public Object getIChatBaseComponent(String text) {
        return null;
    }
}
