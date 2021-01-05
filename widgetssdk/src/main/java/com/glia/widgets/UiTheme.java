package com.glia.widgets;

import androidx.annotation.ColorRes;
import androidx.annotation.FontRes;

public class UiTheme {

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
                    Integer systemAgentBubbleColor,
                    Integer fontRes,
                    Integer systemNegativeColor) {
        this.appBarTitle = appBarTitle;
        this.brandPrimaryColor = brandPrimaryColor;
        this.baseLightColor = baseLightColor;
        this.baseDarkColor = baseDarkColor;
        this.baseNormalColor = baseNormalColor;
        this.systemAgentBubbleColor = systemAgentBubbleColor;
        this.fontRes = fontRes;
        this.systemNegativeColor = systemNegativeColor;
    }

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

        public void setSystemAgentBubbleColor(@ColorRes Integer systemAgentBubbleColor) {
            this.systemAgentBubbleColor = systemAgentBubbleColor;
        }

        public void setSystemNegativeColor(@ColorRes Integer systemNegativeColor) {
            this.systemNegativeColor = systemNegativeColor;
        }

        public UiTheme build() {
            return new UiTheme(appBarTitle,
                    brandPrimaryColor,
                    baseLightColor,
                    baseDarkColor,
                    baseNormalColor,
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
