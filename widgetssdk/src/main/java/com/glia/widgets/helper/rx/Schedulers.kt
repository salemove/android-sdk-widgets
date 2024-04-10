package com.glia.widgets.helper.rx

import io.reactivex.rxjava3.core.Scheduler


internal interface Schedulers {
    val computationScheduler: Scheduler
    val mainScheduler: Scheduler
}
