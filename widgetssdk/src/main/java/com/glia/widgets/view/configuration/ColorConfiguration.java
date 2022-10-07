package com.glia.widgets.view.configuration;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;

import com.glia.widgets.di.Dependencies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ColorConfiguration implements Parcelable {

    public enum Type {
        GRADIENT, FILL
    }

    @NonNull
    private final List<Integer> colorList;

    @NonNull
    private final Type type;

    private ColorConfiguration(Builder builder) {
        this.colorList = builder.colorList;
        this.type = builder.type;
    }

    public static class Builder {
        private List<Integer> colorList;
        private Type type;

        public Builder seColorConfiguration(ColorConfiguration configuration) {
            this.colorList = configuration.colorList;
            this.type = configuration.type;
            return this;
        }

        public Builder setColorResource(@ColorRes int colorResource) {
            int color = Dependencies.getResourceProvider().getColor(colorResource);
            this.colorList = Collections.singletonList(color);
            this.type = Type.FILL;
            return this;
        }

        public Builder setColor(String color) {
            this.colorList = Collections.singletonList(Color.parseColor(color));
            this.type = Type.FILL;
            return this;
        }

        public Builder setColor(int color) {
            this.colorList = Collections.singletonList(color);
            this.type = Type.FILL;
            return this;
        }

        public Builder setGradientColors(List<Integer> colorList) {
            this.colorList = colorList;
            this.type = Type.GRADIENT;
            return this;
        }

        public ColorConfiguration build() {
            return new ColorConfiguration(this);
        }
    }

    @NonNull
    public List<Integer> getColorList() {
        return colorList;
    }

    @Deprecated // Remove after refactoring Survey Screen
    @SuppressLint("ResourceType")
    public String getColor() {
        if (colorList.size() > 0) {
            return String.format("#%06X", (0xFFFFFF & colorList.get(0)));
        }
        return "#000000";
    }

    public ColorStateList getColorStateList() {
        return ColorStateList.valueOf(colorList.get(0));
    }

    @NonNull
    public Type getType() {
        return type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.colorList);
        dest.writeInt(this.type.ordinal());
    }

    protected ColorConfiguration(Parcel in) {
        this.colorList = new ArrayList<>();
        in.readList(this.colorList, Integer.class.getClassLoader());
        int tmpType = in.readInt();
        this.type = Type.values()[tmpType];
    }

    public static final Parcelable.Creator<ColorConfiguration> CREATOR = new Parcelable.Creator<ColorConfiguration>() {
        @Override
        public ColorConfiguration createFromParcel(Parcel source) {
            return new ColorConfiguration(source);
        }

        @Override
        public ColorConfiguration[] newArray(int size) {
            return new ColorConfiguration[size];
        }
    };
}
