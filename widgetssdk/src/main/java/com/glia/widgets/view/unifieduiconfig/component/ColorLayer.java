package com.glia.widgets.view.unifieduiconfig.component;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import com.glia.widgets.view.unifieduiconfig.component.base.Color;
import com.glia.widgets.view.unifieduiconfig.deserializer.ColorLayerDeserializer;
import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents Color from remote config
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
 * Guarantees that at least 1 {@link Color} will present in {@link #values}
 *
 * @see ColorLayerDeserializer
 */
public class ColorLayer implements Parcelable {
    public static final String TYPE_FILL = "fill";
    public static final String TYPE_GRADIENT = "gradient";

    @SerializedName(ColorLayerDeserializer.TYPE_KEY)
    @NonNull
    @ColorType
    private final String type;

    @SerializedName(ColorLayerDeserializer.VALUE_KEY)
    @NonNull
    private final List<Color> values;

    public ColorLayer(@NonNull @ColorType String type, @NonNull List<Color> values) {
        this.type = type;
        this.values = values;
    }

    @NonNull
    @ColorType
    public String getType() {
        return type;
    }

    @NonNull
    public List<Color> getValues() {
        return values;
    }

    public boolean isGradient() {
        return !Objects.equals(type, TYPE_FILL);
    }

    @ColorInt
    public int getPrimaryColor() {
        return values.get(0).getColor();
    }

    @ColorInt
    public int getSecondaryColor() {
        if (Objects.equals(type, TYPE_FILL))
            throw new IllegalStateException("secondary color must be used only with type: GRADIENT");

        return values.get(1).getColor();
    }

    @StringDef({TYPE_FILL, TYPE_GRADIENT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ColorType {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
        dest.writeList(this.values);
    }

    protected ColorLayer(Parcel in) {
        this.type = in.readString();
        this.values = new ArrayList<Color>();
        in.readList(this.values, Color.class.getClassLoader());
    }

    public static final Parcelable.Creator<ColorLayer> CREATOR = new Parcelable.Creator<ColorLayer>() {
        @Override
        public ColorLayer createFromParcel(Parcel source) {
            return new ColorLayer(source);
        }

        @Override
        public ColorLayer[] newArray(int size) {
            return new ColorLayer[size];
        }
    };
}
