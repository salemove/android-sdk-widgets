package com.glia.widgets.view.unifieduiconfig.component;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.glia.widgets.view.unifieduiconfig.component.base.Size;
import com.glia.widgets.view.unifieduiconfig.component.base.TextStyle;
import com.google.gson.annotations.SerializedName;

public class Font implements Parcelable {

    @SerializedName("size")
    @Nullable
    private final Size.Sp size;

    @SerializedName("style")
    @Nullable
    private final TextStyle style;

    @Nullable
    public Size.Sp getSize() {
        return size;
    }

    @Nullable
    public TextStyle getStyle() {
        return style;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.size, flags);
        dest.writeParcelable(this.style, flags);
    }

    protected Font(Parcel in) {
        this.size = in.readParcelable(Size.Sp.class.getClassLoader());
        this.style = in.readParcelable(TextStyle.class.getClassLoader());
    }

    public static final Parcelable.Creator<Font> CREATOR = new Parcelable.Creator<Font>() {
        @Override
        public Font createFromParcel(Parcel source) {
            return new Font(source);
        }

        @Override
        public Font[] newArray(int size) {
            return new Font[size];
        }
    };
}
