package io.puharesource.mc.titlemanager.api.gson.adapters;

import com.google.gson.*;
import io.puharesource.mc.titlemanager.api.ActionbarTitleObject;

import java.lang.reflect.Type;

public final class ActionbarTitleAdapter implements JsonSerializer<ActionbarTitleObject>, JsonDeserializer<ActionbarTitleObject> {
    @Override
    public ActionbarTitleObject deserialize(final JsonElement element, final Type type, final JsonDeserializationContext context) throws JsonParseException {
        return new ActionbarTitleObject(element.getAsJsonObject().get("title").getAsString());
    }

    @Override
    public JsonElement serialize(final ActionbarTitleObject titleObject, Type type, final JsonSerializationContext context) {
        final JsonObject json = new JsonObject();

        json.add("title", context.serialize(titleObject.getTitle()));

        return json;
    }
}
