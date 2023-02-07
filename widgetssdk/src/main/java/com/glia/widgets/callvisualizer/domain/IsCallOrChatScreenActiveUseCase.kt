package com.glia.widgets.callvisualizer.domain

import android.app.Activity
import com.glia.widgets.call.CallActivity
import com.glia.widgets.chat.ChatActivity

class IsCallOrChatScreenActiveUseCase {
    operator fun invoke(resumedActivity: Activity?) =
        (resumedActivity is CallActivity || resumedActivity is ChatActivity)
}
