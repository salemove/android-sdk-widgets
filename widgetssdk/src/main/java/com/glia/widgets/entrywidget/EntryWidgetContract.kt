package com.glia.widgets.entrywidget

import android.app.Activity
import com.glia.widgets.base.BaseController
import com.glia.widgets.base.BaseView

internal interface EntryWidgetContract {

    enum class ViewType {
        BOTTOM_SHEET,
        EMBEDDED_VIEW,
        MESSAGING_LIVE_SUPPORT
    }

    enum class ItemType {
        CHAT,
        AUDIO_CALL,
        VIDEO_CALL,
        SECURE_MESSAGE,
        LOADING_STATE,
        EMPTY_STATE,
        ERROR_STATE,
        PROVIDED_BY
    }

    interface Controller : BaseController {
        fun setView(view: View)
        fun onItemClicked(itemType: ItemType)
    }

    interface View : BaseView<Controller> {
        fun showItems(items: List<ItemType>)
        fun dismiss()
        fun getActivity(): Activity
    }
}
