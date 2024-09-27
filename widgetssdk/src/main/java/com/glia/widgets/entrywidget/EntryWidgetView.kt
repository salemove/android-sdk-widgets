package com.glia.widgets.entrywidget

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.di.Dependencies
import com.glia.widgets.entrywidget.adapter.EntryWidgetAdapter
import com.glia.widgets.entrywidget.adapter.EntryWidgetItemDecoration
import com.glia.widgets.helper.getDrawableCompat
import com.glia.widgets.helper.wrapWithMaterialThemeOverlay
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.entrywidget.EntryWidgetTheme
import com.glia.widgets.view.unifiedui.theme.entrywidget.MediaTypeItemsTheme

/**
 * EntryWidgetView provides a way to display the entry points for the user to start a chat, audio call, video call, or secure messaging.
 */
internal class EntryWidgetView(
    context: Context,
    private val viewAdapter: EntryWidgetAdapter,
    backgroundTheme: LayerTheme? = null,
    mediaTypeItemsTheme: MediaTypeItemsTheme? = null,
) : RecyclerView(context.wrapWithMaterialThemeOverlay(), null, 0), EntryWidgetContract.View {

    constructor(
        context: Context,
        viewAdapter: EntryWidgetAdapter,
        entryWidgetTheme: EntryWidgetTheme?
    ) : this(
        context.wrapWithMaterialThemeOverlay(),
        viewAdapter,
        entryWidgetTheme?.background,
        entryWidgetTheme?.mediaTypeItems,
    )

    var onDismissListener: (() -> Unit)? = null

    private lateinit var controller: EntryWidgetContract.Controller

    init {
        setAdapter(viewAdapter)
        viewAdapter.onItemClickListener = {
            controller.onItemClicked(it)
        }

        disableScrolling()
        applyTheme(backgroundTheme, mediaTypeItemsTheme)
        setController(Dependencies.controllerFactory.entryWidgetController)
        itemAnimator = null
    }

    override fun setController(controller: EntryWidgetContract.Controller) {
        this.controller = controller
        controller.setView(this)
    }

    override fun showItems(items: List<EntryWidgetContract.ItemType>) {
        viewAdapter.submitList(items)
    }

    override fun dismiss() {
        onDismissListener?.invoke()
    }

    private fun disableScrolling() {
        // Entry Widget might be embedded in scrolling views, disabling the scroll for
        // itself would prevent unintended scrolling behavior in such cases
        isNestedScrollingEnabled = false
        layoutManager = object : LinearLayoutManager(context) {
            override fun canScrollVertically() = false
        }
    }

    private fun applyTheme(backgroundTheme: LayerTheme? = null, mediaTypeItemsTheme: MediaTypeItemsTheme? = null) {
        backgroundTheme?.let { applyLayerTheme(it) }
        getDrawableCompat(R.drawable.bg_entry_widget_divider)?.let {
            mediaTypeItemsTheme?.dividerColor?.let { dividerColor ->
                it.setTint(dividerColor.primaryColor)
            }
            addItemDecoration(EntryWidgetItemDecoration(it))
        }
    }
}
