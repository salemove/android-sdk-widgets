package com.glia.widgets.view.configuration.call;

import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.widgets.R;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.ResourceProvider;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.view.configuration.TextConfiguration;

public class BarButtonConfiguration implements Parcelable {
    @Nullable
    private final Integer background;

    @Nullable
    private final Integer imageColor;

    @Nullable
    private final TextConfiguration title;

    @Nullable
    @DrawableRes
    private final Integer imageRes;

    private BarButtonConfiguration(@NonNull Builder builder) {
        this.background = builder.background;
        this.imageColor = builder.imageColor;
        this.title = builder.title;
        this.imageRes = builder.imageRes;
    }

    @Nullable
    public Integer getBackground() {
        return background;
    }

    @Nullable
    public Integer getImageColor() {
        return imageColor;
    }

    @Nullable
    public TextConfiguration getTitle() {
        return title;
    }

    @Nullable
    @DrawableRes
    public Integer getImageRes() {
        return imageRes;
    }

    public static class Builder {
        @Nullable
        private Integer background;

        @Nullable
        private Integer imageColor;

        @Nullable
        private TextConfiguration title;

        @Nullable
        @DrawableRes
        private Integer imageRes;

        public Builder setBarButtonConfiguration(@NonNull BarButtonConfiguration configuration) {
            this.background = configuration.background;
            this.imageColor = configuration.imageColor;
            this.title = configuration.title;
            this.imageRes = configuration.imageRes;
            return this;
        }

        public Builder setBackground(@Nullable Integer background) {
            this.background = background;
            return this;
        }

        public Builder setImageColor(@Nullable Integer imageColor) {
            this.imageColor = imageColor;
            return this;
        }

        public Builder setTitle(@Nullable TextConfiguration title) {
            this.title = title;
            return this;
        }

        public Builder setImageRes(@Nullable @DrawableRes Integer imageRes) {
            this.imageRes = imageRes;
            return this;
        }

        public BarButtonConfiguration build() {
            return new BarButtonConfiguration(this);
        }
    }

    public static BarButtonConfiguration getDefaultInactiveBarButtonConfiguration() {
        ResourceProvider resourceProvider = Dependencies.getResourceProvider();
        int background = Utils.applyAlpha(resourceProvider.getColor(R.color.glia_transparent_black_buttons_bg), 0.2f);
        int imageColor = Utils.applyAlpha(resourceProvider.getColor(R.color.glia_base_light_color), 0.2f);
        return new Builder()
                .setBackground(background)
                .setImageColor(imageColor)
                .setTitle(getDefaultTextConfiguration(resourceProvider))
                .build();
    }

    public static BarButtonConfiguration getDefaultActiveBarButtonConfiguration() {
        ResourceProvider resourceProvider = Dependencies.getResourceProvider();
        int background = Utils.applyAlpha(resourceProvider.getColor(R.color.glia_transparent_black_buttons_bg), 0.4f);
        int imageColor = resourceProvider.getColor(R.color.glia_base_light_color);
        return new Builder()
                .setBackground(background)
                .setImageColor(imageColor)
                .setTitle(getDefaultTextConfiguration(resourceProvider))
                .build();
    }

    public static BarButtonConfiguration getDefaultSelectedBarButtonConfiguration() {
        ResourceProvider resourceProvider = Dependencies.getResourceProvider();
        int background = Utils.applyAlpha(resourceProvider.getColor(R.color.glia_base_light_color), 0.9f);
        int imageColor = resourceProvider.getColor(R.color.glia_transparent_black_buttons_bg);
        return new Builder()
                .setBackground(background)
                .setImageColor(imageColor)
                .setTitle(getDefaultTextConfiguration(resourceProvider))
                .build();
    }

    private static TextConfiguration getDefaultTextConfiguration(ResourceProvider resourceProvider) {
        Integer color = resourceProvider.getColor(R.color.glia_base_light_color);
        return new TextConfiguration.Builder()
                .textColor(ColorStateList.valueOf(color))
                .build();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.background);
        dest.writeValue(this.imageColor);
        dest.writeParcelable(this.title, flags);
        dest.writeValue(this.imageRes);
    }

    protected BarButtonConfiguration(Parcel in) {
        this.background = (Integer) in.readValue(Integer.class.getClassLoader());
        this.imageColor = (Integer) in.readValue(Integer.class.getClassLoader());
        this.title = in.readParcelable(TextConfiguration.class.getClassLoader());
        this.imageRes = (Integer) in.readValue(Integer.class.getClassLoader());
    }

    public static final Creator<BarButtonConfiguration> CREATOR = new Creator<BarButtonConfiguration>() {
        @Override
        public BarButtonConfiguration createFromParcel(Parcel source) {
            return new BarButtonConfiguration(source);
        }

        @Override
        public BarButtonConfiguration[] newArray(int size) {
            return new BarButtonConfiguration[size];
        }
    };
}
