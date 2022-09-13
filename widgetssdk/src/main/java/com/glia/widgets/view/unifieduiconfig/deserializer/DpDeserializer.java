package com.glia.widgets.view.unifieduiconfig.deserializer;

import com.glia.widgets.view.unifieduiconfig.component.base.Size;
import com.glia.widgets.view.unifieduiconfig.component.base.SizeImpl;
import com.glia.widgets.helper.ResourceProvider;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class DpDeserializer implements JsonDeserializer<Size.Dp> {
    private final ResourceProvider resourceProvider;

    public DpDeserializer(ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    @Override
    public Size.Dp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            float sizeDp = json.getAsFloat();
            if (sizeDp <= 0) return null;

            return new SizeImpl(resourceProvider.convertDpToPixel(sizeDp));
        } catch (Exception ignore) {
            return null;
        }
    }
}
