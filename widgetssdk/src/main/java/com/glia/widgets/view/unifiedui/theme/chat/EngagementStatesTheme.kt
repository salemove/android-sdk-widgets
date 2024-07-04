package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge

internal data class EngagementStatesTheme(
    val operator: OperatorTheme? = null,
    val queue: EngagementStateTheme? = null,
    val connecting: EngagementStateTheme? = null,
    val connected: EngagementStateTheme? = null,
    val transferring: EngagementStateTheme? = null,
    val onHold: EngagementStateTheme? = null
) : Mergeable<EngagementStatesTheme> {
    override fun merge(other: EngagementStatesTheme): EngagementStatesTheme = EngagementStatesTheme(
        operator = operator merge other.operator,
        queue = queue merge other.queue,
        connecting = connecting merge other.connecting,
        connected = connected merge other.connected,
        transferring = transferring merge other.transferring,
        onHold = onHold merge other.onHold
    )
}
