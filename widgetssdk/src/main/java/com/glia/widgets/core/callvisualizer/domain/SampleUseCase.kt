package com.glia.widgets.core.callvisualizer.domain

import io.reactivex.Single

class SampleUseCase() {
    operator fun invoke(value: Int): Single<Int> {
        return Single.just(
            if (value % 4 == 0) {
                value / 4
            } else {
                value * 3
            }
        )
    }
}