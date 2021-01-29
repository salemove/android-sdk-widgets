package com.glia.widgets;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorRes;
import androidx.annotation.FontRes;

public class UiTheme implements Parcelable {

    private final String appBarTitle;
    private @ColorRes
    final Integer brandPrimaryColor;
    private @ColorRes
    final Integer baseLightColor;
    private @ColorRes
    final Integer baseDarkColor;
    private @ColorRes
    final Integer baseNormalColor;
    private @ColorRes
    final Integer baseShadeColor;
    private @ColorRes
    final Integer systemAgentBubbleColor;
    private @FontRes
    final Integer fontRes;
    private @ColorRes
    final Integer systemNegativeColor;

    private UiTheme(String appBarTitle,
                    Integer brandPrimaryColor,
                    Integer baseLightColor,
                    Integer baseDarkColor,
                    Integer baseNormalColor,
                    Integer baseShadeColor,
                    Integer systemAgentBubbleColor,
                    Integer fontRes,
                    Integer systemNegativeColor) {
        this.appBarTitle = appBarTitle;
        this.brandPrimaryColor = brandPrimaryColor;
        this.baseLightColor = baseLightColor;
        this.baseDarkColor = baseDarkColor;
        this.baseNormalColor = baseNormalColor;
        this.baseShadeColor = baseShadeColor;
        this.systemAgentBubbleColor = systemAgentBubbleColor;
        this.fontRes = fontRes;
        this.systemNegativeColor = systemNegativeColor;
    }

    protected UiTheme(Parcel in) {
        appBarTitle = in.readString();
        if (in.readByte() == 0) {
            brandPrimaryColor = null;
        } else {
            brandPrimaryColor = in.readInt();
        }
        if (in.readByte() == 0) {
            baseLightColor = null;
        } else {
            baseLightColor = in.readInt();
        }
        if (in.readByte() == 0) {
            baseDarkColor = null;
        } else {
            baseDarkColor = in.readInt();
        }
        if (in.readByte() == 0) {
            baseNormalColor = null;
        } else {
            baseNormalColor = in.readInt();
        }
        if (in.readByte() == 0) {
            baseShadeColor = null;
        } else {
            baseShadeColor = in.readInt();
        }
        if (in.readByte() == 0) {
            systemAgentBubbleColor = null;
        } else {
            systemAgentBubbleColor = in.readInt();
        }
        if (in.readByte() == 0) {
            fontRes = null;
        } else {
            fontRes = in.readInt();
        }
        if (in.readByte() == 0) {
            systemNegativeColor = null;
        } else {
            systemNegativeColor = in.readInt();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(appBarTitle);
        if (brandPrimaryColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(brandPrimaryColor);
        }
        if (baseLightColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(baseLightColor);
        }
        if (baseDarkColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(baseDarkColor);
        }
        if (baseNormalColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(baseNormalColor);
        }
        if (baseShadeColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(baseShadeColor);
        }
        if (systemAgentBubbleColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(systemAgentBubbleColor);
        }
        if (fontRes == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(fontRes);
        }
        if (systemNegativeColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(systemNegativeColor);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UiTheme> CREATOR = new Creator<UiTheme>() {
        @Override
        public UiTheme createFromParcel(Parcel in) {
            return new UiTheme(in);
        }

        @Override
        public UiTheme[] newArray(int size) {
            return new UiTheme[size];
        }
    };

    public static class UiThemeBuilder {
        private String appBarTitle;
        private @ColorRes
        Integer brandPrimaryColor;
        private @ColorRes
        Integer baseLightColor;
        private @ColorRes
        Integer baseDarkColor;
        private @ColorRes
        Integer baseNormalColor;
        private @ColorRes
        Integer baseShadeColor;
        private @ColorRes
        Integer systemAgentBubbleColor;
        private @FontRes
        Integer fontRes;
        private @ColorRes
        Integer systemNegativeColor;

        public void setAppBarTitle(String appBarTitle) {
            this.appBarTitle = appBarTitle;
        }

        public void setFontRes(@FontRes Integer fontRes) {
            if (fontRes != null && fontRes != 0) {
                this.fontRes = fontRes;
            } else {
                this.fontRes = null;
            }
        }

        public void setBrandPrimaryColor(@ColorRes Integer brandPrimaryColor) {
            this.brandPrimaryColor = brandPrimaryColor;
        }

        public void setBaseLightColor(@ColorRes Integer baseLightColor) {
            this.baseLightColor = baseLightColor;
        }

        public void setBaseDarkColor(@ColorRes Integer baseDarkColor) {
            this.baseDarkColor = baseDarkColor;
        }

        public void setBaseNormalColor(Integer baseNormalColor) {
            this.baseNormalColor = baseNormalColor;
        }

        public void setBaseShadeColor(Integer baseShadeColor) {
            this.baseShadeColor = baseShadeColor;
        }

        public void setSystemAgentBubbleColor(@ColorRes Integer systemAgentBubbleColor) {
            this.systemAgentBubbleColor = systemAgentBubbleColor;
        }

        public void setSystemNegativeColor(@ColorRes Integer systemNegativeColor) {
            this.systemNegativeColor = systemNegativeColor;
        }

        public void setTheme(UiTheme theme) {
            this.appBarTitle = theme.appBarTitle;
            this.brandPrimaryColor = theme.brandPrimaryColor;
            this.baseLightColor = theme.baseLightColor;
            this.baseDarkColor = theme.baseDarkColor;
            this.baseNormalColor = theme.baseNormalColor;
            this.baseShadeColor = theme.baseShadeColor;
            this.systemAgentBubbleColor = theme.systemAgentBubbleColor;
            this.fontRes = theme.fontRes;
            this.systemNegativeColor = theme.systemNegativeColor;
        }

        public UiTheme build() {
            return new UiTheme(appBarTitle,
                    brandPrimaryColor,
                    baseLightColor,
                    baseDarkColor,
                    baseNormalColor,
                    baseShadeColor,
                    systemAgentBubbleColor,
                    fontRes,
                    systemNegativeColor);
        }
    }

    public String getAppBarTitle() {
        return appBarTitle;
    }

    public Integer getBrandPrimaryColor() {
        return brandPrimaryColor;
    }

    public Integer getBaseLightColor() {
        return baseLightColor;
    }

    public Integer getBaseDarkColor() {
        return baseDarkColor;
    }

    public Integer getBaseNormalColor() {
        return baseNormalColor;
    }

    public Integer getBaseShadeColor() {
        return baseShadeColor;
    }

    public Integer getSystemAgentBubbleColor() {
        return systemAgentBubbleColor;
    }

    public Integer getFontRes() {
        return fontRes;
    }

    public Integer getSystemNegativeColor() {
        return systemNegativeColor;
    }
}
