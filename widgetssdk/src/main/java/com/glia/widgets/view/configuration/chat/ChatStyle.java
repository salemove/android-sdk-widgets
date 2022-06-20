package com.glia.widgets.view.configuration.chat;

import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.view.configuration.LayerConfiguration;

public class ChatStyle implements Parcelable {
    public NavigationBarConfiguration navigationBar;
    public LayerConfiguration layer;
    public LayerConfiguration separator;
    public TextFieldConfiguration textField;
    public OperatorMessageConfiguration operatorMessage;
    public VisitorMessageConfiguration visitorMessage;
    public WelcomeViewConfiguration welcomeView;

    /* BEGIN: Parcelable related */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.navigationBar, flags);
        dest.writeParcelable(this.layer, flags);
        dest.writeParcelable(this.separator, flags);
        dest.writeParcelable(this.textField, flags);
        dest.writeParcelable(this.operatorMessage, flags);
        dest.writeParcelable(this.visitorMessage, flags);
    }

    public void readFromParcel(Parcel source) {
        this.navigationBar = source.readParcelable(NavigationBarConfiguration.class.getClassLoader());
        this.layer = source.readParcelable(LayerConfiguration.class.getClassLoader());
        this.separator = source.readParcelable(LayerConfiguration.class.getClassLoader());
        this.textField = source.readParcelable(TextFieldConfiguration.class.getClassLoader());
        this.operatorMessage = source.readParcelable(OperatorMessageConfiguration.class.getClassLoader());
        this.visitorMessage = source.readParcelable(VisitorMessageConfiguration.class.getClassLoader());
    }

    public ChatStyle() {
    }

    protected ChatStyle(Parcel in) {
        this.navigationBar = in.readParcelable(NavigationBarConfiguration.class.getClassLoader());
        this.layer = in.readParcelable(LayerConfiguration.class.getClassLoader());
        this.separator = in.readParcelable(LayerConfiguration.class.getClassLoader());
        this.textField = in.readParcelable(TextFieldConfiguration.class.getClassLoader());
        this.operatorMessage = in.readParcelable(OperatorMessageConfiguration.class.getClassLoader());
        this.visitorMessage = in.readParcelable(VisitorMessageConfiguration.class.getClassLoader());
    }

    public static final Creator<ChatStyle> CREATOR = new Creator<ChatStyle>() {
        @Override
        public ChatStyle createFromParcel(Parcel source) {
            return new ChatStyle(source);
        }

        @Override
        public ChatStyle[] newArray(int size) {
            return new ChatStyle[size];
        }
    };
    /* END: Parcelable related */
}
