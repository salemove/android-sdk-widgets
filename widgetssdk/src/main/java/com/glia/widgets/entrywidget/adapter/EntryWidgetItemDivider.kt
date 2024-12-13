package com.glia.widgets.entrywidget.adapter

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView

internal class EntryWidgetItemDivider(
    private val divider: Drawable
) : RecyclerView.ItemDecoration() {

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)

        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val lastIndex = parent.childCount - 1

        for (i in 0 until lastIndex) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            if (position == RecyclerView.NO_POSITION) {
                continue
            }

            val viewType = parent.adapter?.getItemViewType(position)

            if (isContactItem(viewType)) {
                val params = child.layoutParams as RecyclerView.LayoutParams

                val top = child.bottom + params.bottomMargin
                val bottom = top + divider.intrinsicHeight

                divider.setBounds(left, top, right, bottom)
                divider.draw(canvas)
            }
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) {
            return
        }
        val viewType = parent.adapter?.getItemViewType(position)
        if (isContactItem(viewType)) {
            outRect.bottom = divider.intrinsicHeight
        } else {
            outRect.bottom = 0
        }
    }

    private fun isContactItem(viewType: Int?): Boolean {
        return viewType == EntryWidgetAdapter.ViewType.LIVE_MEDIA_TYPE_ITEMS.ordinal ||
            viewType == EntryWidgetAdapter.ViewType.MESSAGING_MEDIA_TYPE_ITEM.ordinal ||
            viewType == EntryWidgetAdapter.ViewType.CALL_VISUALIZER_ITEM.ordinal
    }
}
