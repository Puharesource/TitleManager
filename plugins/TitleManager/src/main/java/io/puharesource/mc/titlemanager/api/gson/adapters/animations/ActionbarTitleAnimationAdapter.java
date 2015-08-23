package io.puharesource.mc.titlemanager.api.gson.adapters.animations;

import com.google.gson.*;
import io.puharesource.mc.titlemanager.api.animations.ActionbarTitleAnimation;
import io.puharesource.mc.titlemanager.api.animations.FrameSequence;

import java.lang.reflect.Type;

public final class ActionbarTitleAnimationAdapter implements JsonSerializer<ActionbarTitleAnimation>, JsonDeserializer<ActionbarTitleAnimation> {
    @Override
    public ActionbarTitleAnimation deserialize(final JsonElement element, final Type type, final JsonDeserializationContext context) throws JsonParseException {
        final FrameSequence frameSequence = context.deserialize(element.getAsJsonObject().get("title"), FrameSequence.class);

        return new ActionbarTitleAnimation(frameSequence);
    }

    @Override
    public JsonElement serialize(final ActionbarTitleAnimation titleObject, Type type, final JsonSerializationContext context) {
        final JsonObject json = new JsonObject();

        json.add("title", context.serialize(titleObject.getTitle()));

        return json;
    }
}