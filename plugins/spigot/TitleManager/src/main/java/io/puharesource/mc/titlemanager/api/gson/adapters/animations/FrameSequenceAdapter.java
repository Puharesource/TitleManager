package io.puharesource.mc.titlemanager.api.gson.adapters.animations;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.puharesource.mc.titlemanager.api.animations.AnimationFrame;
import io.puharesource.mc.titlemanager.api.animations.FrameSequence;

import java.lang.reflect.Type;
import java.util.List;

public final class FrameSequenceAdapter implements JsonSerializer<FrameSequence>, JsonDeserializer<FrameSequence> {
    @Override
    public FrameSequence deserialize(final JsonElement element, final Type type, final JsonDeserializationContext context) throws JsonParseException {
        final List<AnimationFrame> frames = context.deserialize(element.getAsJsonObject().get("frames"), new TypeToken<List<AnimationFrame>>(){}.getType());

        return new FrameSequence(frames);
    }

    @Override
    public JsonElement serialize(final FrameSequence frameSequence, Type type, final JsonSerializationContext context) {
        final JsonObject json = new JsonObject();

        json.add("frames", context.serialize(frameSequence.getFrames()));

        return json;
    }
}