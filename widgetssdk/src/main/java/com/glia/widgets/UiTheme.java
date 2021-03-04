package com.glia.widgets;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
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
    private @DrawableRes
    final Integer iconAppBarBack;
    private @DrawableRes
    final Integer iconLeaveQueue;
    private @DrawableRes
    final Integer iconSendMessage;
    private @DrawableRes
    final Integer iconChatAudioUpgrade;
    private @DrawableRes
    final Integer iconUpgradeAudioDialog;
    private @DrawableRes
    final Integer iconCallAudioOn;
    private @DrawableRes
    final Integer iconChatVideoUpgrade;
    private @DrawableRes
    final Integer iconUpgradeVideoDialog;
    private @DrawableRes
    final Integer iconScreenSharingDialog;
    private @DrawableRes
    final Integer iconCallVideoOn;
    private @DrawableRes
    final Integer iconCallAudioOff;
    private @DrawableRes
    final Integer iconCallVideoOff;
    private @DrawableRes
    final Integer iconCallChat;
    private @DrawableRes
    final Integer iconCallSpeakerOn;
    private @DrawableRes
    final Integer iconCallSpeakerOff;
    private @DrawableRes
    final Integer iconCallMinimize;
    private @DrawableRes
    final Integer iconPlaceholder;

    private UiTheme(String appBarTitle,
                    Integer brandPrimaryColor,
                    Integer baseLightColor,
                    Integer baseDarkColor,
                    Integer baseNormalColor,
                    Integer baseShadeColor,
                    Integer systemAgentBubbleColor,
                    Integer fontRes,
                    Integer systemNegativeColor,
                    Integer iconAppBarBack,
                    Integer iconLeaveQueue,
                    Integer iconSendMessage,
                    Integer iconChatAudioUpgrade,
                    Integer iconUpgradeAudioDialog,
                    Integer iconCallAudioOn,
                    Integer iconChatVideoUpgrade,
                    Integer iconUpgradeVideoDialog,
                    Integer iconScreenSharingDialog,
                    Integer iconCallVideoOn,
                    Integer iconCallAudioOff,
                    Integer iconCallVideoOff,
                    Integer iconCallChat,
                    Integer iconCallSpeakerOn,
                    Integer iconCallSpeakerOff,
                    Integer iconCallMinimize,
                    Integer iconPlaceholder) {
        this.appBarTitle = appBarTitle;
        this.brandPrimaryColor = brandPrimaryColor;
        this.baseLightColor = baseLightColor;
        this.baseDarkColor = baseDarkColor;
        this.baseNormalColor = baseNormalColor;
        this.baseShadeColor = baseShadeColor;
        this.systemAgentBubbleColor = systemAgentBubbleColor;
        this.fontRes = fontRes;
        this.systemNegativeColor = systemNegativeColor;
        this.iconAppBarBack = iconAppBarBack;
        this.iconLeaveQueue = iconLeaveQueue;
        this.iconSendMessage = iconSendMessage;
        this.iconChatAudioUpgrade = iconChatAudioUpgrade;
        this.iconUpgradeAudioDialog = iconUpgradeAudioDialog;
        this.iconCallAudioOn = iconCallAudioOn;
        this.iconChatVideoUpgrade = iconChatVideoUpgrade;
        this.iconUpgradeVideoDialog = iconUpgradeVideoDialog;
        this.iconScreenSharingDialog = iconScreenSharingDialog;
        this.iconCallVideoOn = iconCallVideoOn;
        this.iconCallAudioOff = iconCallAudioOff;
        this.iconCallVideoOff = iconCallVideoOff;
        this.iconCallChat = iconCallChat;
        this.iconCallSpeakerOn = iconCallSpeakerOn;
        this.iconCallSpeakerOff = iconCallSpeakerOff;
        this.iconCallMinimize = iconCallMinimize;
        this.iconPlaceholder = iconPlaceholder;
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
        if (in.readByte() == 0) {
            iconAppBarBack = null;
        } else {
            iconAppBarBack = in.readInt();
        }
        if (in.readByte() == 0) {
            iconLeaveQueue = null;
        } else {
            iconLeaveQueue = in.readInt();
        }
        if (in.readByte() == 0) {
            iconSendMessage = null;
        } else {
            iconSendMessage = in.readInt();
        }
        if (in.readByte() == 0) {
            iconChatAudioUpgrade = null;
        } else {
            iconChatAudioUpgrade = in.readInt();
        }
        if (in.readByte() == 0) {
            iconUpgradeAudioDialog = null;
        } else {
            iconUpgradeAudioDialog = in.readInt();
        }
        if (in.readByte() == 0) {
            iconCallAudioOn = null;
        } else {
            iconCallAudioOn = in.readInt();
        }
        if (in.readByte() == 0) {
            iconChatVideoUpgrade = null;
        } else {
            iconChatVideoUpgrade = in.readInt();
        }
        if (in.readByte() == 0) {
            iconUpgradeVideoDialog = null;
        } else {
            iconUpgradeVideoDialog = in.readInt();
        }
        if (in.readByte() == 0) {
            iconScreenSharingDialog = null;
        } else {
            iconScreenSharingDialog = in.readInt();
        }
        if (in.readByte() == 0) {
            iconCallVideoOn = null;
        } else {
            iconCallVideoOn = in.readInt();
        }
        if (in.readByte() == 0) {
            iconCallAudioOff = null;
        } else {
            iconCallAudioOff = in.readInt();
        }
        if (in.readByte() == 0) {
            iconCallVideoOff = null;
        } else {
            iconCallVideoOff = in.readInt();
        }
        if (in.readByte() == 0) {
            iconCallChat = null;
        } else {
            iconCallChat = in.readInt();
        }
        if (in.readByte() == 0) {
            iconCallSpeakerOn = null;
        } else {
            iconCallSpeakerOn = in.readInt();
        }
        if (in.readByte() == 0) {
            iconCallSpeakerOff = null;
        } else {
            iconCallSpeakerOff = in.readInt();
        }
        if (in.readByte() == 0) {
            iconCallMinimize = null;
        } else {
            iconCallMinimize = in.readInt();
        }
        if (in.readByte() == 0) {
            iconPlaceholder = null;
        } else {
            iconPlaceholder = in.readInt();
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
        if (iconAppBarBack == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconAppBarBack);
        }
        if (iconLeaveQueue == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconLeaveQueue);
        }
        if (iconSendMessage == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconSendMessage);
        }
        if (iconChatAudioUpgrade == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconChatAudioUpgrade);
        }
        if (iconUpgradeAudioDialog == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconUpgradeAudioDialog);
        }
        if (iconCallAudioOn == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconCallAudioOn);
        }
        if (iconChatVideoUpgrade == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconChatVideoUpgrade);
        }
        if (iconUpgradeVideoDialog == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconUpgradeVideoDialog);
        }
        if (iconScreenSharingDialog == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconScreenSharingDialog);
        }
        if (iconCallVideoOn == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconCallVideoOn);
        }
        if (iconCallAudioOff == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconCallAudioOff);
        }
        if (iconCallVideoOff == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconCallVideoOff);
        }
        if (iconCallChat == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconCallChat);
        }
        if (iconCallSpeakerOn == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconCallSpeakerOn);
        }
        if (iconCallSpeakerOff == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconCallSpeakerOff);
        }
        if (iconCallMinimize == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconCallMinimize);
        }
        if (iconPlaceholder == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconPlaceholder);
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
        private @DrawableRes
        Integer iconAppBarBack;
        private @DrawableRes
        Integer iconLeaveQueue;
        private @DrawableRes
        Integer iconSendMessage;
        private @DrawableRes
        Integer iconChatAudioUpgrade;
        private @DrawableRes
        Integer iconUpgradeAudioDialog;
        private @DrawableRes
        Integer iconScreenSharingDialog;
        private @DrawableRes
        Integer iconCallAudioOn;
        private @DrawableRes
        Integer iconChatVideoUpgrade;
        private @DrawableRes
        Integer iconUpgradeVideoDialog;
        private @DrawableRes
        Integer iconCallVideoOn;
        private @DrawableRes
        Integer iconCallAudioOff;
        private @DrawableRes
        Integer iconCallVideoOff;
        private @DrawableRes
        Integer iconCallChat;
        private @DrawableRes
        Integer iconCallSpeakerOn;
        private @DrawableRes
        Integer iconCallSpeakerOff;
        private @DrawableRes
        Integer iconCallMinimize;
        private @DrawableRes
        Integer iconPlaceholder;

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

        public void setIconAppBarBack(@DrawableRes Integer iconAppBarBack) {
            this.iconAppBarBack = iconAppBarBack;
        }

        public void setIconLeaveQueue(@DrawableRes Integer iconLeaveQueue) {
            this.iconLeaveQueue = iconLeaveQueue;
        }

        public void setIconSendMessage(@DrawableRes Integer iconSendMessage) {
            this.iconSendMessage = iconSendMessage;
        }

        public void setIconChatAudioUpgrade(@DrawableRes Integer iconChatAudioUpgrade) {
            this.iconChatAudioUpgrade = iconChatAudioUpgrade;
        }

        public void setIconUpgradeAudioDialog(@DrawableRes Integer iconUpgradeAudioDialog) {
            this.iconUpgradeAudioDialog = iconUpgradeAudioDialog;
        }

        public void setIconCallAudioOn(@DrawableRes Integer iconCallAudioOn) {
            this.iconCallAudioOn = iconCallAudioOn;
        }

        public void setIconChatVideoUpgrade(@DrawableRes Integer iconChatVideoUpgrade) {
            this.iconChatVideoUpgrade = iconChatVideoUpgrade;
        }

        public void setIconUpgradeVideoDialog(@DrawableRes Integer iconUpgradeVideoDialog) {
            this.iconUpgradeVideoDialog = iconUpgradeVideoDialog;
        }

        public void setIconScreenSharingDialog(@DrawableRes Integer iconScreenSharingDialog) {
            this.iconScreenSharingDialog = iconScreenSharingDialog;
        }

        public void setIconCallVideoOn(@DrawableRes Integer iconCallVideoOn) {
            this.iconCallVideoOn = iconCallVideoOn;
        }

        public void setIconCallAudioOff(@DrawableRes Integer iconCallAudioOff) {
            this.iconCallAudioOff = iconCallAudioOff;
        }

        public void setIconCallVideoOff(@DrawableRes Integer iconCallVideoOff) {
            this.iconCallVideoOff = iconCallVideoOff;
        }

        public void setIconCallChat(@DrawableRes Integer iconCallChat) {
            this.iconCallChat = iconCallChat;
        }

        public void setIconCallSpeakerOn(@DrawableRes Integer iconCallSpeakerOn) {
            this.iconCallSpeakerOn = iconCallSpeakerOn;
        }

        public void setIconCallSpeakerOff(@DrawableRes Integer iconCallSpeakerOff) {
            this.iconCallSpeakerOff = iconCallSpeakerOff;
        }

        public void setIconCallMinimize(@DrawableRes Integer iconCallMinimize) {
            this.iconCallMinimize = iconCallMinimize;
        }

        public void setIconPlaceholder(@DrawableRes Integer iconPlaceholder) {
            this.iconPlaceholder = iconPlaceholder;
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
            this.iconAppBarBack = theme.iconAppBarBack;
            this.iconLeaveQueue = theme.iconLeaveQueue;
            this.iconSendMessage = theme.iconSendMessage;
            this.iconChatAudioUpgrade = theme.iconChatAudioUpgrade;
            this.iconUpgradeAudioDialog = theme.iconUpgradeAudioDialog;
            this.iconCallAudioOn = theme.iconCallAudioOn;
            this.iconChatVideoUpgrade = theme.iconChatVideoUpgrade;
            this.iconUpgradeVideoDialog = theme.iconUpgradeVideoDialog;
            this.iconScreenSharingDialog = theme.iconScreenSharingDialog;
            this.iconCallVideoOn = theme.iconCallVideoOn;
            this.iconCallAudioOff = theme.iconCallAudioOff;
            this.iconCallVideoOff = theme.iconCallVideoOff;
            this.iconCallChat = theme.iconCallChat;
            this.iconCallSpeakerOn = theme.iconCallSpeakerOn;
            this.iconCallSpeakerOff = theme.iconCallSpeakerOff;
            this.iconCallMinimize = theme.iconCallMinimize;
            this.iconPlaceholder = theme.iconPlaceholder;
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
                    systemNegativeColor,
                    iconAppBarBack,
                    iconLeaveQueue,
                    iconSendMessage,
                    iconChatAudioUpgrade,
                    iconUpgradeAudioDialog,
                    iconCallAudioOn,
                    iconChatVideoUpgrade,
                    iconUpgradeVideoDialog,
                    iconScreenSharingDialog,
                    iconCallVideoOn,
                    iconCallAudioOff,
                    iconCallVideoOff,
                    iconCallChat,
                    iconCallSpeakerOn,
                    iconCallSpeakerOff,
                    iconCallMinimize,
                    iconPlaceholder);
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

    public Integer getIconAppBarBack() {
        return iconAppBarBack;
    }

    public Integer getIconLeaveQueue() {
        return iconLeaveQueue;
    }

    public Integer getIconSendMessage() {
        return iconSendMessage;
    }

    public Integer getIconChatAudioUpgrade() {
        return iconChatAudioUpgrade;
    }

    public Integer getIconUpgradeAudioDialog() {
        return iconUpgradeAudioDialog;
    }

    public Integer getIconCallAudioOn() {
        return iconCallAudioOn;
    }

    public Integer getIconChatVideoUpgrade() {
        return iconChatVideoUpgrade;
    }

    public Integer getIconUpgradeVideoDialog() {
        return iconUpgradeVideoDialog;
    }

    public Integer getIconScreenSharingDialog() {
        return iconScreenSharingDialog;
    }

    public Integer getIconCallVideoOn() {
        return iconCallVideoOn;
    }

    public Integer getIconCallAudioOff() {
        return iconCallAudioOff;
    }

    public Integer getIconCallVideoOff() {
        return iconCallVideoOff;
    }

    public Integer getIconCallChat() {
        return iconCallChat;
    }

    public Integer getIconCallSpeakerOn() {
        return iconCallSpeakerOn;
    }

    public Integer getIconCallSpeakerOff() {
        return iconCallSpeakerOff;
    }

    public Integer getIconCallMinimize() {
        return iconCallMinimize;
    }

    public Integer getIconPlaceholder() {
        return iconPlaceholder;
    }
}
