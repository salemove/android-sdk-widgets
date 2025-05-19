package com.glia.widgets.entrywidget

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.util.AttributeSet
import android.util.TypedValue
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.di.Dependencies
import com.glia.widgets.entrywidget.adapter.EntryWidgetAdapter
import com.glia.widgets.entrywidget.adapter.EntryWidgetItemDivider
import com.glia.widgets.helper.getDrawableCompat
import com.glia.widgets.helper.requireActivity
import com.glia.widgets.helper.wrapWithMaterialThemeOverlay
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.nullSafeMerge
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.entrywidget.EntryWidgetTheme
import com.glia.widgets.view.unifiedui.theme.entrywidget.MediaTypeItemsTheme
import com.glia.widgets.view.unifiedui.theme.securemessaging.SecureMessagingTheme

/**
 * EntryWidgetView provides a way to display the entry points for the user to start a chat, audio call, video call, or secure messaging.
 */
internal class EntryWidgetView : RecyclerView, EntryWidgetContract.View {

    var onDismissListener: (() -> Unit)? = null

    private var controller: EntryWidgetContract.Controller
    private var _viewAdapter: EntryWidgetAdapter? = null
    private val viewAdapter: EntryWidgetAdapter
        get() = _viewAdapter ?: throw IllegalStateException("Make sure adapter is set up before attempting to show any items")
    private var dividerView: EntryWidgetItemDivider? = null

    private val uiThemeWhiteLabel by lazy {
        TypedValue().run {
            context.theme.resolveAttribute(R.attr.whiteLabel, this, true)
            data != 0
        }
    }

    var unifiedThemeWhiteLabel: Boolean? = null

    override val whiteLabel by lazy {
        uiThemeWhiteLabel nullSafeMerge unifiedThemeWhiteLabel
    }

    init {
        controller = Dependencies.controllerFactory.entryWidgetController
        itemAnimator = null
        setupDefaultViewAppearance()
    }

    constructor(context: Context) : super(context.wrapWithMaterialThemeOverlay())

    constructor(context: Context, attrs: AttributeSet) : super(context.wrapWithMaterialThemeOverlay(), attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context.wrapWithMaterialThemeOverlay(), attrs, defStyle)


    private fun setupDefaultViewAppearance() {
        val value = TypedValue()
        context.theme.resolveAttribute(R.attr.gliaBaseLightColor, value, true)
        setBackgroundColor(value.data)
    }

    override fun setController(controller: EntryWidgetContract.Controller) {
        this.controller = controller
    }

    fun setAdapter(viewAdapter: EntryWidgetAdapter) {
        this._viewAdapter?.release()
        this._viewAdapter = viewAdapter

        super.setAdapter(viewAdapter)

        //For Chat screen we need to handle Item click inside ChatController
        if (viewAdapter.viewType != EntryWidgetContract.ViewType.MESSAGING_LIVE_SUPPORT) {
            viewAdapter.onItemClickListener = {
                controller.onItemClicked(it, context.requireActivity())
            }
        }

        val isBottomSheet = viewAdapter.viewType == EntryWidgetContract.ViewType.BOTTOM_SHEET
        // Entry Widget might be embedded in scrolling views, disabling the scroll for
        // itself would prevent unintended scrolling behavior in such cases.
        // Scrolling is only applicable for bottom sheet view.
        enableScrolling(isBottomSheet)

        applyTheme(null, null)
        controller.setView(this, viewAdapter.viewType)
    }

    fun setEntryWidgetTheme(entryWidgetTheme: EntryWidgetTheme?) {
        val backgroundTheme = entryWidgetTheme?.background
        val mediaTypeItemsTheme = entryWidgetTheme?.mediaTypeItems
        applyTheme(backgroundTheme, mediaTypeItemsTheme)
    }

    fun setSecureMessagingTheme(secureMessagingTheme: SecureMessagingTheme?) {
        val backgroundTheme = secureMessagingTheme?.topBannerBackground
        val mediaTypeItemsTheme = secureMessagingTheme?.mediaTypeItems
        applyTheme(backgroundTheme, mediaTypeItemsTheme)
    }

    override fun showItems(items: List<EntryWidgetContract.ItemType>) {
        viewAdapter.submitList(items)
    }

    override fun dismiss() {
        onDismissListener?.invoke()
    }

    override fun getActivity(): Activity {
        return context.requireActivity()
    }

    private fun enableScrolling(enable: Boolean) {
        if (!enable) {
            // setNestedScrollingEnabled() broke scrolling in some cases (bottom sheet).
            // By default, it is enabled. So we can only disable it if needed.
            isNestedScrollingEnabled = false
        }
        layoutManager = object : LinearLayoutManager(context) {
            override fun canScrollVertically() = enable
        }
    }

    private fun applyTheme(backgroundTheme: LayerTheme? = null, mediaTypeItemsTheme: MediaTypeItemsTheme? = null) {
        backgroundTheme?.let { applyLayerTheme(it) }
        dividerView?.let(::removeItemDecoration)
        dividerView = createDividerDrawable(mediaTypeItemsTheme?.dividerColor)?.let(::EntryWidgetItemDivider)
        dividerView?.let(::addItemDecoration)

    }

    private fun createDividerDrawable(dividerColor: ColorTheme?): Drawable? {
        return getDrawableCompat(R.drawable.bg_entry_widget_divider)
            ?.apply {
                dividerColor?.primaryColor?.let { setTint(it) }
            }
            ?.let {
                if (viewAdapter.viewType == EntryWidgetContract.ViewType.MESSAGING_LIVE_SUPPORT) {
                    it
                } else {
                    val padding = resources.getDimension(R.dimen.glia_large).toInt()
                    InsetDrawable(it, padding, 0, padding, 0)
                }
            }
    }
}
