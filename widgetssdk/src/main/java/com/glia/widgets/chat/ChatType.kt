package com.glia.widgets.chat

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ChatType : Parcelable {
    LIVE_CHAT,
    SECURE_MESSAGING;
}
