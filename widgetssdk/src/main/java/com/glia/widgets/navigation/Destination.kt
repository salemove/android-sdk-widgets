package com.glia.widgets.navigation

import android.os.Parcelable
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.chat.Intention
import kotlinx.parcelize.Parcelize

/**
 * Represents navigation destinations within the SDK.
 *
 * Used by [HostActivity] and [Navigator] to navigate between SDK screens.
 */
internal sealed interface Destination : Parcelable {

    @Parcelize
    data class Chat(val intention: Intention) : Destination

    @Parcelize
    data class Call(val mediaType: String?) : Destination

    @Parcelize
    data class SurveyScreen(val survey: Survey) : Destination

    @Parcelize
    data class MessageCenter(val queueIds: List<String>?) : Destination

    @Parcelize
    data object MessageCenterConfirmation : Destination

    @Parcelize
    data class WebBrowser(val title: String, val url: String) : Destination

    @Parcelize
    data class ImagePreview(
        val imageId: String? = null,
        val imageName: String? = null,
        val localImageUri: String? = null
    ) : Destination

    @Parcelize
    data object VisitorCode : Destination
}