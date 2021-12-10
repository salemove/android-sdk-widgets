package com.glia.widgets.view.configuration;

import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class TextConfiguration implements Parcelable {
    private final Float textSize;
    private final Integer textTypeFaceStyle;
    private final ColorStateList textColor;
    private final ColorStateList textColorHint;
    private final ColorStateList textColorLink;
    private final Integer textColorHighlight;
    private final Integer fontFamily;
    private final Boolean allCaps;

    private TextConfiguration(
            Builder builder
    ) {
        this.textSize = builder.textSize;
        this.textTypeFaceStyle = builder.textTypeFaceStyle;
        this.textColor = builder.textColor;
        this.textColorHint = builder.textColorHint;
        this.textColorHighlight = builder.textColorHighlight;
        this.textColorLink = builder.textColorLink;
        this.fontFamily = builder.fontFamily;
        this.allCaps = builder.allCaps;
    }

    protected TextConfiguration(Parcel in) {
        if (in.readByte() == 0) {
            textSize = null;
        } else {
            textSize = in.readFloat();
        }
        if (in.readByte() == 0) {
            textTypeFaceStyle = null;
        } else {
            textTypeFaceStyle = in.readInt();
        }
        textColor = in.readParcelable(ColorStateList.class.getClassLoader());
        textColorHint = in.readParcelable(ColorStateList.class.getClassLoader());
        textColorLink = in.readParcelable(ColorStateList.class.getClassLoader());
        if (in.readByte() == 0) {
            textColorHighlight = null;
        } else {
            textColorHighlight = in.readInt();
        }
        if (in.readByte() == 0) {
            fontFamily = null;
        } else {
            fontFamily = in.readInt();
        }
        byte tmpAllCaps = in.readByte();
        allCaps = tmpAllCaps == 0 ? null : tmpAllCaps == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (textSize == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeFloat(textSize);
        }
        if (textTypeFaceStyle == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(textTypeFaceStyle);
        }
        dest.writeParcelable(textColor, flags);
        dest.writeParcelable(textColorHint, flags);
        dest.writeParcelable(textColorLink, flags);
        if (textColorHighlight == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(textColorHighlight);
        }
        if (fontFamily == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(fontFamily);
        }
        dest.writeByte((byte) (allCaps == null ? 0 : allCaps ? 1 : 2));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TextConfiguration> CREATOR = new Creator<TextConfiguration>() {
        @Override
        public TextConfiguration createFromParcel(Parcel in) {
            return new TextConfiguration(in);
        }

        @Override
        public TextConfiguration[] newArray(int size) {
            return new TextConfiguration[size];
        }
    };

    @Nullable
    public Float getTextSize() {
        return this.textSize;
    }

    @Nullable
    public Integer getTextTypeFaceStyle() {
        return this.textTypeFaceStyle;
    }

    @Nullable
    public ColorStateList getTextColor() {
        return this.textColor;
    }

    @Nullable
    public ColorStateList getTextColorHint() {
        return this.textColorHint;
    }

    public Integer getTextColorHighlight() {
        return this.textColorHighlight;
    }

    @Nullable
    public ColorStateList getTextColorLink() {
        return this.textColorLink;
    }

    @Nullable
    public Integer getFontFamily() {
        return this.fontFamily;
    }

    @Nullable
    public Boolean getAllCaps() {
        return this.allCaps;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private float textSize;
        private int textTypeFaceStyle;
        private ColorStateList textColor;
        private ColorStateList textColorHint;
        private ColorStateList textColorLink;
        private int textColorHighlight;
        private int fontFamily;
        private boolean allCaps;

        public Builder() {
        }

        public Builder(TextConfiguration buildTime) {
            this.textSize = buildTime.textSize;
            this.textTypeFaceStyle = buildTime.textTypeFaceStyle;
            this.textColor = buildTime.textColor;
            this.textColorHint = buildTime.textColorHint;
            this.textColorHighlight = buildTime.textColorHighlight;
            this.textColorLink = buildTime.textColorLink;
            this.fontFamily = buildTime.fontFamily;
            this.allCaps = buildTime.allCaps;
        }

        public TextConfiguration build() {
            return new TextConfiguration(this);
        }

        public Builder allCaps(boolean allCaps) {
            this.allCaps = allCaps;
            return this;
        }

        public Builder fontFamily(int fontFamily) {
            this.fontFamily = fontFamily;
            return this;
        }

        public Builder textColorLink(ColorStateList textColorLink) {
            this.textColorLink = textColorLink;
            return this;
        }

        public Builder textColorHighlight(int textColorHighlight) {
            this.textColorHighlight = textColorHighlight;
            return this;
        }

        public Builder textColorHint(ColorStateList textColorHint) {
            this.textColorHint = textColorHint;
            return this;
        }

        public Builder textColor(ColorStateList textColor) {
            this.textColor = textColor;
            return this;
        }

        public Builder textTypeFaceStyle(int textTypeFaceStyle) {
            this.textTypeFaceStyle = textTypeFaceStyle;
            return this;
        }

        public Builder textSize(Float textSize) {
            this.textSize = textSize;
            return this;
        }
    }
}
