package io.puharesource.mc.titlemanager.api.scripts;

import io.puharesource.mc.titlemanager.api.TextConverter;
import io.puharesource.mc.titlemanager.api.animations.AnimationFrame;
import lombok.Data;
import lombok.val;
import org.bukkit.entity.Player;
import org.luaj.vm2.LuaValue;

import java.util.Iterator;

@Data
public final class LuaScriptIterator implements Iterator<AnimationFrame> {
    private final LuaValue value;
    private final String originalString;
    private final Player player;

    private int i;
    private boolean done;

    @Override
    public boolean hasNext() {
        return !done;
    }

    @Override
    public AnimationFrame next() {
        val args = value.get("tm_main").invoke(LuaValue.valueOf(TextConverter.setVariables(player, originalString)), LuaValue.valueOf(i));
        done = args.arg(2).toboolean();
        i++;
        return new AnimationFrame(args.arg(1).toString(), args.arg(3).toint(), args.arg(4).toint(), args.arg(5).toint());
    }

    public void reset() {
        done = false;
        i = 0;
    }
}
