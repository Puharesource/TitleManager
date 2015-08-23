package io.puharesource.mc.titlemanager.api.gson.adapters.animations;

import com.google.gson.*;
import io.puharesource.mc.titlemanager.api.animations.AnimationFrame;

import java.lang.reflect.Type;

public final class AnimationFrameAdapter implements JsonSerializer<AnimationFrame>, JsonDeserializer<AnimationFrame> {
    @Override
    public AnimationFrame deserialize(final JsonElement element, final Type type, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject json = element.getAsJsonObject();

        return new AnimationFrame(
                json.get("text").getAsString(),
                json.get("fadeIn").getAsInt(),
                json.get("stay").getAsInt(),
                json.get("fadeOut").getAsInt());
    }

    @Override
    public JsonElement serialize(final AnimationFrame frame, Type type, final JsonSerializationContext context) {
        final JsonObject json = new JsonObject();

        json.add("text", context.serialize(frame.getText()));
        json.add("fadeIn", context.serialize(frame.getFadeIn()));
        json.add("stay", context.serialize(frame.getStay()));
        json.add("fadeOut", context.serialize(frame.getFadeOut()));

        return json;
    }
}