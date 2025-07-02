package com.glia.widgets.snapshotutils

import android.content.Context
import android.view.View
import androidx.annotation.DrawableRes
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.internal.dialog.model.Link
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.view.dialog.base.DialogType
import com.glia.widgets.view.dialog.base.DialogViewFactory
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.google.gson.JsonObject

internal interface SnapshotDialog : SnapshotTheme, SnapshotProviders {
    val title: LocaleString
        get() = LocaleString(R.string.dialog_title)

    val message: LocaleString
        get() = LocaleString(R.string.dialog_message)

    val positiveButtonText: LocaleString
        get() = LocaleString(R.string.dialog_positive_button)

    val negativeButtonText: LocaleString
        get() = LocaleString(R.string.dialog_negative_button)

    val poweredByText: LocaleString
        get() = LocaleString(R.string.general_powered_by_glia)

    val link1Text: LocaleString
        get() = LocaleString(R.string.dialog_link1_text)

    val link1Url: LocaleString
        get() = LocaleString(R.string.dialog_link1_url)

    val link1: Link
        get() = Link(link1Text, link1Url)

    val link2Text: LocaleString
        get() = LocaleString(R.string.dialog_link2_text)

    val link2Url: LocaleString
        get() = LocaleString(R.string.dialog_link2_url)

    val link2: Link
        get() = Link(link2Text, link2Url)

    @get:DrawableRes
    val icon: Int
        get() = R.drawable.test_ic_placeholder

    val buttonDescription: LocaleString
        get() = LocaleString(R.string.dialog_button_description)

    fun inflateView(context: Context, unifiedTheme: UnifiedTheme? = null, dialogType: DialogType): View {
        return DialogViewFactory(context, UiTheme(whiteLabel = unifiedTheme != null), unifiedTheme).createView(dialogType)
    }

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
