package io.puharesource.mc.titlemanager.api.scripts;

import io.puharesource.mc.titlemanager.api.animations.LuaScriptAnimationConverter;
import io.puharesource.mc.titlemanager.api.iface.Script;
import io.puharesource.mc.titlemanager.api.iface.ScriptConverter;
import lombok.Data;
import org.luaj.vm2.LuaValue;

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
    public ScriptConverter getConverter() {
        return new LuaScriptAnimationConverter(value);
    }
}
