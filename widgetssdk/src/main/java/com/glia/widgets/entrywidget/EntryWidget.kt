package com.glia.widgets.entrywidget

import android.app.Activity
import android.content.Context
import android.view.View
import com.glia.widgets.entrywidget.adapter.EntryWidgetAdapter
import com.glia.widgets.launcher.ActivityLauncher
import com.glia.widgets.view.unifiedui.theme.UnifiedThemeManager

/**
 * An interface for interacting with the entry widget(pre-built UI for engagement type selection).
 */
interface EntryWidget {
    /**
     * Shows the entry widget.
     *
     * @param activity The Activity used to show the entry widget
     */
    fun show(activity: Activity)

    /**
     * Creates the entry widget view that might be easily embedded in a custom UI.
     *
     * @return The entry widget view.
     */
    fun createEntryWidgetView(context: Context): View

    /**
     * Hides the entry widget.
     */
    fun hide()
}

internal class EntryWidgetImpl(private val activityLauncher: ActivityLauncher, private val themeManager: UnifiedThemeManager) : EntryWidget {
    override fun show(activity: Activity) = activityLauncher.launchEntryWidget(activity)

    override fun createEntryWidgetView(context: Context): View {
        val adapter = EntryWidgetAdapter(EntryWidgetContract.ViewType.EMBEDDED_VIEW, themeManager.theme?.entryWidgetTheme)
        return EntryWidgetView(context, adapter)
    }

    override fun hide() {
        TODO("Will be implemented later, in scope of EntryWidget feature")
    }
}
