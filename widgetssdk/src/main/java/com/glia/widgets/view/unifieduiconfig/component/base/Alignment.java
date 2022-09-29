package com.glia.widgets.view.unifieduiconfig.component.base;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.StringDef;

import com.glia.widgets.view.unifieduiconfig.deserializer.AlignmentDeserializer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Represents Alignment from remote config.
 *
 * @see AlignmentDeserializer
 */
public class Alignment implements Parcelable {
    public static final String TYPE_LEADING = "leading";
    public static final String TYPE_CENTER = "center";
    public static final String TYPE_TRAILING = "trailing";

    @StringDef({TYPE_LEADING, TYPE_CENTER, TYPE_TRAILING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    @Type
    private final String type;

    public Alignment(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
    }

    protected Alignment(Parcel in) {
        this.type = in.readString();
    }

    public static final Parcelable.Creator<Alignment> CREATOR = new Parcelable.Creator<Alignment>() {
        @Override
        public Alignment createFromParcel(Parcel source) {
            return new Alignment(source);
        }

        @Override
        public Alignment[] newArray(int size) {
            return new Alignment[size];
        }
    };
}
