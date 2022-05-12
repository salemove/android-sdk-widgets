package com.glia.widgets.helper.rx;

import io.reactivex.Scheduler;

public interface Schedulers {
    Scheduler getComputationScheduler();

    Scheduler getMainScheduler();
}
