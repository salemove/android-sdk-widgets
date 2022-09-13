package com.glia.widgets.view.unifiedui.config.chat

import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.chat.ResponseCardTheme
import com.google.gson.annotations.SerializedName

internal data class ResponseCardRemoteConfig(

    @SerializedName("background")
    val background: LayerRemoteConfig?,

    @SerializedName("option")
    val option: ResponseCardOptionRemoteConfig?,

    @SerializedName("text")
    val text: TextRemoteConfig?,

    @SerializedName("userImage")
    val userImage: UserImageRemoteConfig?
) {
    fun toResponseCardTheme(): ResponseCardTheme = ResponseCardTheme(
        background = background?.toLayerTheme(),
        option = option?.toResponseCardOptionTheme(),
        text = text?.toTextTheme(),
        userImage = userImage?.toUserImageTheme()
    )
}
