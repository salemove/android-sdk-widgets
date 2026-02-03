package com.glia.widgets.di

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.chat.Intention
import com.glia.widgets.filepreview.ui.ImagePreviewDialogFragment
import com.glia.widgets.messagecenter.MessageCenterConfirmationFragment
import com.glia.widgets.messagecenter.MessageCenterWelcomeFragment
import com.glia.widgets.survey.SurveyBottomSheetFragment
import com.glia.widgets.view.dialog.VisitorCodeDialogFragment
import com.glia.widgets.webbrowser.WebBrowserFragment

/**
 * Factory for creating Fragment instances with proper arguments.
 *
 * This centralizes Fragment creation logic, making it easier to:
 * - Maintain consistent argument handling
 * - Test Fragment creation
 * - Discover available Fragments
 *
 * Note: Methods will be implemented incrementally as each screen is migrated
 * to Fragment-based architecture in phases 5-12 of the MVI migration.
 */
internal class FragmentFactory {

    // Full-screen fragments

    /**
     * Creates a ChatFragment with the specified intention.
     *
     * Will be implemented in Phase 12 when ChatFragment is created.
     */
    fun createChatFragment(intention: Intention): Fragment {
        // TODO: Implement in Phase 12
        throw NotImplementedError("ChatFragment will be implemented in Phase 12")
    }

    /**
     * Creates a CallFragment with the specified media type.
     *
     * Will be implemented in Phase 11 when CallFragment is created.
     */
    fun createCallFragment(mediaType: String?): Fragment {
        // TODO: Implement in Phase 11
        throw NotImplementedError("CallFragment will be implemented in Phase 11")
    }

    /**
     * Creates a MessageCenterWelcomeFragment with the specified queue IDs.
     */
    fun createMessageCenterWelcomeFragment(queueIds: ArrayList<String>?): MessageCenterWelcomeFragment {
        return MessageCenterWelcomeFragment().apply {
            arguments = Bundle().apply {
                queueIds?.let { putStringArrayList(MessageCenterWelcomeFragment.ARG_QUEUE_IDS, it) }
            }
        }
    }

    /**
     * Creates a MessageCenterConfirmationFragment.
     */
    fun createMessageCenterConfirmationFragment(): MessageCenterConfirmationFragment {
        return MessageCenterConfirmationFragment()
    }

    /**
     * Creates a WebBrowserFragment with the specified title and URL.
     */
    fun createWebBrowserFragment(title: String, url: String): WebBrowserFragment {
        return WebBrowserFragment().apply {
            arguments = Bundle().apply {
                putString(WebBrowserFragment.ARG_TITLE, title)
                putString(WebBrowserFragment.ARG_URL, url)
            }
        }
    }

    // Bottom sheets

    /**
     * Creates a SurveyBottomSheetFragment with the specified survey.
     */
    fun createSurveyBottomSheet(survey: Survey): SurveyBottomSheetFragment {
        return SurveyBottomSheetFragment().apply {
            arguments = Bundle().apply {
                putParcelable(SurveyBottomSheetFragment.ARG_SURVEY, survey)
            }
        }
    }

    // Dialogs

    /**
     * Creates an ImagePreviewDialogFragment with the specified parameters.
     *
     * @param imageId The ID of the image to preview (for remote images)
     * @param imageName The name of the image file
     * @param localImageUri The local URI of the image (for local images)
     */
    fun createImagePreviewFragment(
        imageId: String? = null,
        imageName: String? = null,
        localImageUri: String? = null
    ): ImagePreviewDialogFragment {
        return ImagePreviewDialogFragment().apply {
            arguments = Bundle().apply {
                imageId?.let { putString(ImagePreviewDialogFragment.ARG_IMAGE_ID, it) }
                imageName?.let { putString(ImagePreviewDialogFragment.ARG_IMAGE_NAME, it) }
                localImageUri?.let { putString(ImagePreviewDialogFragment.ARG_LOCAL_IMAGE_URI, it) }
            }
        }
    }

    /**
     * Creates a VisitorCodeDialogFragment.
     *
     * This fragment is a thin wrapper around VisitorCodeView which can be used standalone.
     * The Controller remains the source of truth for business logic.
     */
    fun createVisitorCodeDialog(): VisitorCodeDialogFragment {
        return VisitorCodeDialogFragment()
    }

    // Additional dialog helpers (for internal use by Navigator)

    /**
     * Creates an EndEngagementDialogFragment with a confirmation callback.
     *
     * Will be implemented when EndEngagementDialogFragment is created.
     */
    fun createEndEngagementDialog(onConfirm: () -> Unit): DialogFragment {
        // TODO: Implement when dialog is created
        throw NotImplementedError("EndEngagementDialogFragment will be implemented in a future phase")
    }

    /**
     * Creates a MediaUpgradeDialogFragment with accept/decline callbacks.
     *
     * Will be implemented when MediaUpgradeDialogFragment is created.
     *
     * @param mediaType The type of media upgrade (audio, video)
     * @param onAccept Callback when user accepts the upgrade
     * @param onDecline Callback when user declines the upgrade
     */
    fun createMediaUpgradeDialog(
        mediaType: String,
        onAccept: () -> Unit,
        onDecline: () -> Unit
    ): DialogFragment {
        // TODO: Implement when dialog is created
        throw NotImplementedError("MediaUpgradeDialogFragment will be implemented in a future phase")
    }
}
