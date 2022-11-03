package com.glia.widgets.view.unifiedui.config.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.AlignmentTypeRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class MessageBalloonRemoteConfig(
    @SerializedName("background")
    val background: LayerRemoteConfig?,

    @SerializedName("text")
    val textRemoteConfig: TextRemoteConfig?,

    @SerializedName("status")
    val status: TextRemoteConfig?,

    @SerializedName("alignment")
    val alignmentTypeRemoteConfig: AlignmentTypeRemoteConfig?,
) : Parcelable