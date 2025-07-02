package com.glia.widgets.entrywidget

import android.app.Activity
import android.content.Context
import android.view.View
import com.glia.widgets.chat.Intention
import com.glia.widgets.entrywidget.adapter.EntryWidgetAdapter
import com.glia.widgets.helper.wrapWithTheme
import com.glia.widgets.internal.secureconversations.domain.HasOngoingSecureConversationUseCase
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
     * @return New instance of an entry widget view.
     */
    fun getView(context: Context): View

    /**
     * Hides the entry widget.
     */
    fun hide()
}

internal class EntryWidgetImpl(
    private val activityLauncher: ActivityLauncher,
    private val themeManager: UnifiedThemeManager,
    private val entryWidgetHideController: EntryWidgetHideController,
    private val hasOngoingSecureConversationUseCase: HasOngoingSecureConversationUseCase
) : EntryWidget {

    override fun show(activity: Activity) {
        hasOngoingSecureConversationUseCase(
            onHasOngoingSecureConversation = { activityLauncher.launchChat(activity, Intention.SC_CHAT) },
            onNoOngoingSecureConversation = { activityLauncher.launchEntryWidget(activity) }
        )
    }

    override fun getView(context: Context): View {
        val unifiedTheme = themeManager.theme
        val adapter = EntryWidgetAdapter(EntryWidgetContract.ViewType.EMBEDDED_VIEW, unifiedTheme?.entryWidgetTheme)

        //Wrapping with Glia theme to make the Theme available for embedded view
        return EntryWidgetView(context.wrapWithTheme()).apply {
            unifiedThemeWhiteLabel = unifiedTheme?.isWhiteLabel
            setAdapter(adapter)
            setEntryWidgetTheme(unifiedTheme?.entryWidgetTheme)
        }
    }

    override fun hide() {
        entryWidgetHideController.hide()
    }
}
