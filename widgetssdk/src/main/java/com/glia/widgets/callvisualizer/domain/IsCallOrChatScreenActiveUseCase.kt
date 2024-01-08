package com.glia.widgets.callvisualizer.domain

import android.app.Activity
import com.glia.widgets.call.CallActivity
import com.glia.widgets.chat.ChatActivity

internal class IsCallOrChatScreenActiveUseCase {
    operator fun invoke(resumedActivity: Activity?): Boolean = resumedActivity?.let {
        it is ChatActivity || it is CallActivity
    } ?: false
}
