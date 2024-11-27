package com.glia.widgets.helper.rx

import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

fun <T: Any> Observable<T>.timeoutFirstWithDefaultUntilChanged(timeout: Long, unit: TimeUnit, defaultValue: T): Observable<T> {
    return this
        .firstOrError()
        .timeout(timeout, unit)
        .onErrorReturnItem(defaultValue)
        .flatMapObservable { firstValue ->
            this.startWithItem(firstValue)
        }
        .distinctUntilChanged()
}
