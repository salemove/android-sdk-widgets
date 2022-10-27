package com.glia.widgets.view.unifiedui.config.chat

import com.glia.widgets.view.unifiedui.config.base.AlignmentTypeRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.chat.MessageBalloonTheme
import com.google.gson.annotations.SerializedName

internal data class MessageBalloonRemoteConfig(
    @SerializedName("background")
    val background: LayerRemoteConfig?,

    @SerializedName("text")
    val textRemoteConfig: TextRemoteConfig?,

    @SerializedName("status")
    val status: TextRemoteConfig?,

    @SerializedName("alignment")
    val alignmentTypeRemoteConfig: AlignmentTypeRemoteConfig?,

    @SerializedName("userImage")
    val userImageRemoteConfig: UserImageRemoteConfig?
) {
    fun toMessageBalloonTheme(): MessageBalloonTheme = MessageBalloonTheme(
        background = background?.toLayerTheme(),
        text = textRemoteConfig?.toTextTheme(),
        status = status?.toTextTheme(),
        alignment = alignmentTypeRemoteConfig?.nativeAlignment,
        userImage = userImageRemoteConfig?.toUserImageTheme()
    )
}