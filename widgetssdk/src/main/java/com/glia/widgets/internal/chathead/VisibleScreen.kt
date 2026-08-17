package com.glia.widgets.internal.chathead

import android.app.Activity
import com.glia.widgets.call.CallActivity
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.filepreview.ui.ImagePreviewActivity
import com.glia.widgets.helper.DialogHolderActivity
import com.glia.widgets.messagecenter.MessageCenterActivity

/**
 * The screen the visitor is currently looking at, as far as the bubble is concerned.
 *
 * [OTHER] covers integrator screens as well as Glia screens that carry no bubble-specific rule,
 * which matches the "name is not in the excluded set" semantics the bubble rules used before.
 */
internal enum class VisibleScreen {
    CHAT,
    CALL,
    IMAGE_PREVIEW,
    MESSAGE_CENTER,
    DIALOG_HOLDER,
    OTHER;

    /**
     * Glia's own screens already show the engagement, so a bubble on top of them would be redundant.
     * That is the only reason the entries above are named at all — being listed here *is* being
     * excluded — so the rule lives on the enum instead of in a set that repeats it.
     */
    val hidesBubble: Boolean get() = this != OTHER
}

/**
 * The single place that knows how activities map to [VisibleScreen].
 *
 * Mapping keys on the Activity rather than on the hosted view: all Glia views are internal and not
 * embeddable, and `GliaActivity<T>` makes the activity-to-view relation 1:1, so the information is
 * the same — but activities are what the lifecycle callbacks deliver.
 */
internal fun Activity.toVisibleScreen(): VisibleScreen = when (this) {
    is ChatActivity -> VisibleScreen.CHAT
    is CallActivity -> VisibleScreen.CALL
    is ImagePreviewActivity -> VisibleScreen.IMAGE_PREVIEW
    is MessageCenterActivity -> VisibleScreen.MESSAGE_CENTER
    is DialogHolderActivity -> VisibleScreen.DIALOG_HOLDER
    else -> VisibleScreen.OTHER
}
