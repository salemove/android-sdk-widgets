package com.glia.widgets.dialog

import android.graphics.Typeface
import com.glia.widgets.SnapshotTest
import com.glia.widgets.snapshotutils.SnapshotDialog
import com.glia.widgets.view.dialog.base.DialogPayload
import com.glia.widgets.view.dialog.base.DialogType
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.glia.widgets.view.unifiedui.theme.alert.AlertTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import org.junit.Test

/** Dialogs swap in the theme font in code, and the JSON text style must still apply on top of it. */
internal class DialogCustomFontTest : SnapshotTest(theme = "Test_Glia_CustomFont"), SnapshotDialog {

    private val dialogType: DialogType = DialogType.OperatorEndedEngagement(
        DialogPayload.OperatorEndedEngagement(
            title = title,
            message = message,
            buttonText = positiveButtonText,
            buttonClickListener = {}
        )
    )

    @Test
    fun themeFont() {
        snapshot(inflateView(context = context, dialogType = dialogType))
    }

    @Test
    fun boldMessageFromJson() {
        val unifiedTheme = UnifiedTheme(alertTheme = AlertTheme(message = TextTheme(textStyle = Typeface.BOLD)))
        snapshot(inflateView(context = context, unifiedTheme = unifiedTheme, dialogType = dialogType))
    }
}
