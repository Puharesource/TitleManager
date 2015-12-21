package io.puharesource.mc.titlemanager.api.scripts;

import io.puharesource.mc.titlemanager.api.animations.AnimationFrame;
import io.puharesource.mc.titlemanager.api.iface.Script;
import lombok.Data;
import org.bukkit.entity.Player;
import org.luaj.vm2.LuaValue;

import java.util.Iterator;

@Data
public class LuaScript implements Script {
    private final LuaValue value;

    @Override
    public String getName() {
        return value.get("name").toString();
    }

    @Override
    public String getVersion() {
        return value.get("version").toString();
    }

    @Override
    public String getAuthor() {
        return value.get("author").toString();
    }

    @Override
    public Iterator<AnimationFrame> getIterator(final String originalString, final Player player) {
        return new LuaScriptIterator(value, originalString, player);
    }
}
