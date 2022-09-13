package com.glia.widgets.view.unifieduiconfig.deserializer;

import android.text.TextUtils;

import com.glia.widgets.view.unifieduiconfig.component.ColorLayer;
import com.glia.widgets.view.unifieduiconfig.component.base.Color;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Deserializes Color from remote config
 * <code>
 *  {
 *       "type": "gradient",
 *       "value": [
 *           "#FF4433DD",
 *           "#AA4433DD"
 *       ]
 *   }
 * </code>
 * <p>
 * to {@link ColorLayer}
 *
 * will return {@link null} if "value" property is missing or empty
 * will change {@link ColorLayer#getType()} to {@link ColorLayer#TYPE_FILL} if "value" property contains single color.
 *
 * @see ColorLayerDeserializer
 */
public class ColorLayerDeserializer implements JsonDeserializer<ColorLayer> {
    public static final String TYPE_KEY = "type";
    public static final String VALUE_KEY = "value";
    private final ColorDeserializer colorDeserializer = new ColorDeserializer();

    @Override
    public ColorLayer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root;
        try {
            root = json.getAsJsonObject();
        } catch (IllegalStateException ignore) {
            return null;
        }

        JsonElement rawType = root.get(TYPE_KEY);
        JsonElement rawValues = root.get(VALUE_KEY);

        if (rawValues == null || !rawValues.isJsonArray() || rawValues.getAsJsonArray().size() == 0) {
            return null;
        }

        List<Color> colors = parseColors(rawValues.getAsJsonArray());

        if (colors.isEmpty()) return null;

        String type = parseType(rawType);

        if (colors.size() == 1 && !type.equals(ColorLayer.TYPE_FILL)) {
            type = ColorLayer.TYPE_FILL;
        }

        return new ColorLayer(type, colors);

    }

    private List<Color> parseColors(JsonArray valuesArray) {
        List<Color> values = new ArrayList<>();

        for (int i = 0; i < valuesArray.size(); i++) {
            JsonElement rawColor = valuesArray.get(i);
            Color color = colorDeserializer.deserialize(rawColor, null, null);

            if (color == null) continue;

            values.add(color);
        }

        return values;
    }

    private String parseType(JsonElement element) {
        try {
            String type = element.getAsString();

            if (TextUtils.isEmpty(type)) {
                return ColorLayer.TYPE_FILL;
            }

            return type;

        } catch (IllegalStateException ignore) {
            return ColorLayer.TYPE_FILL;
        }
    }
}
