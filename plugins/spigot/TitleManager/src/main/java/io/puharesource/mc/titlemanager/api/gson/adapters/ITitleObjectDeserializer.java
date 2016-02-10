package io.puharesource.mc.titlemanager.api.gson.adapters;

import com.google.gson.*;
import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.api.animations.TitleAnimation;
import io.puharesource.mc.titlemanager.api.iface.ITitleObject;

import java.lang.reflect.Type;

public final class ITitleObjectDeserializer implements JsonDeserializer<ITitleObject> {
    @Override
    public ITitleObject deserialize(final JsonElement element, final Type type, final JsonDeserializationContext context) throws JsonParseException {
        if (element.getAsJsonObject().has("stay")) {
            return context.deserialize(element, TitleObject.class);
        } else {
            return context.deserialize(element, TitleAnimation.class);
        }
    }
}
