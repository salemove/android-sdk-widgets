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

    sealed class ItemType(private val order: Int) : Comparable<ItemType> {
        data object VideoCall : ItemType(0)
        data object AudioCall : ItemType(1)
        data object Chat : ItemType(2)
        data class Messaging(val value: Int) : ItemType(3)
        data object LoadingState : ItemType(10)
        data object EmptyState : ItemType(10)
        data object SdkNotInitializedState : ItemType(10)
        data object ErrorState : ItemType(10)
        data object ProvidedBy : ItemType(11)

        override fun compareTo(other: ItemType): Int {
            return this.order.compareTo(other.order)
        }
    }

    interface Controller : BaseController {
        fun setView(view: View, type: ViewType)
        fun onItemClicked(itemType: ItemType, activity: Activity)
    }

    interface View : BaseView<Controller> {
        fun showItems(items: List<ItemType>)
        fun dismiss()
        fun getActivity(): Activity
    }
}
