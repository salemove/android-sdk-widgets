package com.glia.widgets.view.configuration;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.widgets.R;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.ResourceProvider;

public class TextConfiguration implements Parcelable {

    /* BEGIN: Remote configuration fields */
    // Font size in SP.
    @Nullable
    private final Float textSize;

    @Nullable
    private final Integer textTypeFaceStyle;

    @Nullable
    private final ColorStateList textColor;

    @Nullable
    private final ColorConfiguration backgroundColor;

    @Nullable
    private final Integer textAlignment;
    /* END: Remote configuration fields */

    @Nullable
    private final ColorStateList hintColor;

    @Nullable
    private final ColorStateList textColorLink;

    @Nullable
    private final Integer textColorHighlight;

    @Nullable
    private final Integer fontFamily;

    @Nullable
    private final Boolean bold;

    @Nullable
    private final Boolean allCaps;

    private TextConfiguration(
            Builder builder
    ) {
        this.textSize = builder.textSize;
        this.textTypeFaceStyle = builder.textTypeFaceStyle;
        this.textColor = builder.textColor;
        this.backgroundColor = builder.backgroundColor;
        this.textAlignment = builder.textAlignment;
        this.hintColor = builder.hintColor;
        this.textColorLink = builder.textColorLink;
        this.textColorHighlight = builder.textColorHighlight;
        this.fontFamily = builder.fontFamily;
        this.bold = builder.bold;
        this.allCaps = builder.allCaps;
    }

    @Nullable
    public Float getTextSize() {
        return textSize;
    }

    @Nullable
    public Integer getTextTypeFaceStyle() {
        return textTypeFaceStyle;
    }

    @Nullable
    public ColorStateList getTextColor() {
        return textColor;
    }

    @Nullable
    public ColorConfiguration getBackgroundColor() {
        return backgroundColor;
    }

    @Nullable
    public Integer getTextAlignment() {
        return textAlignment;
    }

    @Nullable
    public ColorStateList getHintColor() {
        return hintColor;
    }

    @Nullable
    public ColorStateList getTextColorLink() {
        return textColorLink;
    }

    @Nullable
    public Integer getTextColorHighlight() {
        return textColorHighlight;
    }

    @Nullable
    public Integer getFontFamily() {
        return fontFamily;
    }

    @Nullable
    public Boolean isBold() {
        return this.bold;
    }

    @Nullable
    public Boolean isAllCaps() {
        return allCaps;
    }

    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        @Nullable
        private Float textSize;

        @Nullable
        private Integer textTypeFaceStyle;

        @Nullable
        private ColorStateList textColor;

        @Nullable
        private ColorConfiguration backgroundColor;

        @Nullable
        private Integer textAlignment;

        @Nullable
        private ColorStateList hintColor;

        @Nullable
        private ColorStateList textColorLink;

        @Nullable
        private Integer textColorHighlight;

        @Nullable
        private Integer fontFamily;

        @Nullable
        private Boolean bold;

        @Nullable
        private Boolean allCaps;

        public Builder() {
        }

        public Builder(@NonNull TextConfiguration textConfiguration) {
            textConfiguration(textConfiguration);
        }

        public Builder textConfiguration(@NonNull TextConfiguration textConfiguration) {
            this.textSize = textConfiguration.textSize;
            this.textTypeFaceStyle = textConfiguration.textTypeFaceStyle;
            this.textColor = textConfiguration.textColor;
            this.backgroundColor = textConfiguration.backgroundColor;
            this.textAlignment = textConfiguration.textAlignment;
            this.hintColor = textConfiguration.hintColor;
            this.textColorLink = textConfiguration.textColorLink;
            this.textColorHighlight = textConfiguration.textColorHighlight;
            this.fontFamily = textConfiguration.fontFamily;
            this.bold = textConfiguration.bold;
            this.allCaps = textConfiguration.allCaps;
            return this;
        }

        public Builder textSize(@Nullable Float textSize) {
            this.textSize = textSize;
            return this;
        }

        public Builder textSize(@Nullable Integer textSize) {
            if (textSize != null) {
                this.textSize = (float) textSize;
            } else {
                this.textSize = null;
            }
            return this;
        }

        public Builder textTypeFaceStyle(@Nullable Integer textTypeFaceStyle) {
            this.textTypeFaceStyle = textTypeFaceStyle;
            return this;
        }

        public Builder textColor(@Nullable ColorStateList textColor) {
            this.textColor = textColor;
            return this;
        }

        public Builder textColor(@NonNull String textColor) {
            this.textColor = ColorStateList.valueOf(Color.parseColor(textColor));
            return this;
        }

        public Builder backgroundColor(@Nullable ColorConfiguration backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder textAlignment(@Nullable Integer textAlignment) {
            this.textAlignment = textAlignment;
            return this;
        }

        public Builder hintColor(@Nullable ColorStateList hintColor) {
            this.hintColor = hintColor;
            return this;
        }

        public Builder hintColor(@Nullable String hintColor) {
            this.hintColor = ColorStateList.valueOf(Color.parseColor(hintColor));
            return this;
        }

        public Builder textColorLink(@Nullable ColorStateList textColorLink) {
            this.textColorLink = textColorLink;
            return this;
        }

        public Builder textColorHighlight(@Nullable Integer textColorHighlight) {
            this.textColorHighlight = textColorHighlight;
            return this;
        }

        public Builder fontFamily(@Nullable Integer fontFamily) {
            this.fontFamily = fontFamily;
            return this;
        }

        public Builder bold(@Nullable Boolean bold) {
            this.bold = bold;
            return this;
        }

        public Builder allCaps(@Nullable Boolean allCaps) {
            this.allCaps = allCaps;
            return this;
        }

        public TextConfiguration build() {
            return new TextConfiguration(this);
        }
    }

    public static TextConfiguration getDefaultTextConfiguration() {
        ResourceProvider resourceProvider = Dependencies.getResourceProvider();
        return new Builder()
                .textSize(resourceProvider.getDimension(R.dimen.glia_survey_default_text_size))
                .textColor(resourceProvider.getColorStateList(R.color.glia_base_dark_color))
                .bold(false)
                .allCaps(false)
                .build();
    }

    /* BEGIN: Parcelable related */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.textSize);
        dest.writeValue(this.textTypeFaceStyle);
        dest.writeParcelable(this.textColor, flags);
        dest.writeParcelable(this.backgroundColor, flags);
        dest.writeValue(this.textAlignment);
        dest.writeParcelable(this.hintColor, flags);
        dest.writeParcelable(this.textColorLink, flags);
        dest.writeValue(this.textColorHighlight);
        dest.writeValue(this.fontFamily);
        dest.writeValue(this.bold);
        dest.writeValue(this.allCaps);
    }

    protected TextConfiguration(Parcel in) {
        this.textSize = (Float) in.readValue(Float.class.getClassLoader());
        this.textTypeFaceStyle = (Integer) in.readValue(Integer.class.getClassLoader());
        this.textColor = in.readParcelable(ColorStateList.class.getClassLoader());
        this.backgroundColor = in.readParcelable(ColorConfiguration.class.getClassLoader());
        this.textAlignment = (Integer) in.readValue(Integer.class.getClassLoader());
        this.hintColor = in.readParcelable(ColorStateList.class.getClassLoader());
        this.textColorLink = in.readParcelable(ColorStateList.class.getClassLoader());
        this.textColorHighlight = (Integer) in.readValue(Integer.class.getClassLoader());
        this.fontFamily = (Integer) in.readValue(Integer.class.getClassLoader());
        this.bold = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.allCaps = (Boolean) in.readValue(Boolean.class.getClassLoader());
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
