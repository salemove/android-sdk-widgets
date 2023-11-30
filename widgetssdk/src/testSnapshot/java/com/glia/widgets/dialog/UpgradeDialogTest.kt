package com.glia.widgets.dialog

import com.glia.widgets.SnapshotTest
import com.glia.widgets.snapshotutils.SnapshotDialog
import com.glia.widgets.view.dialog.base.DialogPayload
import com.glia.widgets.view.dialog.base.DialogType
import org.junit.Test

class UpgradeDialogTest : SnapshotTest(), SnapshotDialog {

    private val dialogType: DialogType = DialogType.Upgrade(
        DialogPayload.Upgrade(
            title = title,
            positiveButtonText = positiveButtonText,
            negativeButtonText = negativeButtonText,
            poweredByText = poweredByText,
            iconRes = icon,
            positiveButtonClickListener = {},
            negativeButtonClickListener = {}

        )
    )

    @Test
    fun withDefaultTheme() {
        val view = inflateView(context = context, dialogType = dialogType)
        snapshotFullWidth(view)
    }

    @Test
    fun withUiTheme() {
        val view = inflateView(context = context, uiTheme = uiTheme(), dialogType = dialogType)
        snapshotFullWidth(view)
    }

    @Test
    fun withUnifiedTheme() {
        val view = inflateView(context = context, uiTheme = uiTheme(whiteLabel = false), unifiedTheme(), dialogType = dialogType)
        snapshotFullWidth(view)
    }

    @Test
    fun withGlobalColors() {
        val view = inflateView(context = context, uiTheme = uiTheme(), unifiedThemeWithGlobalColors(), dialogType = dialogType)
        snapshotFullWidth(view)
    }
}
