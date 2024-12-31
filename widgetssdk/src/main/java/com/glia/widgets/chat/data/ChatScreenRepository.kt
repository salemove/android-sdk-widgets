package com.glia.widgets.chat.data

import io.reactivex.rxjava3.core.Flowable

internal interface ChatScreenRepository {
    val isChatScreenOpenObservable: Flowable<Boolean>
    fun setChatScreenOpen(chatScreenOpen: Boolean)
    fun isFromCallScreen(): Boolean
    fun setFromCallScreen(fromCallScreen: Boolean)
}
