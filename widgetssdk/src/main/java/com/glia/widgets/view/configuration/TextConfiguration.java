package com.glia.widgets.view.configuration;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.R;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.ResourceProvider;

/**
 * Please use remote configurations {@link com.glia.widgets.GliaWidgetsConfig.Builder#setUiJsonRemoteConfig(String)}
 */
@Deprecated
//Removed textTypeFaceStyle and allCaps because those properties were unused
public class TextConfiguration implements Parcelable {
    //text size in PX
    private float textSize;
    private ColorStateList textColor;
    private ColorStateList hintColor;
    private ColorStateList textColorLink;
    private int textColorHighlight;
    private int fontFamily;
    private Boolean bold;

    private TextConfiguration(
        Builder builder
    ) {
        this.textSize = builder.textSize;
        this.textColor = builder.textColor;
        this.hintColor = builder.hintColor;
        this.textColorLink = builder.textColorLink;
        this.textColorHighlight = builder.textColorHighlight;
        this.fontFamily = builder.fontFamily;
        this.bold = builder.bold;
    }

    public float getTextSize() {
        return textSize;
    }

    public ColorStateList getTextColor() {
        return textColor;
    }

    public ColorStateList getHintColor() {
        return hintColor;
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

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Please use remote configurations {@link com.glia.widgets.GliaWidgetsConfig.Builder#setUiJsonRemoteConfig(String)}
     */
    @Deprecated
    public static class Builder {
        private final String TAG = TextConfiguration.Builder.class.getSimpleName();

        private float textSize;
        private ColorStateList textColor;
        private ColorStateList hintColor;
        private ColorStateList textColorLink;
        private int textColorHighlight;
        private int fontFamily;
        private Boolean bold;
        private Boolean allCaps;

        public Builder() {
        }

        public Builder(TextConfiguration textConfiguration) {
            this.textSize = textConfiguration.textSize;
            this.textColor = textConfiguration.textColor;
            this.hintColor = textConfiguration.hintColor;
            this.textColorLink = textConfiguration.textColorLink;
            this.textColorHighlight = textConfiguration.textColorHighlight;
            this.fontFamily = textConfiguration.fontFamily;
            this.bold = textConfiguration.bold;
        }

        public Builder textSize(float textSize) {
            this.textSize = textSize;
            return this;
        }

        public Builder textTypeFaceStyle(int textTypeFaceStyle) {
            return this;
        }

        public Builder textColor(ColorStateList textColor) {
            this.textColor = textColor;
            return this;
        }

        public Builder textColor(String textColor) {
            this.textColor = ColorStateList.valueOf(Color.parseColor(textColor));
            return this;
        }

        public Builder hintColor(ColorStateList hintColor) {
            this.hintColor = hintColor;
            return this;
        }

        public Builder hintColor(String hintColor) {
            this.hintColor = ColorStateList.valueOf(Color.parseColor(hintColor));
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
            Logger.logDeprecatedClassUse(TextConfiguration.class.getSimpleName() + "." + TAG);
            // Default configuration
            if (this.textSize == 0) {
                this.textSize = resourceProvider.getDimension(R.dimen.glia_survey_default_text_size);
            }
            if (this.textColor == null) {
                this.textColor = resourceProvider.getColorStateList(R.color.glia_dark_color);
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
        dest.writeParcelable(this.textColor, flags);
        dest.writeParcelable(this.hintColor, flags);
        dest.writeParcelable(this.textColorLink, flags);
        dest.writeInt(this.textColorHighlight);
        dest.writeInt(this.fontFamily);
        dest.writeByte(this.bold ? (byte) 1 : (byte) 0);
    }

    public void readFromParcel(Parcel source) {
        this.textSize = source.readFloat();
        this.textColor = source.readParcelable(ColorStateList.class.getClassLoader());
        this.hintColor = source.readParcelable(ColorStateList.class.getClassLoader());
        this.textColorLink = source.readParcelable(ColorStateList.class.getClassLoader());
        this.textColorHighlight = source.readInt();
        this.fontFamily = source.readInt();
        this.bold = source.readByte() != 0;
    }

    protected TextConfiguration(Parcel in) {
        this.textSize = in.readFloat();
        this.textColor = in.readParcelable(ColorStateList.class.getClassLoader());
        this.hintColor = in.readParcelable(ColorStateList.class.getClassLoader());
        this.textColorLink = in.readParcelable(ColorStateList.class.getClassLoader());
        this.textColorHighlight = in.readInt();
        this.fontFamily = in.readInt();
        this.bold = in.readByte() != 0;
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
