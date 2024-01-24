package com.glia.widgets.chat

import com.glia.widgets.SnapshotTest
import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.snapshotutils.SnapshotChatView
import org.junit.Test

internal class ChatViewSnapshotTest : SnapshotTest(), SnapshotChatView {

    @Test
    fun initialState() {
        snapshot(
            setupView(
                chatState = ChatState()
            ).root
        )
    }
}
