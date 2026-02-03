package com.glia.widgets.di

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.chat.Intention
import com.glia.widgets.survey.SurveyBottomSheetFragment

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
     * Creates a MessageCenterFragment with the specified queue IDs.
     *
     * Will be implemented in Phase 9 when MessageCenterFragment is created.
     */
    fun createMessageCenterFragment(queueIds: ArrayList<String>?): Fragment {
        // TODO: Implement in Phase 9
        throw NotImplementedError("MessageCenterFragment will be implemented in Phase 9")
    }

    /**
     * Creates a WebBrowserFragment with the specified title and URL.
     *
     * Will be implemented in Phase 6 when WebBrowserFragment is created.
     */
    fun createWebBrowserFragment(title: String, url: String): Fragment {
        // TODO: Implement in Phase 6
        throw NotImplementedError("WebBrowserFragment will be implemented in Phase 6")
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
     * Will be implemented in Phase 7 when ImagePreviewDialogFragment is created.
     *
     * @param imageId The ID of the image to preview (for remote images)
     * @param imageName The name of the image file
     * @param localImageUri The local URI of the image (for local images)
     */
    fun createImagePreviewFragment(
        imageId: String? = null,
        imageName: String? = null,
        localImageUri: String? = null
    ): DialogFragment {
        // TODO: Implement in Phase 7
        throw NotImplementedError("ImagePreviewDialogFragment will be implemented in Phase 7")
    }

    /**
     * Creates a VisitorCodeDialogFragment.
     *
     * Will be implemented in Phase 8 when VisitorCodeDialogFragment is created.
     */
    fun createVisitorCodeDialog(): DialogFragment {
        // TODO: Implement in Phase 8
        throw NotImplementedError("VisitorCodeDialogFragment will be implemented in Phase 8")
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
