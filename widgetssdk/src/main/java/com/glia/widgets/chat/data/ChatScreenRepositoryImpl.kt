package com.glia.widgets.chat.data

import com.glia.widgets.helper.asStateFlowable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.BehaviorProcessor

internal class ChatScreenRepositoryImpl : ChatScreenRepository {
    private var fromCallScreen = false

    private val _isChatScreenOpenObservable: BehaviorProcessor<Boolean> = BehaviorProcessor.createDefault(false)
    override val isChatScreenOpenObservable: Flowable<Boolean> get() = _isChatScreenOpenObservable.asStateFlowable()

    override fun isFromCallScreen(): Boolean {
        return fromCallScreen
    }

    override fun setFromCallScreen(fromCallScreen: Boolean) {
        this.fromCallScreen = fromCallScreen
    }

    override fun setChatScreenOpen(chatScreenOpen: Boolean) {
        _isChatScreenOpenObservable.onNext(chatScreenOpen)
    }
}
