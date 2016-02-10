package io.puharesource.mc.titlemanager.api.gson.adapters;

import com.google.gson.*;
import io.puharesource.mc.titlemanager.api.TitleObject;

import java.lang.reflect.Type;

public final class TitleObjectAdapter implements JsonSerializer<TitleObject>, JsonDeserializer<TitleObject> {
    @Override
    public TitleObject deserialize(final JsonElement element, final Type type, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject json = element.getAsJsonObject();

        return new TitleObject(
                json.get("title").getAsString(),
                json.get("subtitle").getAsString())
                .setFadeIn(json.get("fadeIn").getAsInt())
                .setStay(json.get("stay").getAsInt())
                .setFadeOut(json.get("fadeOut").getAsInt());
    }

    @Override
    public JsonElement serialize(final TitleObject titleObject, Type type, final JsonSerializationContext context) {
        final JsonObject json = new JsonObject();

        json.add("fadeIn", context.serialize(titleObject.getFadeIn()));
        json.add("stay", context.serialize(titleObject.getStay()));
        json.add("fadeOut", context.serialize(titleObject.getFadeOut()));

        json.add("title", context.serialize(titleObject.getTitle()));
        json.add("subtitle", context.serialize(titleObject.getSubtitle()));

        return json;
    }
}
