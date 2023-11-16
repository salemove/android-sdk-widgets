package com.glia.widgets.dialog

import com.glia.widgets.SnapshotTest
import com.glia.widgets.view.dialog.base.DialogPayload
import com.glia.widgets.view.dialog.base.DialogType
import org.junit.Test

internal class OperatorEndedEngagementTest : SnapshotTest(), DialogData {

    private val dialogType: DialogType = DialogType.OperatorEndedEngagement(
        DialogPayload.OperatorEndedEngagement(
            title = title,
            message = message,
            buttonText = positiveButtonText,
            buttonClickListener = {}
        )
    )

    @Test
    fun withDefaultTheme() {
        val view = inflateView(context = context, dialogType = dialogType)
        snapshot(view)
    }

    @Test
    fun withUiTheme() {
        val view = inflateView(context = context, uiTheme = uiTheme(), dialogType = dialogType)
        snapshot(view)
    }

    @Test
    fun withUnifiedTheme() {
        val view = inflateView(context = context, uiTheme = uiTheme(), unifiedTheme(), dialogType = dialogType)
        snapshot(view)
    }

    @Test
    fun withGlobalColors() {
        val view = inflateView(context = context, uiTheme = uiTheme(), unifiedThemeWithGlobalColors(), dialogType = dialogType)
        snapshot(view)
    }
}
