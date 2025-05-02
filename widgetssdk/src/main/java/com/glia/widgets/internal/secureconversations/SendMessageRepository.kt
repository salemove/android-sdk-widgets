package com.glia.widgets.internal.secureconversations

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject


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
