package com.glia.widgets.view.unifieduiconfig.component;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.glia.widgets.view.unifieduiconfig.component.base.Alignment;
import com.google.gson.annotations.SerializedName;

public class Text implements Parcelable {

    @SerializedName("foreground")
    @Nullable
    private final ColorLayer textColor;

    @SerializedName("background")
    @Nullable
    private final ColorLayer backgroundColor;

    @SerializedName("font")
    @Nullable
    private final Font font;

    @SerializedName("alignment")
    @Nullable
    private final Alignment alignment;

    @Nullable
    public ColorLayer getTextColor() {
        return textColor;
    }

    @Nullable
    public ColorLayer getBackgroundColor() {
        return backgroundColor;
    }

    @Nullable
    public Font getFont() {
        return font;
    }

    @Nullable
    public Alignment getAlignment() {
        return alignment;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.textColor, flags);
        dest.writeParcelable(this.backgroundColor, flags);
        dest.writeParcelable(this.font, flags);
        dest.writeParcelable(this.alignment, flags);
    }

    protected Text(Parcel in) {
        this.textColor = in.readParcelable(ColorLayer.class.getClassLoader());
        this.backgroundColor = in.readParcelable(ColorLayer.class.getClassLoader());
        this.font = in.readParcelable(Font.class.getClassLoader());
        this.alignment = in.readParcelable(Alignment.class.getClassLoader());
    }

    public static final Parcelable.Creator<Text> CREATOR = new Parcelable.Creator<Text>() {
        @Override
        public Text createFromParcel(Parcel source) {
            return new Text(source);
        }

        @Override
        public Text[] newArray(int size) {
            return new Text[size];
        }
    };
}
