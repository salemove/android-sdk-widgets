package com.glia.widgets.chat.model

import android.net.Uri
import androidx.annotation.StringDef
import com.glia.androidsdk.chat.SingleChoiceAttachment
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

    sealed interface ButtonType {
        data object BroadcastEvent : ButtonType
        data class PostBack(val singleChoiceAttachment: SingleChoiceAttachment) : ButtonType
        data class Phone(val uri: Uri) : ButtonType
        data class Email(val uri: Uri) : ButtonType
        data class Url(val uri: Uri) : ButtonType
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
    fun toResponse(): SingleChoiceAttachment = SingleChoiceAttachment.from(value, text)
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
