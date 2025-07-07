package com.glia.widgets.dialog

import com.glia.widgets.SnapshotTest
import com.glia.widgets.snapshotutils.SnapshotDialog
import com.glia.widgets.view.dialog.base.DialogPayload
import com.glia.widgets.view.dialog.base.DialogType
import org.junit.Test

internal class AlertDialogTest : SnapshotTest(
    renderingMode = fullWidthRenderMode
), SnapshotDialog {
    private val payloadWithButton: DialogPayload.AlertDialog = DialogPayload.AlertDialog(
        title = title,
        message = message,
        buttonVisible = true,
        buttonDescription = buttonDescription,
        buttonClickListener = {}
    )
    private val payloadWithoutButton: DialogPayload.AlertDialog = DialogPayload.AlertDialog(
        title = title,
        message = message
    )

    @Test
    fun withDefaultTheme() {
        val view = inflateView(context = context, dialogType = DialogType.AlertDialog(payloadWithoutButton))
        snapshot(view)
    }

    @Test
    fun withUnifiedTheme() {
        val view = inflateView(context = context, unifiedTheme(), dialogType = DialogType.AlertDialog(payloadWithoutButton))
        snapshot(view)
    }

    @Test
    fun withGlobalColors() {
        val view = inflateView(
            context = context,
            unifiedThemeWithGlobalColors(),
            dialogType = DialogType.AlertDialog(payloadWithoutButton)
        )
        snapshot(view)
    }

    @Test
    fun withDefaultThemeAndButton() {
        val view = inflateView(context = context, dialogType = DialogType.AlertDialog(payloadWithButton))
        snapshot(view)
    }

    @Test
    fun withUnifiedThemeAndButton() {
        val view =
            inflateView(context = context, unifiedTheme = unifiedTheme(), dialogType = DialogType.AlertDialog(payloadWithButton))
        snapshot(view)
    }

    @Test
    fun withGlobalColorsAndButton() {
        val view = inflateView(
            context = context,
            unifiedTheme = unifiedThemeWithGlobalColors(),
            dialogType = DialogType.AlertDialog(payloadWithButton)
        )
        snapshot(view)
    }
}
