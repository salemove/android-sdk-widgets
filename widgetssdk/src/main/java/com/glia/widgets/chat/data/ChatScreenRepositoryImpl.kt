package com.glia.widgets.chat.data

internal class ChatScreenRepositoryImpl : ChatScreenRepository {
    private var fromCallScreen = false
    override fun isFromCallScreen(): Boolean {
        return fromCallScreen
    }

    override fun setFromCallScreen(fromCallScreen: Boolean) {
        this.fromCallScreen = fromCallScreen
    }
}
