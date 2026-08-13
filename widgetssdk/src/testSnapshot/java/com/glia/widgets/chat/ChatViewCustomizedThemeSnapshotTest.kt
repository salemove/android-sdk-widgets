package com.glia.widgets.chat

import com.glia.widgets.SnapshotTest
import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.chat.model.OperatorStatusItem
import com.glia.widgets.snapshotutils.SnapshotChatScreen
import com.glia.widgets.snapshotutils.SnapshotChatView
import com.glia.widgets.snapshotutils.SnapshotStrings
import org.junit.Test

/**
 * `ChatViewSnapshotTest` rendered against `Test.Glia.Customized` - the whole-screen equivalent of an
 * integrator's `GliaTheme`.
 *
 * Its value is coverage of the *declarative* half of the mechanism: every colour and icon here
 * reaches the screen through a layout's `?attr/glia*`, with no code path left to re-apply it after
 * Phases 6 and 9 deleted the `UiTheme` fan-out. It also locks Phase 3's layout-precision work - the
 * fixture gives the coarse and precise attributes different colours, so a layout that fell back to
 * the coarse one would move its golden.
 *
 * The last variant additionally proves the ordering the whole migration rests on: JSON is applied in
 * code, after the theme, so it still wins.
 */
internal class ChatViewCustomizedThemeSnapshotTest : SnapshotTest(
    theme = "Test_Glia_Customized"
), SnapshotChatView, SnapshotChatScreen, SnapshotStrings {

    @Test
    fun initialState() {
        snapshot(
            setupView(
                chatState = ChatState()
            ).root
        )
    }

    private fun engagedState(): ChatState = ChatState()
        .initChat()
        .changeVisibility(true)
        .engagementStarted()
        .operatorConnected("Snap Test", null)
        .setSendButtonEnabled(true)
        .setIsAttachmentButtonEnabled(true)

    @Test
    fun engaged() {
        snapshot(
            setupView(
                chatState = engagedState(),
                message = mediumLengthTexts()[0]
            ).root
        )
    }

    @Test
    fun transferring() {
        snapshot(
            setupView(
                chatState = ChatState()
                    .changeVisibility(true)
                    .transferring(),
                chatItems = listOf(OperatorStatusItem.Transferring)
            ).root
        )
    }

    @Test
    fun engagedWithUnifiedTheme() {
        snapshot(
            setupView(
                chatState = engagedState(),
                message = mediumLengthTexts()[0],
                unifiedTheme = unifiedTheme()
            ).root
        )
    }
}
