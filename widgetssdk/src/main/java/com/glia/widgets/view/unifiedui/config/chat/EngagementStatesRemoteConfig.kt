package com.glia.widgets.view.unifiedui.config.chat

import com.glia.widgets.view.unifiedui.theme.chat.EngagementStatesTheme
import com.google.gson.annotations.SerializedName

internal data class EngagementStatesRemoteConfig(

    @SerializedName("operator")
    val operatorRemoteConfig: OperatorRemoteConfig?,

    @SerializedName("queue")
    val queue: EngagementStateRemoteConfig?,

    @SerializedName("connecting")
    val connecting: EngagementStateRemoteConfig?,

    @SerializedName("connected")
    val connected: EngagementStateRemoteConfig?,

    @SerializedName("transferring")
    val transferring: EngagementStateRemoteConfig?,

    @SerializedName("onHold")
    val onHold: EngagementStateRemoteConfig?
) {
    fun toEngagementStatesTheme(): EngagementStatesTheme = EngagementStatesTheme(
        operator = operatorRemoteConfig?.toOperatorTheme(),
        queue = queue?.toEngagementStateTheme(),
        connecting = connecting?.toEngagementStateTheme(),
        connected = connected?.toEngagementStateTheme(),
        transferring = transferring?.toEngagementStateTheme(),
        onHold = onHold?.toEngagementStateTheme()
    )
}
