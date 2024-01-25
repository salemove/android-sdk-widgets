package com.glia.widgets.core.secureconversations

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

internal class SendMessageRepository {
    private val _observable = BehaviorSubject.createDefault("")

    val observable: Observable<String> = _observable

    val value: String
        get() = _observable.value.orEmpty()

    fun onNextMessage(message: String) {
        _observable.onNext(message)
    }

    fun reset() {
        _observable.onNext("")
    }
}
