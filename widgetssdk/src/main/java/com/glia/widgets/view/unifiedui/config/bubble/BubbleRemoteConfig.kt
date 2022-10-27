package com.glia.widgets.view.unifiedui.config.bubble

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.base.ButtonRemoteConfig
import com.glia.widgets.view.unifiedui.config.chat.UserImageRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class BubbleRemoteConfig(

    @SerializedName("userImage")
    val userImage: UserImageRemoteConfig?,

    @SerializedName("badge")
    val badge: ButtonRemoteConfig?,

    @SerializedName("onHoldOverlay")
    val onHoldOverlay: OnHoldOverlayRemoteConfig?
): Parcelable
