package io.puharesource.mc.titlemanager.api.gson.adapters.animations;

import com.google.gson.*;
import io.puharesource.mc.titlemanager.api.animations.FrameSequence;
import io.puharesource.mc.titlemanager.api.animations.TabTitleAnimation;

import java.lang.reflect.Type;

public final class TabTitleAnimationAdapter implements JsonSerializer<TabTitleAnimation>, JsonDeserializer<TabTitleAnimation> {
    @Override
    public TabTitleAnimation deserialize(final JsonElement element, final Type type, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject json = element.getAsJsonObject();

        Object header;
        Object footer;

        if (json.get("header").isJsonPrimitive()) {
            header = json.get("header").getAsString();
        } else {
            header = context.deserialize(json.get("header"), FrameSequence.class);
        }

        if (json.get("footer").isJsonPrimitive()) {
            footer = json.get("footer").getAsString();
        } else {
            footer = context.deserialize(json.get("footer"), FrameSequence.class);
        }

        return new TabTitleAnimation(header, footer);
    }

    @Override
    public JsonElement serialize(final TabTitleAnimation titleObject, Type type, final JsonSerializationContext context) {
        final JsonObject json = new JsonObject();

        json.add("header", context.serialize(titleObject.getHeader()));
        json.add("footer", context.serialize(titleObject.getFooter()));

        return json;
    }
}