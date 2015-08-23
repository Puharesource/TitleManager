package io.puharesource.mc.titlemanager.api.iface;

import io.puharesource.mc.titlemanager.api.animations.FrameSequence;

public interface ScriptConverter {
    FrameSequence convert(final String stringToConvert);
}
