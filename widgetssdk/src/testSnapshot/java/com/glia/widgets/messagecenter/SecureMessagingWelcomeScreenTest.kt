package com.glia.widgets.messagecenter

import com.android.ide.common.rendering.api.SessionParams
import com.glia.widgets.R
import com.glia.widgets.SnapshotTest
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.glia.widgets.snapshotutils.SnapshotMessageCenterView
import com.glia.widgets.snapshotutils.SnapshotStrings
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.junit.Test

internal class SecureMessagingWelcomeScreenTest : SnapshotTest(
    renderingMode = SessionParams.RenderingMode.NORMAL
), SnapshotMessageCenterView, SnapshotStrings {

    // MARK: Welcome screen without edit field

    @Test
    fun notAvailable() {
        snapshot(
            setupView().view
        )
    }

    @Test
    fun notAvailableWithGlobalColors() {
        snapshot(
            setupView(
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).view
        )
    }

    @Test
    fun notAvailableWithUnifiedTheme() {
        snapshot(
            setupView(
                unifiedTheme = unifiedTheme()
            ).view
        )
    }

    @Test
    fun notAvailableWithUnifiedThemeWithoutWelcomeScreen() {
        snapshot(
            setupView(
                unifiedTheme = unifiedThemeWithoutWelcomeScreen()
            ).view
        )
    }

    // MARK: Welcome screen with edit field

    private fun availableView(unifiedTheme: UnifiedTheme? = null) = setupView(
        state = MessageCenterState(
            showSendMessageGroup = true,
            sendMessageButtonState = MessageCenterState.ButtonState.NORMAL,
            addAttachmentButtonVisible = true,
            addAttachmentButtonEnabled = true
        ),
        fileAttachments = listOf(
            fileAttachment(
                displayName = "image.jpg",
                isImage = true
            ),
            fileAttachment(
                displayName = "photo.jpg",
                isImage = true,
                status = LocalAttachment.Status.SECURITY_SCAN
            ),
            fileAttachment(
                displayName = "test.doc",
                status = LocalAttachment.Status.READY_TO_SEND
            )
        ),
        imageResources = listOf(
            R.drawable.test_banner,
            R.drawable.test_launcher
        ),
        unifiedTheme = unifiedTheme
    ).view

    @Test
    fun available() {
        snapshot(
            availableView()
        )
    }

    @Test
    fun availableWithGlobalColors() {
        snapshot(
            availableView(
                unifiedTheme = unifiedThemeWithGlobalColors()
            )
        )
    }

    @Test
    fun availableWithUnifiedTheme() {
        snapshot(
            availableView(
                unifiedTheme = unifiedTheme()
            )
        )
    }

    @Test
    fun availableWithUnifiedThemeWithoutWelcomeScreen() {
        snapshot(
            availableView(
                unifiedTheme = unifiedThemeWithoutWelcomeScreen()
            )
        )
    }

    // MARK: Welcome screen with errors

    private fun limitErrorView(unifiedTheme: UnifiedTheme? = null) = setupView(
        state = MessageCenterState(
            showSendMessageGroup = true,
            addAttachmentButtonVisible = true,
            addAttachmentButtonEnabled = false,
            showMessageLimitError = true,
            sendMessageButtonState = MessageCenterState.ButtonState.DISABLE
        ),
        message = mediumLengthTexts().joinToString(),
        fileAttachments = listOf(
            fileAttachment(
                displayName = "photo.jpg",
                isImage = true,
                status = LocalAttachment.Status.ERROR_SECURITY_SCAN_FAILED
            ),
            fileAttachment(
                displayName = "test.doc",
                status = LocalAttachment.Status.ERROR_NETWORK_TIMEOUT
            )
        ),
        unifiedTheme = unifiedTheme
    ).view

    @Test
    fun limitError() {
        snapshot(
            limitErrorView()
        )
    }

    @Test
    fun limitErrorWithGlobalColors() {
        snapshot(
            limitErrorView(
                unifiedTheme = unifiedThemeWithGlobalColors()
            )
        )
    }

    @Test
    fun limitErrorWithUnifiedTheme() {
        snapshot(
            limitErrorView(
                unifiedTheme = unifiedTheme()
            )
        )
    }

    @Test
    fun limitErrorWithUnifiedThemeWithoutWelcomeScreen() {
        snapshot(
            limitErrorView(
                unifiedTheme = unifiedThemeWithoutWelcomeScreen()
            )
        )
    }

    // MARK: Sending message status

    private fun sendingView(unifiedTheme: UnifiedTheme? = null) = setupView(
        state = MessageCenterState(
            showSendMessageGroup = true,
            sendMessageButtonState = MessageCenterState.ButtonState.PROGRESS,
        ),
        message = mediumLengthTexts().first(),
        unifiedTheme = unifiedTheme
    ).view

    @Test
    fun sending() {
        snapshot(
            sendingView()
        )
    }

    @Test
    fun sendingWithGlobalColors() {
        snapshot(
            sendingView(
                unifiedTheme = unifiedThemeWithGlobalColors()
            )
        )
    }

    @Test
    fun sendingWithUnifiedTheme() {
        snapshot(
            sendingView(
                unifiedTheme = unifiedTheme()
            )
        )
    }

    @Test
    fun sendingWithUnifiedThemeWithoutWelcomeScreen() {
        snapshot(
            sendingView(
                unifiedTheme = unifiedThemeWithoutWelcomeScreen()
            )
        )
    }

}
