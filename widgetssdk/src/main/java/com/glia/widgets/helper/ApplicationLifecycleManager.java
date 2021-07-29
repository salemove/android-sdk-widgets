package com.glia.widgets.helper;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.ProcessLifecycleOwner;

import java.util.ArrayList;
import java.util.List;


public class ApplicationLifecycleManager {
    private final List<LifecycleEventObserver> lifecycleEventObserverList = new ArrayList<>();
    private final Lifecycle lifecycle;

    public ApplicationLifecycleManager() {
        lifecycle = ProcessLifecycleOwner.get().getLifecycle();
    }

    public void addObserver(LifecycleEventObserver observer) {
        lifecycle.addObserver(observer);
        lifecycleEventObserverList.add(observer);
    }

    public void removeObserver(LifecycleEventObserver observer) {
        lifecycle.removeObserver(observer);
        lifecycleEventObserverList.remove(observer);
    }

    public void onDestroy() {
        for (LifecycleEventObserver observer : lifecycleEventObserverList) {
            lifecycle.removeObserver(observer);
        }
        lifecycleEventObserverList.clear();
    }
}
