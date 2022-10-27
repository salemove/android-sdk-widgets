package com.glia.widgets.view.unifiedui.config.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ResponseCardRemoteConfig(

    @SerializedName("background")
    val background: LayerRemoteConfig?,

    @SerializedName("option")
    val option: ResponseCardOptionRemoteConfig?,

    @SerializedName("message")
    val message: MessageBalloonRemoteConfig?,
): Parcelable
