package com.glia.widgets.helper.rx

import io.reactivex.rxjava3.core.Observable
import org.junit.Test
import java.util.concurrent.TimeUnit

class RxExtensionsTest {

    @Test
    fun `timeoutFirstWithDefaultUntilChanged emits items in right order when no timeout`() {
        Observable.just(0, 1, 2, 3, 4)
            .timeoutFirstWithDefaultUntilChanged(1, TimeUnit.SECONDS, 999)
            .doOnNext{ println("Emitted value: $it") }
            .test()
            .assertResult(0, 1, 2, 3, 4)
    }

    @Test
    fun `timeoutFirstWithDefaultUntilChanged emits items in right order on replay(1) when no timeout`() {
        Observable.just(0, 1, 2, 3, 4)
            .replay(1)
            .autoConnect()
            .timeoutFirstWithDefaultUntilChanged(1, TimeUnit.SECONDS, 999)
            .doOnNext{ println("Emitted value: $it") }
            .test()
            .assertResult(0, 1, 2, 3, 4)
    }

    @Test
    fun `timeoutFirstWithDefaultUntilChanged emits items in right order when timeout is triggered before first item`() {
        Observable.just(0, 1, 2, 3)
            .delay(200, TimeUnit.MILLISECONDS)
            .timeoutFirstWithDefaultUntilChanged(100, TimeUnit.MILLISECONDS, 999)
            .doOnNext{ println("Emitted value: $it") }
            .test()
            .awaitCount(5)
            .assertResult(999, 0, 1, 2, 3)
    }

    @Test
    fun `timeoutFirstWithDefaultUntilChanged emits items in right order when timeout is triggered on every item`() {
        Observable.interval(100, 100, TimeUnit.MILLISECONDS)
            .timeoutFirstWithDefaultUntilChanged(50, TimeUnit.MILLISECONDS, 999)
            .doOnNext{ println("Emitted value: $it") }
            .test()
            .awaitCount(5)
            .assertValues(999, 0, 1, 2, 3)
            .apply { dispose() }
            .awaitDone(1, TimeUnit.SECONDS)
    }

    @Test
    fun `timeoutFirstWithDefaultUntilChanged emits items in right order when timeout is triggered after first item`() {
        Observable.interval(0, 100, TimeUnit.MILLISECONDS)
            .timeoutFirstWithDefaultUntilChanged(50, TimeUnit.MILLISECONDS, 999)
            .doOnNext{ println("Emitted value: $it") }
            .test()
            .awaitCount(5)
            .assertValues(0, 1, 2, 3, 4)
            .apply { dispose() }
            .awaitDone(1, TimeUnit.SECONDS)
    }
}
