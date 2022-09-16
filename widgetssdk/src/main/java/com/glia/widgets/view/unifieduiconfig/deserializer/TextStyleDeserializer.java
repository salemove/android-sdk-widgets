package com.glia.widgets.view.unifieduiconfig.deserializer;

import android.graphics.Typeface;

import com.glia.widgets.view.unifieduiconfig.component.base.TextStyle;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Deserializes textStyle property from remote config
 * returns null if property differs from {@link #REGULAR},{@link #BOLD},{@link #ITALIC},{@link #BOLD_ITALIC}
 */
public class TextStyleDeserializer implements JsonDeserializer<TextStyle> {
    private static final String REGULAR = "regular";
    private static final String BOLD = "bold";
    private static final String ITALIC = "italic";
    private static final String BOLD_ITALIC = "bold_italic";

    @Override
    public TextStyle deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            int typeface;

            switch (json.getAsString()) {
                case BOLD:
                    typeface = Typeface.BOLD;
                    break;
                case ITALIC:
                    typeface = Typeface.ITALIC;
                    break;
                case BOLD_ITALIC:
                    typeface = Typeface.BOLD_ITALIC;
                    break;
                case REGULAR:
                    typeface = Typeface.NORMAL;
                    break;
                default:
                    return null;
            }

            return new TextStyle(typeface);

        } catch (Exception ignore) {
            return null;
        }
    }
}
