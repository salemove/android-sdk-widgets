package com.glia.widgets.view.unifieduiconfig.component.base;

import android.os.Parcel;

public class SizeImpl implements Size.Sp, Size.Dp {
    private final float sizeDimension;
    private final float size;

    public SizeImpl(float sizeDimension, float size) {
        this.sizeDimension = sizeDimension;
        this.size = size;
    }

    public float getSizeDimension() {
        return sizeDimension;
    }

    @Override
    public float getSizePx() {
        return size;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.sizeDimension);
        dest.writeFloat(this.size);
    }

    protected SizeImpl(Parcel in) {
        this.sizeDimension = in.readFloat();
        this.size = in.readFloat();
    }

    public static final Creator<SizeImpl> CREATOR = new Creator<SizeImpl>() {
        @Override
        public SizeImpl createFromParcel(Parcel source) {
            return new SizeImpl(source);
        }

        @Override
        public SizeImpl[] newArray(int size) {
            return new SizeImpl[size];
        }
    };
}
