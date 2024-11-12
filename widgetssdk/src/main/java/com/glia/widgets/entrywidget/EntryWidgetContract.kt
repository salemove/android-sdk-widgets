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

    sealed class ItemType {
        data object Chat : ItemType()
        data object AudioCall : ItemType()
        data object VideoCall : ItemType()
        data class Messaging(val value: Int) : ItemType()
        data object LoadingState : ItemType()
        data object EmptyState : ItemType()
        data object SdkNotInitializedState : ItemType()
        data object ErrorState : ItemType()
        data object ProvidedBy : ItemType()
    }

    interface Controller : BaseController {
        fun setView(view: View)
        fun onItemClicked(itemType: ItemType, activity: Activity)
    }

    interface View : BaseView<Controller> {
        fun showItems(items: List<ItemType>)
        fun dismiss()
        fun getActivity(): Activity
    }
}
