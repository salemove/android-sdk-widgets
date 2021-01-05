package com.glia.widgets;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

class ChatSaveState extends View.BaseSavedState {
    boolean started;
    Integer defaultStatusbarColor;
    String queueId;
    String companyName;
    boolean visible;
    boolean exitDialogShowing;
    boolean noOperatorsAvailableDialogShowing;
    boolean unexpectedErrorDialogShowing;

    public ChatSaveState(Parcel source) {
        super(source);
        this.started = source.readInt() == 1;
        int defaultStatusbarColorFromParcel = source.readInt();
        this.defaultStatusbarColor =
                defaultStatusbarColorFromParcel != 0 ? defaultStatusbarColorFromParcel : null;
        this.queueId = source.readString();
        this.companyName = source.readString();
        this.visible = source.readInt() == 1;
        this.exitDialogShowing = source.readInt() == 1;
        this.noOperatorsAvailableDialogShowing = source.readInt() == 1;
        this.unexpectedErrorDialogShowing = source.readInt() == 1;
    }

    public ChatSaveState(Parcelable superState) {
        super(superState);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(this.started ? 1 : 0);
        out.writeInt(this.defaultStatusbarColor != null ? this.defaultStatusbarColor : 0);
        out.writeString(this.queueId);
        out.writeString(this.companyName);
        out.writeInt(this.visible ? 1 : 0);
        out.writeInt(this.exitDialogShowing ? 1 : 0);
        out.writeInt(this.noOperatorsAvailableDialogShowing ? 1 : 0);
        out.writeInt(this.unexpectedErrorDialogShowing ? 1 : 0);
    }
}
