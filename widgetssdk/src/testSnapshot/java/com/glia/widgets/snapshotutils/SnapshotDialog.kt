package com.glia.widgets.snapshotutils

import android.content.Context
import android.view.View
import androidx.annotation.DrawableRes
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.view.dialog.base.DialogType
import com.glia.widgets.view.dialog.base.DialogViewFactory
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.google.gson.JsonObject

internal interface SnapshotDialog: SnapshotTheme {
    val title: String
        get() = "This is a title"

    val message: String
        get() = "This is a much longer message that will provide more detailed information."

    val positiveButtonText: String
        get() = "Confirm"

    val negativeButtonText: String
        get() = "Cancel"

    val poweredByText: String
        get() = "Powered by Glia"

    val link1Text: String
        get() = "Terms and Conditions"

    val link2Text: String
        get() = "Privacy Policies"

    @get:DrawableRes
    val icon: Int
        get() = R.drawable.test_ic_placeholder

    val buttonDescription: String
        get() = "button description"

    fun inflateView(context: Context, uiTheme: UiTheme = UiTheme(), unifiedTheme: UnifiedTheme? = null, dialogType: DialogType): View =
        DialogViewFactory(context, uiTheme, unifiedTheme).createView(dialogType)

    fun unifiedThemeWithoutDialog(): UnifiedTheme = unifiedTheme(R.raw.test_unified_config) { unifiedTheme ->
        unifiedTheme.remove("alert")
    }

    fun unifiedThemeWithoutDialogLinkButton(): UnifiedTheme = unifiedTheme(R.raw.test_unified_config) { unifiedTheme ->
        unifiedTheme.add(
            "alert",
            (unifiedTheme.remove("alert") as JsonObject).also {
                it.remove("linkButton")
            }
        )
    }
}
