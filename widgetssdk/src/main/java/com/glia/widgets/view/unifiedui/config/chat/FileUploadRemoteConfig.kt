package com.glia.widgets.view.unifiedui.config.chat

import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.chat.UploadFileTheme
import com.google.gson.annotations.SerializedName

internal data class FileUploadRemoteConfig(
    @SerializedName("text")
    val textRemoteConfig: TextRemoteConfig?,

    @SerializedName("info")
    val info: TextRemoteConfig?
) {
    fun toUploadFileTheme(): UploadFileTheme = UploadFileTheme(
        text = textRemoteConfig?.toTextTheme(),
        info = info?.toTextTheme()
    )
}
