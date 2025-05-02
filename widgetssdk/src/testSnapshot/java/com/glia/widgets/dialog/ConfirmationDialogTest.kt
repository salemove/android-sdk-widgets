package com.glia.widgets.dialog

import com.glia.widgets.SnapshotTest
import com.glia.widgets.internal.dialog.model.Link
import com.glia.widgets.snapshotutils.SnapshotDialog
import com.glia.widgets.view.dialog.base.DialogPayload
import com.glia.widgets.view.dialog.base.DialogType
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

internal class ConfirmationDialogTest : SnapshotTest(
    renderingMode = fullWidthRenderMode
), SnapshotDialog {

    private fun dialogType() = DialogType.Confirmation(
        DialogPayload.Confirmation(
            title = title,
            message = message,
            positiveButtonText = positiveButtonText,
            negativeButtonText = negativeButtonText,
            poweredByText = poweredByText,
            positiveButtonClickListener = {},
            negativeButtonClickListener = {},
            link1 = link1,
            link2 = link2,
            link1ClickListener = {},
            link2ClickListener = {}
        )
    )

    private fun disableLinkButton(vararg links: Link) {
        links.forEach { item ->
            localeProviderMock().stub {
                on { getStringInternal(item.url.stringKey) } doReturn ""
            }
        }
    }

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
    fun withUnifiedTheme() {
        val view = inflateView(
            context = context,
            unifiedTheme = unifiedTheme(),
            dialogType = dialogType()
        )
        snapshot(view)
    }

    @Test
    fun withUnifiedThemeWithoutDialogLinkButton() {
        val view = inflateView(
            context = context,
            unifiedTheme = unifiedThemeWithoutDialogLinkButton(),
            dialogType = dialogType()
        )
        snapshot(view)
    }

    @Test
    fun withGlobalColors() {
        val view = inflateView(
            context = context,
            unifiedTheme = unifiedThemeWithGlobalColors(),
            dialogType = dialogType()
        )
        snapshot(view)
    }

    // MARK: tests with first link

    @Test
    fun withLink1WithDefaultTheme() {
        disableLinkButton(link2)
        val view = inflateView(
            context = context,
            dialogType = dialogType()
        )
        snapshot(view)
    }

    @Test
    fun withLink1WithGlobalColors() {
        disableLinkButton(link2)
        val view = inflateView(
            context = context,
            unifiedTheme = unifiedThemeWithGlobalColors(),
            dialogType = dialogType()
        )
        snapshot(view)
    }

    // MARK: tests with second link

    @Test
    fun withLink2WithDefaultTheme() {
        disableLinkButton(link1)
        val view = inflateView(
            context = context,
            dialogType = dialogType()
        )
        snapshot(view)
    }

    @Test
    fun withLink2WithGlobalColors() {
        disableLinkButton(link1)
        val view = inflateView(
            context = context,
            unifiedTheme = unifiedThemeWithGlobalColors(),
            dialogType = dialogType()
        )
        snapshot(view)
    }

    // MARK: tests without links

    @Test
    fun withoutLinksWithDefaultTheme() {
        disableLinkButton(link1, link2)
        val view = inflateView(
            context = context,
            dialogType = dialogType()
        )
        snapshot(view)
    }

    @Test
    fun withoutLinksWithGlobalColors() {
        disableLinkButton(link1, link2)
        val view = inflateView(
            context = context,
            unifiedTheme = unifiedThemeWithGlobalColors(),
            dialogType = dialogType()
        )
        snapshot(view)
    }
}
