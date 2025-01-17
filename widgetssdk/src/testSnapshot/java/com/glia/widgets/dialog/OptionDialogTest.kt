package com.glia.widgets.dialog

import com.glia.widgets.SnapshotTest
import com.glia.widgets.snapshotutils.SnapshotDialog
import com.glia.widgets.view.dialog.base.DialogPayload
import com.glia.widgets.view.dialog.base.DialogType
import org.junit.Test

internal class OptionDialogTest : SnapshotTest(
    renderingMode = fullWidthRenderMode
), SnapshotDialog {

    private val dialogPayload = DialogPayload.Option(
        title = title,
        message = message,
        positiveButtonText = positiveButtonText,
        negativeButtonText = negativeButtonText,
        poweredByText = poweredByText,
        positiveButtonClickListener = {},
        negativeButtonClickListener = {}

    )

    private val default = DialogType.Option(dialogPayload)
    private val negativeNeutral = DialogType.OptionWithNegativeNeutral(dialogPayload)

    @Test
    fun withDefaultTheme() {
        val view = inflateView(context = context, dialogType = default)
        snapshot(view)
    }

    @Test
    fun withUnifiedTheme() {
        val view = inflateView(context = context, unifiedTheme(), dialogType = default)
        snapshot(view)
    }

    @Test
    fun withGlobalColors() {
        val view = inflateView(context = context, unifiedThemeWithGlobalColors(), dialogType = default)
        snapshot(view)
    }

    @Test
    fun negativeNeutralDefaultTheme() {
        val view = inflateView(context = context, dialogType = negativeNeutral)
        snapshot(view)
    }

    @Test
    fun negativeNeutralGlobalColors() {
        val view = inflateView(context = context, unifiedThemeWithGlobalColors(), dialogType = negativeNeutral)
        snapshot(view)
    }
}
