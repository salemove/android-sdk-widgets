package com.glia.widgets.view.configuration.call;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.widgets.R;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.ResourceProvider;
import com.glia.widgets.view.configuration.LayerConfiguration;
import com.glia.widgets.view.configuration.TextConfiguration;

public class CallStyle implements Parcelable {
    /* BEGIN: Remote configuration fields */
    @Nullable
    private final LayerConfiguration background;

    @Nullable
    private final TextConfiguration bottomText;

    @Nullable
    private final ButtonBarConfiguration buttonBar;

    @Nullable
    private final TextConfiguration duration;

    @Nullable
    private final TextConfiguration operator;

    @Nullable
    private final TextConfiguration topText;
    /* END: Remote configuration fields */

    private CallStyle(Builder builder) {
        this.background = builder.background;
        this.bottomText = builder.bottomText;
        this.buttonBar = builder.buttonBar;
        this.duration = builder.duration;
        this.operator = builder.operator;
        this.topText = builder.topText;
    }

    @Nullable
    public LayerConfiguration getBackground() {
        return background;
    }

    @Nullable
    public TextConfiguration getBottomText() {
        return bottomText;
    }

    @Nullable
    public ButtonBarConfiguration getButtonBar() {
        return buttonBar;
    }

    @Nullable
    public TextConfiguration getDuration() {
        return duration;
    }

    @Nullable
    public TextConfiguration getOperator() {
        return operator;
    }

    @Nullable
    public TextConfiguration getTopText() {
        return topText;
    }

    public static class Builder {
        @Nullable
        private LayerConfiguration background;

        @Nullable
        private TextConfiguration bottomText;

        @Nullable
        private ButtonBarConfiguration buttonBar;

        @Nullable
        private TextConfiguration duration;

        @Nullable
        private TextConfiguration operator;

        @Nullable
        private TextConfiguration topText;

        public Builder setCallStyle(@NonNull CallStyle callStyle) {
            this.background = callStyle.background;
            this.bottomText = callStyle.bottomText;
            this.buttonBar = callStyle.buttonBar;
            this.duration = callStyle.duration;
            this.operator = callStyle.operator;
            this.topText = callStyle.topText;
            return this;
        }

        public Builder setBackground(LayerConfiguration background) {
            this.background = background;
            return this;
        }

        public Builder setBottomText(TextConfiguration bottomText) {
            this.bottomText = bottomText;
            return this;
        }

        public Builder setButtonBar(@Nullable ButtonBarConfiguration buttonBar) {
            this.buttonBar = buttonBar;
            return this;
        }

        public Builder setDuration(@Nullable TextConfiguration duration) {
            this.duration = duration;
            return this;
        }

        public Builder setOperator(@Nullable TextConfiguration operator) {
            this.operator = operator;
            return this;
        }

        public Builder setTopText(@Nullable TextConfiguration topText) {
            this.topText = topText;
            return this;
        }

        public CallStyle build() {
            return new CallStyle(this);
        }
    }

    public static CallStyle getDefaultCallStyle() {
        ResourceProvider resourceProvider = Dependencies.getResourceProvider();
        return new CallStyle.Builder()
                .setBackground(new LayerConfiguration.Builder()
                        .backgroundColor(R.color.glia_transparent_black_bg)
                        .build()
                )
                .setBottomText(new TextConfiguration.Builder()
                        .textColor(resourceProvider.getColorStateList(R.color.glia_base_light_color))
                        .build()
                )
                .setDuration(new TextConfiguration.Builder()
                        .textColor(resourceProvider.getColorStateList(R.color.glia_base_light_color))
                        .build()
                )
                .setOperator(new TextConfiguration.Builder()
                        .textColor(resourceProvider.getColorStateList(R.color.glia_base_light_color))
                        .build()
                )
                .setTopText(new TextConfiguration.Builder()
                        .textColor(resourceProvider.getColorStateList(R.color.glia_base_light_color))
                        .build()
                )
                .setButtonBar(ButtonBarConfiguration.getDefaultButtonBarConfiguration())
                .build();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.background, flags);
        dest.writeParcelable(this.bottomText, flags);
        dest.writeParcelable(this.buttonBar, flags);
        dest.writeParcelable(this.duration, flags);
        dest.writeParcelable(this.operator, flags);
        dest.writeParcelable(this.topText, flags);
    }

    protected CallStyle(Parcel in) {
        this.background = in.readParcelable(LayerConfiguration.class.getClassLoader());
        this.bottomText = in.readParcelable(TextConfiguration.class.getClassLoader());
        this.buttonBar = in.readParcelable(ButtonBarConfiguration.class.getClassLoader());
        this.duration = in.readParcelable(TextConfiguration.class.getClassLoader());
        this.operator = in.readParcelable(TextConfiguration.class.getClassLoader());
        this.topText = in.readParcelable(TextConfiguration.class.getClassLoader());
    }

    public static final Creator<CallStyle> CREATOR = new Creator<CallStyle>() {
        @Override
        public CallStyle createFromParcel(Parcel source) {
            return new CallStyle(source);
        }

        @Override
        public CallStyle[] newArray(int size) {
            return new CallStyle[size];
        }
    };
}
