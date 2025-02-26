package com.glia.widgets.entrywidget

import android.app.Activity
import com.glia.widgets.base.BaseController
import com.glia.widgets.base.BaseView

private const val LOWEST_ORDER = Int.MAX_VALUE
private const val HIGHEST_ORDER = Int.MIN_VALUE

internal interface EntryWidgetContract {

    enum class ViewType {
        BOTTOM_SHEET,
        EMBEDDED_VIEW,
        MESSAGING_LIVE_SUPPORT
    }

    sealed class ItemType(private val order: Int = HIGHEST_ORDER) : Comparable<ItemType> {
        data object VideoCall : ItemType(order = 0)
        data object VideoCallOngoing : ItemType(order = 1)
        data object AudioCall : ItemType(order = 2)
        data object AudioCallOngoing : ItemType(order = 3)
        data object Chat : ItemType(order = 4)
        data object ChatOngoing : ItemType(order = 5)
        data class Messaging(val value: Int) : ItemType(order = 6)
        data class MessagingOngoing(val value: Int) : ItemType(order = 7)
        data object CallVisualizerOngoing : ItemType(order = 8)
        data object LoadingState : ItemType()
        data object EmptyState : ItemType()
        data object SdkNotInitializedState : ItemType()
        data object ErrorState : ItemType()
        data object PoweredBy : ItemType(order = LOWEST_ORDER)

        override fun compareTo(other: ItemType): Int {
            return this.order.compareTo(other.order)
        }
    }

    interface Controller : BaseController {
        fun setView(view: View, type: ViewType)
        fun onItemClicked(itemType: ItemType, activity: Activity)
    }

    interface View : BaseView<Controller> {
        val whiteLabel: Boolean
        fun showItems(items: List<ItemType>)
        fun dismiss()
        fun getActivity(): Activity
    }
}
