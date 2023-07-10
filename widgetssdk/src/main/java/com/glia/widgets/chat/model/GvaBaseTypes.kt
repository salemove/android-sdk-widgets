package com.glia.widgets.chat.model

import androidx.annotation.StringDef
import com.google.gson.annotations.SerializedName

internal object Gva {

    object Keys {
        const val TYPE = "type"
        const val CONTENT = "content"
        const val OPTIONS = "options"
        const val GALLERY_CARDS = "galleryCards"
    }

    enum class Type(val value: String) {
        PLAIN_TEXT("plainText"),
        PERSISTENT_BUTTONS("persistentButtons"),
        QUICK_REPLIES("quickReplies"),
        GALLERY_CARDS("galleryCards")
    }

    @StringDef(
        UrlTarget.MODAL,
        UrlTarget.SELF,
        UrlTarget.BLANK
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class UrlTarget {
        companion object {
            const val MODAL = "modal"
            const val SELF = "self"
            const val BLANK = "blank"
        }
    }
}

internal data class GvaButton(
    @SerializedName("text")
    val text: String = "",
    @SerializedName("value")
    val value: String = "",
    @SerializedName("url")
    val url: String? = null,
    @Gva.UrlTarget
    @SerializedName("urlTarget")
    val urlTarget: String? = null,
    @SerializedName("destinationPbBroadcastEvent")
    val destinationPbBroadcastEvent: String? = null,
    @SerializedName("transferPhoneNumber")
    val transferPhoneNumber: String? = null
) {
    val isPostBack: Boolean
        get() = url.isNullOrBlank()

    val isBroadCastEvent: Boolean
        get() = !destinationPbBroadcastEvent.isNullOrBlank()

//    fun toResponse(): SingleChoiceAttachment = SingleChoiceAttachment.from(text, value) TODO should be available with core sdk's next release.
}

internal data class GvaGalleryCard(
    @SerializedName("title")
    val title: String = "",
    @SerializedName("subtitle")
    val subtitle: String? = null,
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    @SerializedName("options")
    val options: List<GvaButton> = listOf()
)
