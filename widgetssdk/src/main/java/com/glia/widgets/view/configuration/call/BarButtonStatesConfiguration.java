package com.glia.widgets.view.configuration.call;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BarButtonStatesConfiguration implements Parcelable {
    @Nullable
    private final BarButtonConfiguration inactive;

    @Nullable
    private final BarButtonConfiguration active;

    @Nullable
    private final BarButtonConfiguration selected;

    private BarButtonStatesConfiguration(@NonNull Builder builder) {
        this.inactive = builder.inactive;
        this.active = builder.active;
        this.selected = builder.selected;
    }

    @Nullable
    public BarButtonConfiguration getInactive() {
        return inactive;
    }

    @Nullable
    public BarButtonConfiguration getActive() {
        return active;
    }

    @Nullable
    public BarButtonConfiguration getSelected() {
        return selected;
    }

    public static class Builder {
        @Nullable
        private BarButtonConfiguration inactive;

        @Nullable
        private BarButtonConfiguration active;

        @Nullable
        private BarButtonConfiguration selected;

        public Builder setBarButtonStatesConfiguration(BarButtonStatesConfiguration configuration) {
            this.inactive = configuration.inactive;
            this.active = configuration.active;
            this.selected = configuration.selected;
            return this;
        }

        public Builder setInactive(@Nullable BarButtonConfiguration inactive) {
            this.inactive = inactive;
            return this;
        }

        public Builder setActive(@Nullable BarButtonConfiguration active) {
            this.active = active;
            return this;
        }

        public Builder setSelected(@Nullable BarButtonConfiguration selected) {
            this.selected = selected;
            return this;
        }

        public BarButtonStatesConfiguration build() {
            return new BarButtonStatesConfiguration(this);
        }
    }

    public static BarButtonStatesConfiguration getDefaultBarButtonStatesConfiguration() {
        return new BarButtonStatesConfiguration.Builder()
                .setInactive(BarButtonConfiguration.getDefaultInactiveBarButtonConfiguration())
                .setActive(BarButtonConfiguration.getDefaultActiveBarButtonConfiguration())
                .setSelected(BarButtonConfiguration.getDefaultSelectedBarButtonConfiguration())
                .build();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.inactive, flags);
        dest.writeParcelable(this.active, flags);
        dest.writeParcelable(this.selected, flags);
    }

    protected BarButtonStatesConfiguration(Parcel in) {
        this.inactive = in.readParcelable(BarButtonConfiguration.class.getClassLoader());
        this.active = in.readParcelable(BarButtonConfiguration.class.getClassLoader());
        this.selected = in.readParcelable(BarButtonConfiguration.class.getClassLoader());
    }

    public static final Parcelable.Creator<BarButtonStatesConfiguration> CREATOR = new Parcelable.Creator<BarButtonStatesConfiguration>() {
        @Override
        public BarButtonStatesConfiguration createFromParcel(Parcel source) {
            return new BarButtonStatesConfiguration(source);
        }

        @Override
        public BarButtonStatesConfiguration[] newArray(int size) {
            return new BarButtonStatesConfiguration[size];
        }
    };
}
