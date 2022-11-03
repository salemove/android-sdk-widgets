package com.glia.widgets.view.unifiedui.config.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.HeaderRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ChatRemoteConfig(
    @SerializedName("background")
    val background: LayerRemoteConfig?,

    @SerializedName("header")
    val headerRemoteConfig: HeaderRemoteConfig?,

    @SerializedName("operatorMessage")
    val operatorMessage: MessageBalloonRemoteConfig?,

    @SerializedName("visitorMessage")
    val visitorMessage: MessageBalloonRemoteConfig?,

    @SerializedName("connect")
    val connect: EngagementStatesRemoteConfig?,

    @SerializedName("input")
    val inputRemoteConfig: InputRemoteConfig?,

    @SerializedName("responseCard")
    val responseCardRemoteConfig: ResponseCardRemoteConfig?,

    @SerializedName("audioUpgrade")
    val audioUpgradeRemoteConfig: UpgradeRemoteConfig?,

    @SerializedName("videoUpgrade")
    val videoUpgradeRemoteConfig: UpgradeRemoteConfig?,

    @SerializedName("bubble")
    val bubbleRemoteConfig: BubbleRemoteConfig?,

    @SerializedName("attachmentSourceList")
    val attachmentSourceListRemoteConfig: AttachmentSourceListRemoteConfig?,

    @SerializedName("unreadIndicator")
    val unreadIndicator: BubbleRemoteConfig?,

    @SerializedName("typingIndicator")
    val typingIndicator: ColorLayerRemoteConfig?
): Parcelable