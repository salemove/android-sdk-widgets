package com.glia.widgets.helper

import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import com.glia.androidsdk.Engagement.MediaType
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.chat.Intention
import com.glia.widgets.locale.LocaleString

/**
 * Argument keys for Fragment arguments.
 * Mirrors the Intent extra keys pattern but for Fragment arguments.
 */
internal object FragmentArgumentKeys {
    const val OPEN_CHAT_INTENTION = "open_chat_intention"
    const val MEDIA_TYPE = "media_type"
    const val IS_UPGRADE_TO_CALL = "is_upgrade_to_call"
    const val IMAGE_PREVIEW_IMAGE_ID = "image_preview_image_id"
    const val IMAGE_PREVIEW_IMAGE_NAME = "image_preview_image_name"
    const val IMAGE_PREVIEW_LOCAL_IMAGE_URI = "image_preview_local_image_uri"
    const val WEB_BROWSER_URL = "web_browser_url"
    const val WEB_BROWSER_TITLE = "web_browser_title"
    const val SURVEY = "survey"
}

/**
 * Helper object for creating type-safe Fragment arguments.
 *
 * Provides factory methods for creating argument Bundles for each Fragment type.
 * This replaces the Intent extras pattern with Fragment arguments pattern.
 */
internal object FragmentArgumentHelper {
    /**
     * Create arguments for ChatFragment.
     *
     * @param intention The chat intention (welcome screen, chat history, etc.)
     * @return Bundle with chat arguments
     */
    fun chatArguments(intention: Intention): Bundle {
        return bundleOf(
            FragmentArgumentKeys.OPEN_CHAT_INTENTION to intention.ordinal
        )
    }

    /**
     * Create arguments for CallFragment.
     *
     * @param mediaType The media type (audio or video), or null
     * @param upgradeToCall Whether this is an upgrade from chat to call
     * @return Bundle with call arguments
     */
    fun callArguments(mediaType: MediaType?, upgradeToCall: Boolean): Bundle {
        return bundleOf(
            FragmentArgumentKeys.MEDIA_TYPE to mediaType?.ordinal,
            FragmentArgumentKeys.IS_UPGRADE_TO_CALL to upgradeToCall
        )
    }

    /**
     * Create arguments for MessageCenterFragment.
     *
     * @param intention The chat intention for secure conversations
     * @return Bundle with message center arguments
     */
    fun messageCenterArguments(intention: Intention): Bundle {
        return bundleOf(
            FragmentArgumentKeys.OPEN_CHAT_INTENTION to intention.ordinal
        )
    }

    /**
     * Create arguments for ImagePreviewFragment with remote attachment.
     *
     * @param imageId The attachment file ID
     * @param imageName The attachment file name
     * @return Bundle with image preview arguments
     */
    fun imagePreviewArguments(imageId: String, imageName: String): Bundle {
        return bundleOf(
            FragmentArgumentKeys.IMAGE_PREVIEW_IMAGE_ID to imageId,
            FragmentArgumentKeys.IMAGE_PREVIEW_IMAGE_NAME to imageName
        )
    }

    /**
     * Create arguments for ImagePreviewFragment with local attachment.
     *
     * @param localImageUri The local image URI
     * @return Bundle with image preview arguments
     */
    fun imagePreviewArgumentsLocal(localImageUri: Uri): Bundle {
        return bundleOf(
            FragmentArgumentKeys.IMAGE_PREVIEW_LOCAL_IMAGE_URI to localImageUri
        )
    }

    /**
     * Create arguments for WebBrowserFragment.
     *
     * @param url The URL to load
     * @param title The localized title
     * @return Bundle with web browser arguments
     */
    fun webBrowserArguments(url: String, title: LocaleString): Bundle {
        return bundleOf(
            FragmentArgumentKeys.WEB_BROWSER_URL to url,
            FragmentArgumentKeys.WEB_BROWSER_TITLE to title
        )
    }

    /**
     * Create arguments for SurveyFragment.
     *
     * @param survey The survey to display
     * @return Bundle with survey arguments
     */
    fun surveyArguments(survey: Survey): Bundle {
        return bundleOf(
            FragmentArgumentKeys.SURVEY to survey
        )
    }
}

/**
 * Extension functions for retrieving Fragment arguments.
 */

/**
 * Get enum extra from Bundle by ordinal.
 *
 * @param key The argument key
 * @return The enum value, or null if not found or invalid
 */
internal inline fun <reified T : Enum<T>> Bundle.getEnumArgument(key: String): T? {
    val ordinal = getInt(key, -1)
    if (ordinal == -1) return null
    return enumValues<T>().getOrNull(ordinal)
}
