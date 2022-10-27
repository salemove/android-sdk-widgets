package com.glia.widgets.view.unifiedui.config.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.bubble.OnHoldOverlayRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class BubbleRemoteConfig(

    @SerializedName("userImage")
    val userImage: UserImageRemoteConfig?,

    @SerializedName("badge")
    val badgeRemoteConfig: BadgeRemoteConfig?,

    @SerializedName("onHoldOverlay")
    val onHoldOverlay: OnHoldOverlayRemoteConfig?,

    ) : Parcelable
