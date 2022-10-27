package com.glia.widgets.view.unifiedui.config.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class FileUploadRemoteConfig(
    @SerializedName("text")
    val textRemoteConfig: TextRemoteConfig?,

    @SerializedName("info")
    val info: TextRemoteConfig?
) : Parcelable
