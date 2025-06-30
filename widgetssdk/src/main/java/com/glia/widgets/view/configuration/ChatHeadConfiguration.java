package com.glia.widgets.view.configuration;

import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.helper.Logger;

/**
 * Please use remote configurations {@link com.glia.widgets.GliaWidgetsConfig.Builder#setUiJsonRemoteConfig(String)}
 */
@Deprecated
public class ChatHeadConfiguration implements Parcelable {
    private final Integer operatorPlaceholderBackgroundColor;
    private final Integer operatorPlaceholderIcon;
    private final Integer operatorPlaceholderIconTintList;
    private final Integer badgeBackgroundTintList;
    private final Integer badgeTextColor;
    private final Integer backgroundColorRes;
    private final Integer iconOnHold;
    private final Integer iconOnHoldTintList;

    private ChatHeadConfiguration(
            Builder builder
    ) {
        operatorPlaceholderBackgroundColor = builder.operatorPlaceholderBackgroundColor;
        operatorPlaceholderIcon = builder.operatorPlaceholderIcon;
        operatorPlaceholderIconTintList = builder.operatorPlaceholderIconTintList;
        badgeBackgroundTintList = builder.badgeBackgroundTintList;
        badgeTextColor = builder.badgeTextColor;
        backgroundColorRes = builder.backgroundColorRes;
        iconOnHold = builder.iconOnHold;
        iconOnHoldTintList = builder.iconOnHoldTintList;
    }

    protected ChatHeadConfiguration(Parcel in) {
        if (in.readByte() == 0) {
            operatorPlaceholderBackgroundColor = null;
        } else {
            operatorPlaceholderBackgroundColor = in.readInt();
        }
        if (in.readByte() == 0) {
            operatorPlaceholderIcon = null;
        } else {
            operatorPlaceholderIcon = in.readInt();
        }
        if (in.readByte() == 0) {
            operatorPlaceholderIconTintList = null;
        } else {
            operatorPlaceholderIconTintList = in.readInt();
        }
        if (in.readByte() == 0) {
            badgeBackgroundTintList = null;
        } else {
            badgeBackgroundTintList = in.readInt();
        }
        if (in.readByte() == 0) {
            badgeTextColor = null;
        } else {
            badgeTextColor = in.readInt();
        }
        if (in.readByte() == 0) {
            backgroundColorRes = null;
        } else {
            backgroundColorRes = in.readInt();
        }
        if (in.readByte() == 0) {
            iconOnHold = null;
        } else {
            iconOnHold = in.readInt();
        }
        if (in.readByte() == 0) {
            iconOnHoldTintList = null;
        } else {
            iconOnHoldTintList = in.readInt();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (operatorPlaceholderBackgroundColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(operatorPlaceholderBackgroundColor);
        }
        if (operatorPlaceholderIcon == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(operatorPlaceholderIcon);
        }
        if (operatorPlaceholderIconTintList == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(operatorPlaceholderIconTintList);
        }
        if (badgeBackgroundTintList == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(badgeBackgroundTintList);
        }
        if (badgeTextColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(badgeTextColor);
        }
        if (backgroundColorRes == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(backgroundColorRes);
        }
        if (iconOnHold == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconOnHold);
        }
        if (iconOnHoldTintList == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconOnHoldTintList);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ChatHeadConfiguration> CREATOR = new Creator<ChatHeadConfiguration>() {
        @Override
        public ChatHeadConfiguration createFromParcel(Parcel in) {
            return new ChatHeadConfiguration(in);
        }

        @Override
        public ChatHeadConfiguration[] newArray(int size) {
            return new ChatHeadConfiguration[size];
        }
    };

    public Integer getOperatorPlaceholderIcon() {
        return operatorPlaceholderIcon;
    }

    public Integer getOperatorPlaceholderBackgroundColor() {
        return operatorPlaceholderBackgroundColor;
    }

    public Integer getOperatorPlaceholderIconTintList() {
        return operatorPlaceholderIconTintList;
    }

    public Integer getBadgeBackgroundTintList() {
        return badgeBackgroundTintList;
    }

    public Integer getBadgeTextColor() {
        return badgeTextColor;
    }

    public Integer getBackgroundColorRes() {
        return backgroundColorRes;
    }

    public Integer getIconOnHold() {
        return iconOnHold;
    }

    public Integer getIconOnHoldTintList() {
        return iconOnHoldTintList;
    }

    /**
     * Please use remote configurations {@link com.glia.widgets.GliaWidgetsConfig.Builder#setUiJsonRemoteConfig(String)}
     */
    @Deprecated
    public static class Builder {
        private final String TAG = ChatHeadConfiguration.Builder.class.getSimpleName();

        private Integer operatorPlaceholderBackgroundColor;
        private Integer operatorPlaceholderIcon;
        private Integer operatorPlaceholderIconTintList;
        private Integer badgeBackgroundTintList;
        private Integer badgeTextColor;
        private Integer backgroundColorRes;
        private Integer iconOnHold;
        private Integer iconOnHoldTintList;

        public Builder() {
        }

        public Builder(ChatHeadConfiguration buildTime) {
            operatorPlaceholderBackgroundColor = buildTime.operatorPlaceholderBackgroundColor;
            operatorPlaceholderIcon = buildTime.operatorPlaceholderIcon;
            operatorPlaceholderIconTintList = buildTime.operatorPlaceholderIconTintList;
            badgeBackgroundTintList = buildTime.badgeBackgroundTintList;
            badgeTextColor = buildTime.badgeTextColor;
            backgroundColorRes = buildTime.backgroundColorRes;
            iconOnHold = buildTime.iconOnHold;
            iconOnHoldTintList = buildTime.iconOnHoldTintList;
        }

        public Builder operatorPlaceholderBackgroundColor(Integer operatorPlaceholderBackgroundColor) {
            this.operatorPlaceholderBackgroundColor = operatorPlaceholderBackgroundColor;
            return this;
        }

        public Builder operatorPlaceholderIcon(Integer operatorPlaceholderIcon) {
            this.operatorPlaceholderIcon = operatorPlaceholderIcon;
            return this;
        }

        public Builder operatorPlaceholderIconTintList(Integer operatorPlaceholderIconTintList) {
            this.operatorPlaceholderIconTintList = operatorPlaceholderIconTintList;
            return this;
        }

        public Builder badgeBackgroundTintList(Integer badgeBackgroundTintList) {
            this.badgeBackgroundTintList = badgeBackgroundTintList;
            return this;
        }

        public Builder badgeTextColor(Integer badgeTextColor) {
            this.badgeTextColor = badgeTextColor;
            return this;
        }

        public Builder backgroundColorRes(Integer backgroundColorRes) {
            this.backgroundColorRes = backgroundColorRes;
            return this;
        }

        public Builder iconOnHold(Integer iconOnHold) {
            this.iconOnHold = iconOnHold;
            return this;
        }

        public Builder iconOnHoldTintList(Integer iconOnHoldTintList) {
            this.iconOnHoldTintList = iconOnHoldTintList;
            return this;
        }

        public ChatHeadConfiguration build() {
            Logger.logDeprecatedClassUse(ChatHeadConfiguration.class.getSimpleName() + "." + TAG);
            return new ChatHeadConfiguration(this);
        }
    }

    public static Builder builder(ChatHeadConfiguration configuration) {
        return new ChatHeadConfiguration.Builder(configuration);
    }

    public static Builder builder() {
        return new Builder();
    }
}
