package com.glia.widgets.dialog

import com.glia.widgets.SnapshotTest
import com.glia.widgets.snapshotutils.SnapshotDialog
import com.glia.widgets.view.dialog.base.DialogPayload
import com.glia.widgets.view.dialog.base.DialogType
import org.junit.Test

internal class ScreenSharingDialogTest : SnapshotTest(
    renderingMode = fullWidthRenderMode
), SnapshotDialog {

    private val dialogType: DialogType = DialogType.ScreenSharing(
        DialogPayload.ScreenSharing(
            title = title,
            message = message,
            positiveButtonText = positiveButtonText,
            negativeButtonText = negativeButtonText,
            poweredByText = poweredByText,
            positiveButtonClickListener = {},
            negativeButtonClickListener = {}

        )
    )

    @Test
    fun withDefaultTheme() {
        val view = inflateView(context = context, dialogType = dialogType)
        snapshot(view)
    }

    @Test
    fun withUnifiedTheme() {
        val view = inflateView(context = context, unifiedTheme(), dialogType = dialogType)
        snapshot(view)
    }

    @Test
    fun withGlobalColors() {
        val view = inflateView(context = context, unifiedThemeWithGlobalColors(), dialogType = dialogType)
        snapshot(view)
    }
}
