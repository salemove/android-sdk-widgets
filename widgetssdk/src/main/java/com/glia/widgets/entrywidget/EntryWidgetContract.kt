package com.glia.widgets.entrywidget

import com.glia.widgets.base.BaseController
import com.glia.widgets.base.BaseView

internal interface EntryWidgetContract {

    enum class ItemType {
        VIDEO_CALL,
        AUDIO_CALL,
        CHAT,
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
    }
}
