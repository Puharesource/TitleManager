package io.puharesource.mc.titlemanager.api.gson.adapters;

import com.google.gson.*;
import io.puharesource.mc.titlemanager.api.TabTitleObject;
import io.puharesource.mc.titlemanager.api.animations.TabTitleAnimation;
import io.puharesource.mc.titlemanager.api.iface.IActionbarObject;

import java.lang.reflect.Type;

public final class ITabTitleObjectDeserializer implements JsonDeserializer<IActionbarObject> {
    @Override
    public IActionbarObject deserialize(final JsonElement element, final Type type, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject json = element.getAsJsonObject();

        if (json.get("header").isJsonPrimitive() && json.get("footer").isJsonPrimitive()) {
            return context.deserialize(element, TabTitleObject.class);
        } else {
            return context.deserialize(element, TabTitleAnimation.class);
        }
    }
}
