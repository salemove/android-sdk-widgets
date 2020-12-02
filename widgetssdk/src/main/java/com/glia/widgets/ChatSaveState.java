package com.glia.widgets;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

class ChatSaveState extends View.BaseSavedState {
    boolean started;
    UiTheme uiTheme;
    Integer defaultStatusbarColor;

    public ChatSaveState(Parcel source) {
        super(source);
        this.started = source.readInt() == 1;
        this.uiTheme = source.readParcelable(getClass().getClassLoader());
        int defaultStatusbarColorFromParcel = source.readInt();
        this.defaultStatusbarColor =
                defaultStatusbarColorFromParcel != 0 ? defaultStatusbarColorFromParcel : null;
    }

    public ChatSaveState(Parcelable superState) {
        super(superState);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(this.started ? 1 : 0);
        out.writeParcelable(uiTheme, flags);
        out.writeInt(this.defaultStatusbarColor != null ? this.defaultStatusbarColor : 0);
    }
}
