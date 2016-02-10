package io.puharesource.mc.titlemanager.api.gson.adapters;

import com.google.gson.*;
import io.puharesource.mc.titlemanager.api.TabTitleObject;

import java.lang.reflect.Type;

public final class TabTitleObjectAdapter implements JsonSerializer<TabTitleObject>, JsonDeserializer<TabTitleObject> {
    @Override
    public TabTitleObject deserialize(final JsonElement element, final Type type, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject json = element.getAsJsonObject();

        return new TabTitleObject(
                json.get("header").getAsString(),
                json.get("footer").getAsString());
    }

    @Override
    public JsonElement serialize(final TabTitleObject titleObject, Type type, final JsonSerializationContext context) {
        final JsonObject json = new JsonObject();

        json.add("header", context.serialize(titleObject.getHeader()));
        json.add("footer", context.serialize(titleObject.getFooter()));

        return json;
    }
}
