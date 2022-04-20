package com.glia.widgets.view.configuration;

import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.R;
import com.glia.widgets.helper.ResourceProvider;

public class TextConfiguration implements Parcelable {
    private float textSize;
    private int textTypeFaceStyle;
    private ColorStateList textColor;
    private ColorStateList textColorHint;
    private ColorStateList textColorLink;
    private int textColorHighlight;
    private int fontFamily;
    private Boolean bold;
    private Boolean allCaps;

    private TextConfiguration(
            Builder builder
    ) {
        this.textSize = builder.textSize;
        this.textTypeFaceStyle = builder.textTypeFaceStyle;
        this.textColor = builder.textColor;
        this.textColorHint = builder.textColorHint;
        this.textColorLink = builder.textColorLink;
        this.textColorHighlight = builder.textColorHighlight;
        this.fontFamily = builder.fontFamily;
        this.bold = builder.bold;
        this.allCaps = builder.allCaps;
    }

    public float getTextSize() {
        return textSize;
    }

    public int getTextTypeFaceStyle() {
        return textTypeFaceStyle;
    }

    public ColorStateList getTextColor() {
        return textColor;
    }

    public ColorStateList getTextColorHint() {
        return textColorHint;
    }

    public ColorStateList getTextColorLink() {
        return textColorLink;
    }

    public int getTextColorHighlight() {
        return textColorHighlight;
    }

    public int getFontFamily() {
        return fontFamily;
    }

    public Boolean isBold() {
        return this.bold;
    }

    public Boolean isAllCaps() {
        return allCaps;
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
        private Boolean bold;
        private Boolean allCaps;

        public Builder() {
        }

        public Builder(TextConfiguration textConfiguration) {
            this.textSize = textConfiguration.textSize;
            this.textTypeFaceStyle = textConfiguration.textTypeFaceStyle;
            this.textColor = textConfiguration.textColor;
            this.textColorHint = textConfiguration.textColorHint;
            this.textColorLink = textConfiguration.textColorLink;
            this.textColorHighlight = textConfiguration.textColorHighlight;
            this.fontFamily = textConfiguration.fontFamily;
            this.bold = textConfiguration.bold;
            this.allCaps = textConfiguration.allCaps;
        }

        public Builder textSize(float textSize) {
            this.textSize = textSize;
            return this;
        }

        public Builder textTypeFaceStyle(int textTypeFaceStyle) {
            this.textTypeFaceStyle = textTypeFaceStyle;
            return this;
        }

        public Builder textColor(ColorStateList textColor) {
            this.textColor = textColor;
            return this;
        }

        public Builder textColorHint(ColorStateList textColorHint) {
            this.textColorHint = textColorHint;
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

        public Builder fontFamily(int fontFamily) {
            this.fontFamily = fontFamily;
            return this;
        }

        public Builder bold(Boolean bold) {
            this.bold = bold;
            return this;
        }

        public Builder allCaps(Boolean allCaps) {
            this.allCaps = allCaps;
            return this;
        }

        public TextConfiguration build(ResourceProvider resourceProvider) {
            // Default configuration
            if (this.textSize == 0) {
                this.textSize = resourceProvider.getDimension(R.dimen.glia_survey_default_text_size);
            }
            if (this.bold == null) {
                this.bold = false;
            }
            if (this.allCaps == null) {
                this.allCaps = false;
            }
            return new TextConfiguration(this);
        }
    }

    /* BEGIN: Parcelable related */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.textSize);
        dest.writeInt(this.textTypeFaceStyle);
        dest.writeParcelable(this.textColor, flags);
        dest.writeParcelable(this.textColorHint, flags);
        dest.writeParcelable(this.textColorLink, flags);
        dest.writeInt(this.textColorHighlight);
        dest.writeInt(this.fontFamily);
        dest.writeByte(this.bold ? (byte) 1 : (byte) 0);
        dest.writeByte(this.allCaps ? (byte) 1 : (byte) 0);
    }

    public void readFromParcel(Parcel source) {
        this.textSize = source.readFloat();
        this.textTypeFaceStyle = source.readInt();
        this.textColor = source.readParcelable(ColorStateList.class.getClassLoader());
        this.textColorHint = source.readParcelable(ColorStateList.class.getClassLoader());
        this.textColorLink = source.readParcelable(ColorStateList.class.getClassLoader());
        this.textColorHighlight = source.readInt();
        this.fontFamily = source.readInt();
        this.bold = source.readByte() != 0;
        this.allCaps = source.readByte() != 0;
    }

    protected TextConfiguration(Parcel in) {
        this.textSize = in.readFloat();
        this.textTypeFaceStyle = in.readInt();
        this.textColor = in.readParcelable(ColorStateList.class.getClassLoader());
        this.textColorHint = in.readParcelable(ColorStateList.class.getClassLoader());
        this.textColorLink = in.readParcelable(ColorStateList.class.getClassLoader());
        this.textColorHighlight = in.readInt();
        this.fontFamily = in.readInt();
        this.bold = in.readByte() != 0;
        this.allCaps = in.readByte() != 0;
    }

    public static final Creator<TextConfiguration> CREATOR = new Creator<TextConfiguration>() {
        @Override
        public TextConfiguration createFromParcel(Parcel source) {
            return new TextConfiguration(source);
        }

        @Override
        public TextConfiguration[] newArray(int size) {
            return new TextConfiguration[size];
        }
    };
    /* END: Parcelable related */
}
