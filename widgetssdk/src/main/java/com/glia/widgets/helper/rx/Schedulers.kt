package com.glia.widgets.helper.rx

import io.reactivex.Scheduler

internal interface Schedulers {
    val computationScheduler: Scheduler?
    val mainScheduler: Scheduler?
}
