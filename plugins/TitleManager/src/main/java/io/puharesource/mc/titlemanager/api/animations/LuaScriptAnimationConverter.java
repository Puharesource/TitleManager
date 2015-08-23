package io.puharesource.mc.titlemanager.api.animations;

import io.puharesource.mc.titlemanager.api.iface.ScriptConverter;
import lombok.Data;
import lombok.val;
import org.luaj.vm2.LuaValue;

import java.util.ArrayList;
import java.util.List;

@Data
public final class LuaScriptAnimationConverter implements ScriptConverter {
    private final LuaValue value;

    public FrameSequence convert(final String textToAnimation) {
        boolean done = false;
        int i = 0;
        final List<AnimationFrame> frames = new ArrayList<>();

        while (!done) {
            val args = value.get("tm_main").invoke(LuaValue.valueOf(textToAnimation), LuaValue.valueOf(i));
            done = args.arg(2).toboolean();
            frames.add(new AnimationFrame(args.arg(1).toString(), args.arg(3).toint(), args.arg(4).toint(), args.arg(5).toint()));
            i++;
        }

        return new FrameSequence(frames);
    }
}
