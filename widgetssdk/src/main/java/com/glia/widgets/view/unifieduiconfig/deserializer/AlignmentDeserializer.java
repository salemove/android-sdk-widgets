package com.glia.widgets.view.unifieduiconfig.deserializer;

import com.glia.widgets.view.unifieduiconfig.component.base.Alignment;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Deserializes textStyle property from remote config
 * returns null if property differs from {@link Alignment.Type}
 */
public class AlignmentDeserializer implements JsonDeserializer<Alignment> {
    @Override
    public Alignment deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {

            String type = json.getAsString();

            if (Alignment.TYPE_CENTER.equals(type) || Alignment.TYPE_LEADING.equals(type) || Alignment.TYPE_TRAILING.equals(type)) {
                return new Alignment(type);
            }
            return null;

        } catch (Exception ignore) {
            return null;
        }
    }
}
