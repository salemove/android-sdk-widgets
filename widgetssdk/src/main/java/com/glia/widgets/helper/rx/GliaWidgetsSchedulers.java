package com.glia.widgets.helper.rx;

import io.reactivex.Scheduler;

public class GliaWidgetsSchedulers implements Schedulers {

    @Override
    public Scheduler getComputationScheduler() {
        return io.reactivex.schedulers.Schedulers.io();
    }

    @Override
    public Scheduler getMainScheduler() {
        return io.reactivex.android.schedulers.AndroidSchedulers.mainThread();
    }
}
