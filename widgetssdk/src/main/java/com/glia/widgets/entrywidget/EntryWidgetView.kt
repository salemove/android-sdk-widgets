package com.glia.widgets.entrywidget

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.di.Dependencies
import com.glia.widgets.entrywidget.adapter.EntryWidgetAdapter
import com.glia.widgets.entrywidget.adapter.EntryWidgetItemDecoration
import com.glia.widgets.helper.getDrawableCompat

/**
 * EntryWidgetView provides a way to display the entry points for the user to start a chat, audio call, video call, or secure messaging.
 */
internal class EntryWidgetView(
    context: Context,
    viewType: EntryWidgetContract.ViewType
) : RecyclerView(context, null, 0), EntryWidgetContract.View {
    var onDismissListener: (() -> Unit)? = null

    private lateinit var controller: EntryWidgetContract.Controller
    private val adapter = EntryWidgetAdapter(viewType).also { setAdapter(it) }

    init {
        layoutManager = LinearLayoutManager(context)
        getDrawableCompat(R.drawable.bg_entry_widget_divider)?.let {
            addItemDecoration(EntryWidgetItemDecoration(it))
        }
        adapter.onItemClickListener = {
            controller.onItemClicked(it)
        }

        setController(Dependencies.controllerFactory.entryWidgetController)
    }

    override fun setController(controller: EntryWidgetContract.Controller) {
        this.controller = controller
        controller.setView(this)
    }

    override fun showItems(items: List<EntryWidgetContract.ItemType>) {
        adapter.items = items
    }

    override fun dismiss() {
        onDismissListener?.invoke()
    }
}
