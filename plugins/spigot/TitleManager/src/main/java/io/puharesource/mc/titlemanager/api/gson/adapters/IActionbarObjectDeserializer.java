package io.puharesource.mc.titlemanager.api.gson.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.puharesource.mc.titlemanager.api.ActionbarTitleObject;
import io.puharesource.mc.titlemanager.api.animations.ActionbarTitleAnimation;
import io.puharesource.mc.titlemanager.api.iface.IActionbarObject;

import java.lang.reflect.Type;

public final class IActionbarObjectDeserializer implements JsonDeserializer<IActionbarObject> {
    @Override
    public IActionbarObject deserialize(final JsonElement element, final Type type, final JsonDeserializationContext context) throws JsonParseException {
        if (element.getAsJsonObject().get("title").isJsonPrimitive()) {
            return context.deserialize(element, ActionbarTitleObject.class);
        } else {
            return context.deserialize(element, ActionbarTitleAnimation.class);
        }
    }
}
