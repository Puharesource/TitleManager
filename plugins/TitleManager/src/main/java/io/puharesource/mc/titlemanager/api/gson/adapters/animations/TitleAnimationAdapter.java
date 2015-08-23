package io.puharesource.mc.titlemanager.api.gson.adapters.animations;

import com.google.gson.*;
import io.puharesource.mc.titlemanager.api.animations.FrameSequence;
import io.puharesource.mc.titlemanager.api.animations.TitleAnimation;

import java.lang.reflect.Type;

public final class TitleAnimationAdapter implements JsonSerializer<TitleAnimation>, JsonDeserializer<TitleAnimation> {
    @Override
    public TitleAnimation deserialize(final JsonElement element, final Type type, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject json = element.getAsJsonObject();

        Object title;
        Object subtitle;

        if (json.get("title").isJsonPrimitive()) {
            title = json.get("title").getAsString();
        } else {
            title = context.deserialize(json.get("title"), FrameSequence.class);
        }

        if (json.get("subtitle").isJsonPrimitive()) {
            subtitle = json.get("subtitle").getAsString();
        } else {
            subtitle = context.deserialize(json.get("subtitle"), FrameSequence.class);
        }

        return new TitleAnimation(title, subtitle);
    }

    @Override
    public JsonElement serialize(final TitleAnimation titleObject, Type type, final JsonSerializationContext context) {
        final JsonObject json = new JsonObject();

        json.add("title", context.serialize(titleObject.getTitle()));
        json.add("subtitle", context.serialize(titleObject.getSubtitle()));

        return json;
    }
}