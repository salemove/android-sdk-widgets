package com.glia.widgets.helper.rx;


import static io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.rxjava3.schedulers.Schedulers.io;

import io.reactivex.rxjava3.core.Scheduler;

/**
 * @hide
 */
public class GliaWidgetsSchedulers implements Schedulers {

    @Override
    public Scheduler getComputationScheduler() {
        return io();
    }

    @Override
    public Scheduler getMainScheduler() {
        return mainThread();
    }
}
