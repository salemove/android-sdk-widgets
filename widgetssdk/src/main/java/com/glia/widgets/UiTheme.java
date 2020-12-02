package com.glia.widgets;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorRes;
import androidx.annotation.FontRes;

public class UiTheme implements Parcelable {

    private final String title;
    private @ColorRes
    final Integer primaryBrandColorRes;
    private @ColorRes
    final Integer backgroundColorRes;
    private @ColorRes
    final Integer operatorMessageBgColorRes;
    private @FontRes
    final Integer fontRes;
    private @ColorRes
    final Integer primaryTextColorRes;

    private UiTheme(String title,
                    Integer primaryBrandColorRes,
                    Integer backgroundColorRes,
                    Integer operatorMessageBgColorRes,
                    Integer fontRes,
                    Integer primaryTextColorRes) {
        this.title = title;
        this.primaryBrandColorRes = primaryBrandColorRes;
        this.backgroundColorRes = backgroundColorRes;
        this.operatorMessageBgColorRes = operatorMessageBgColorRes;
        this.fontRes = fontRes;
        this.primaryTextColorRes = primaryTextColorRes;
    }

    protected UiTheme(Parcel in) {
        title = in.readString();
        if (in.readByte() == 0) {
            primaryBrandColorRes = null;
        } else {
            primaryBrandColorRes = in.readInt();
        }
        if (in.readByte() == 0) {
            backgroundColorRes = null;
        } else {
            backgroundColorRes = in.readInt();
        }
        if (in.readByte() == 0) {
            operatorMessageBgColorRes = null;
        } else {
            operatorMessageBgColorRes = in.readInt();
        }
        if (in.readByte() == 0) {
            fontRes = null;
        } else {
            fontRes = in.readInt();
        }
        if (in.readByte() == 0) {
            primaryTextColorRes = null;
        } else {
            primaryTextColorRes = in.readInt();
        }
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeInt(this.primaryBrandColorRes != null ? this.primaryBrandColorRes : -1);
        parcel.writeInt(this.backgroundColorRes != null ? this.backgroundColorRes : -1);
        parcel.writeInt(this.operatorMessageBgColorRes != null ? this.operatorMessageBgColorRes : -1);
        parcel.writeInt(this.fontRes != null ? this.fontRes : -1);
        parcel.writeInt(this.primaryTextColorRes != null ? this.primaryTextColorRes : -1);
    }

    public static class UiThemeBuilder {
        private String title;
        private @ColorRes
        Integer primaryBrandColorRes;
        private @ColorRes
        Integer backgroundColorRes;
        private @ColorRes
        Integer operatorMessageBgColorRes;
        private @FontRes
        Integer fontRes;
        private @ColorRes
        Integer primaryTextColorRes;

        public void setTitle(String title) {
            this.title = title;
        }

        public void setPrimaryBrandColorRes(Integer primaryBrandColorRes) {
            this.primaryBrandColorRes = primaryBrandColorRes;
        }

        public void setBackgroundColorRes(Integer backgroundColorRes) {
            this.backgroundColorRes = backgroundColorRes;
        }

        public void setOperatorMessageBgColorRes(Integer operatorMessageBgColorRes) {
            this.operatorMessageBgColorRes = operatorMessageBgColorRes;
        }

        public void setFontRes(Integer fontRes) {
            this.fontRes = fontRes;
        }

        public void setPrimaryTextColorRes(Integer primaryTextColorRes) {
            this.primaryTextColorRes = primaryTextColorRes;
        }

        public UiTheme build() {
            return new UiTheme(title,
                    primaryBrandColorRes,
                    backgroundColorRes,
                    operatorMessageBgColorRes,
                    fontRes,
                    primaryTextColorRes);
        }
    }

    public String getTitle() {
        return title;
    }

    public Integer getPrimaryBrandColorRes() {
        return primaryBrandColorRes;
    }

    public Integer getBackgroundColorRes() {
        return backgroundColorRes;
    }

    public Integer getOperatorMessageBgColorRes() {
        return operatorMessageBgColorRes;
    }

    public Integer getFontRes() {
        return fontRes;
    }

    public Integer getPrimaryTextColorRes() {
        return primaryTextColorRes;
    }
}
