package com.glia.widgets.dialog

import com.glia.widgets.SnapshotTest
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.snapshotutils.SnapshotDialog
import com.glia.widgets.view.dialog.base.DialogPayload
import com.glia.widgets.view.dialog.base.DialogType
import org.junit.Test

class ConfirmationDialogTest : SnapshotTest(
    renderingMode = fullWidthRenderMode
), SnapshotDialog {

    private fun dialogType(
        link1Text: LocaleString? = this.link1Text,
        link2Text: LocaleString? = this.link2Text,
    ) = DialogType.Confirmation(
        DialogPayload.Confirmation(
            title = title,
            message = message,
            positiveButtonText = positiveButtonText,
            negativeButtonText = negativeButtonText,
            poweredByText = poweredByText,
            positiveButtonClickListener = {},
            negativeButtonClickListener = {},
            link1Text = link1Text,
            link2Text = link2Text,
            link1ClickListener = {},
            link2ClickListener = {}
        )
    )

    // MARK: tests with links

    @Test
    fun withDefaultTheme() {
        val view = inflateView(
            context = context,
            dialogType = dialogType()
        )
        snapshot(view)
    }

    @Test
    fun withUiTheme() {
        val view = inflateView(context = context,
            uiTheme = uiTheme(),
            dialogType = dialogType()
        )
        snapshot(view)
    }

    @Test
    fun withUnifiedTheme() {
        val view = inflateView(
            context = context,
            uiTheme = uiTheme(whiteLabel = false),
            unifiedTheme = unifiedTheme(),
            dialogType = dialogType()
        )
        snapshot(view)
    }

    @Test
    fun withUnifiedThemeWithoutDialogLinkButton() {
        val view = inflateView(
            context = context,
            uiTheme = uiTheme(whiteLabel = false),
            unifiedTheme = unifiedThemeWithoutDialogLinkButton(),
            dialogType = dialogType()
        )
        snapshot(view)
    }

    @Test
    fun withGlobalColors() {
        val view = inflateView(
            context = context,
            uiTheme = uiTheme(),
            unifiedTheme = unifiedThemeWithGlobalColors(),
            dialogType = dialogType()
        )
        snapshot(view)
    }

    // MARK: tests with first link

    @Test
    fun withLink1WithDefaultTheme() {
        val view = inflateView(
            context = context,
            dialogType = dialogType(link2Text = null)
        )
        snapshot(view)
    }

    @Test
    fun withLink1WithGlobalColors() {
        val view = inflateView(
            context = context,
            unifiedTheme = unifiedThemeWithGlobalColors(),
            dialogType = dialogType(link2Text = null)
        )
        snapshot(view)
    }

    // MARK: tests with second link

    @Test
    fun withLink2WithDefaultTheme() {
        val view = inflateView(
            context = context,
            dialogType = dialogType(link1Text = null)
        )
        snapshot(view)
    }

    @Test
    fun withLink2WithGlobalColors() {
        val view = inflateView(
            context = context,
            unifiedTheme = unifiedThemeWithGlobalColors(),
            dialogType = dialogType(link1Text = null)
        )
        snapshot(view)
    }

    // MARK: tests without links

    @Test
    fun withoutLinksWithDefaultTheme() {
        val view = inflateView(
            context = context,
            dialogType = dialogType(null, null)
        )
        snapshot(view)
    }

    @Test
    fun withoutLinksWithGlobalColors() {
        val view = inflateView(
            context = context,
            unifiedTheme = unifiedThemeWithGlobalColors(),
            dialogType = dialogType(null, null)
        )
        snapshot(view)
    }
}
