package com.glia.widgets.view.unifieduiconfig.deserializer;

import com.glia.widgets.view.unifieduiconfig.component.base.Size;
import com.glia.widgets.view.unifieduiconfig.component.base.SizeImpl;
import com.glia.widgets.helper.ResourceProvider;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class SpDeserializer implements JsonDeserializer<Size.Sp> {
    private final ResourceProvider resourceProvider;

    public SpDeserializer(ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    @Override
    public Size.Sp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            float sizeSp = json.getAsFloat();

            if (sizeSp <= 0) return null;

            return new SizeImpl(sizeSp, resourceProvider.convertSpToPixel(sizeSp));
        } catch (Exception ignore) {
            return null;
        }
    }
}
