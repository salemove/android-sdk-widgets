package com.glia.widgets;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

class ChatSaveState extends View.BaseSavedState {
    boolean started;

    public ChatSaveState(Parcel source) {
        super(source);
        this.started = source.readInt() == 1;
    }

    public ChatSaveState(Parcelable superState) {
        super(superState);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(this.started ? 1 : 0);
    }
}
