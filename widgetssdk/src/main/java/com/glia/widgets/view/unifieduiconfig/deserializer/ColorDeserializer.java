package com.glia.widgets.view.unifieduiconfig.deserializer;


import com.glia.widgets.view.unifieduiconfig.component.base.Color;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Json deserializer for ARB color, will return {@link null} if color is not valid and {@link Color} for other cases
 */
public class ColorDeserializer implements JsonDeserializer<Color> {

    @Override
    public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return new Color(android.graphics.Color.parseColor(json.getAsString()));
        } catch (Exception ignore) {
            return null;
        }
    }
}
