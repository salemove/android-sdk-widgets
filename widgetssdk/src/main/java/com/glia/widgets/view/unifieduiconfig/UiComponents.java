package com.glia.widgets.view.unifieduiconfig;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.widgets.view.unifieduiconfig.component.ColorLayer;
import com.glia.widgets.view.unifieduiconfig.component.RemoteConfiguration;
import com.glia.widgets.view.unifieduiconfig.component.base.Alignment;
import com.glia.widgets.view.unifieduiconfig.component.base.Size;
import com.glia.widgets.view.unifieduiconfig.component.base.TextStyle;
import com.glia.widgets.view.unifieduiconfig.deserializer.AlignmentDeserializer;
import com.glia.widgets.view.unifieduiconfig.deserializer.DpDeserializer;
import com.glia.widgets.view.unifieduiconfig.deserializer.ColorLayerDeserializer;
import com.glia.widgets.view.unifieduiconfig.deserializer.SpDeserializer;
import com.glia.widgets.view.unifieduiconfig.deserializer.TextStyleDeserializer;
import com.glia.widgets.di.Dependencies;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UiComponents {

    /**
     * @return {@link Gson} instance with applied deserializers to parse remote config.
     */
    public static Gson getGsonInstance() {
        return new GsonBuilder()
                .registerTypeAdapter(ColorLayer.class, new ColorLayerDeserializer())
                .registerTypeAdapter(Size.Dp.class, new DpDeserializer(Dependencies.getResourceProvider()))
                .registerTypeAdapter(Size.Sp.class, new SpDeserializer(Dependencies.getResourceProvider()))
                .registerTypeAdapter(TextStyle.class, new TextStyleDeserializer())
                .registerTypeAdapter(Alignment.class, new AlignmentDeserializer())
                .create();
    }

    @Nullable
    public static RemoteConfiguration parseRemoteConfiguration(@NonNull String remoteConfiguration) {
        return getGsonInstance().fromJson(remoteConfiguration, RemoteConfiguration.class);
    }

}
