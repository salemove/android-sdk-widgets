package com.glia.widgets.navigation

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.R
import com.glia.widgets.chat.Intention
import com.glia.widgets.di.Dependencies
import com.glia.widgets.di.FragmentFactory

/**
 * Handles fragment navigation within HostActivity.
 *
 * Manages:
 * - Full-screen fragment transactions (Chat, Call, MessageCenter)
 * - Dialog/BottomSheet display (Survey, VisitorCode, confirmations)
 * - Back stack management
 *
 * Note: Fragment creation is delegated to [FragmentFactory]. Navigation methods will be
 * fully functional as individual screen migrations are completed in phases 5-12.
 */
internal class Navigator(
    private val fragmentManager: FragmentManager,
    private val containerId: Int = R.id.fragment_container,
    private val fragmentFactory: FragmentFactory = Dependencies.fragmentFactory
) {

    val hasContent: Boolean
        get() = fragmentManager.findFragmentById(containerId) != null

    val hasDialogs: Boolean
        get() = fragmentManager.fragments.any { it.isAdded && it.tag?.startsWith(DIALOG_TAG_PREFIX) == true }

    val isEmpty: Boolean
        get() = !hasContent && !hasDialogs

    // Full-screen fragments
    // Navigation methods delegate to FragmentFactory for fragment creation.
    // Each method will be fully implemented as the corresponding Fragment is created.

    fun showChat(intention: Intention) {
        // Phase 12: ChatFragment
        // val fragment = fragmentFactory.createChatFragment(intention)
        // replaceFragment(fragment, TAG_CHAT)
    }

    fun showCall(mediaType: String?) {
        // Phase 11: CallFragment
        // val fragment = fragmentFactory.createCallFragment(mediaType)
        // replaceFragment(fragment, TAG_CALL)
    }

    fun showMessageCenter(queueIds: List<String>?) {
        // Phase 9: MessageCenterFragment
        // val fragment = fragmentFactory.createMessageCenterFragment(queueIds?.let { ArrayList(it) })
        // replaceFragment(fragment, TAG_MESSAGE_CENTER)
    }

    fun showWebBrowser(title: String, url: String) {
        // Phase 6: WebBrowserFragment
        // val fragment = fragmentFactory.createWebBrowserFragment(title, url)
        // fragmentManager.beginTransaction()
        //     .replace(containerId, fragment, TAG_WEB_BROWSER)
        //     .addToBackStack(null)
        //     .commit()
    }

    fun showImagePreview(imageId: String?, imageName: String?, localImageUri: String?) {
        // Phase 7: ImagePreviewDialogFragment
        // val fragment = fragmentFactory.createImagePreviewFragment(imageId, imageName, localImageUri)
        // fragment.show(fragmentManager, DIALOG_TAG_IMAGE_PREVIEW)
    }

    // Bottom sheets and dialogs

    fun showSurvey(survey: Survey) {
        // Phase 5: SurveyBottomSheetFragment
        // val fragment = fragmentFactory.createSurveyBottomSheet(survey)
        // fragment.show(fragmentManager, DIALOG_TAG_SURVEY)
    }

    fun showVisitorCode() {
        // Phase 8: VisitorCodeDialogFragment
        // val fragment = fragmentFactory.createVisitorCodeDialog()
        // fragment.show(fragmentManager, DIALOG_TAG_VISITOR_CODE)
    }

    // Navigation helpers

    fun popBackStack(): Boolean {
        return if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
            true
        } else {
            false
        }
    }

    fun dismissAllDialogs() {
        fragmentManager.fragments
            .filter { it.tag?.startsWith(DIALOG_TAG_PREFIX) == true }
            .forEach { (it as? DialogFragment)?.dismissAllowingStateLoss() }
    }

    fun clearContent() {
        fragmentManager.findFragmentById(containerId)?.let {
            fragmentManager.beginTransaction()
                .remove(it)
                .commit()
        }
    }

    // Helper methods for fragment transactions

    private fun replaceFragment(fragment: Fragment, tag: String) {
        fragmentManager.beginTransaction()
            .replace(containerId, fragment, tag)
            .commit()
    }

    companion object {
        private const val DIALOG_TAG_PREFIX = "dialog_"

        private const val TAG_CHAT = "chat"
        private const val TAG_CALL = "call"
        private const val TAG_MESSAGE_CENTER = "message_center"
        private const val TAG_WEB_BROWSER = "web_browser"

        private const val DIALOG_TAG_SURVEY = "dialog_survey"
        private const val DIALOG_TAG_VISITOR_CODE = "dialog_visitor_code"
        private const val DIALOG_TAG_IMAGE_PREVIEW = "dialog_image_preview"
        private const val DIALOG_TAG_END_ENGAGEMENT = "dialog_end_engagement"
        private const val DIALOG_TAG_MEDIA_UPGRADE = "dialog_media_upgrade"
    }
}