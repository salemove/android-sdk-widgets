package com.glia.exampleapp;

import com.glia.widgets.GliaWidgets;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        GliaWidgets.onAppCreate(this);
    }

}
