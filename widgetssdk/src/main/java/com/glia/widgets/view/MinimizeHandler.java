package com.glia.widgets.view;

import com.glia.widgets.helper.Logger;

import java.util.ArrayList;
import java.util.List;

public class MinimizeHandler {
    private final static String TAG = "MinimizeHandler";

    private final List<OnMinimizeCalledListener> listeners = new ArrayList<>();

    public void addListener(OnMinimizeCalledListener listener) {
        Logger.d(TAG, "addListener");
        listeners.add(listener);
    }

    public void removeListener(OnMinimizeCalledListener listener){
        listeners.remove(listener);
    }

    public void clear() {
        Logger.d(TAG, "clear");
        listeners.clear();
    }

    public void minimize() {
        Logger.d(TAG, "minimizeCalled, number of listeners: " + listeners.size());
        for (OnMinimizeCalledListener listener : listeners) {
            listener.called();
        }
    }

    public interface OnMinimizeCalledListener {
        void called();
    }
}
