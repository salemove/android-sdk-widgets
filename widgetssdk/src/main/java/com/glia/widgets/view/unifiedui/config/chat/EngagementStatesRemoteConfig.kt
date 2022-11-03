package com.glia.widgets.view.unifiedui.config.chat

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
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
): Parcelable
