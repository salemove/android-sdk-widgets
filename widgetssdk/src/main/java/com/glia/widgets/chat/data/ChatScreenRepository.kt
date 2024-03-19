package com.glia.widgets.chat.data

internal interface ChatScreenRepository {
    fun isFromCallScreen(): Boolean
    fun setFromCallScreen(fromCallScreen: Boolean)
}
