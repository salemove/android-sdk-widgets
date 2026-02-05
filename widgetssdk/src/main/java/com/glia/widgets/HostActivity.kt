package com.glia.widgets

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.core.os.BundleCompat
import com.glia.widgets.base.FadeTransitionActivity
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.navigation.Destination
import com.glia.widgets.navigation.Navigator

/**
 * Single transparent Activity that hosts all SDK screens.
 *
 * This Activity:
 * - Has a transparent background (client's UI visible when no content)
 * - Hosts full-screen Fragments (Chat, Call, MessageCenter)
 * - Shows BottomSheets and Dialogs (Survey, VisitorCode, confirmations)
 * - Handles all permission requests
 * - Manages unified back navigation
 * - Supports edge-to-edge display (via FadeTransitionActivity)
 *
 * Edge-to-edge handling:
 * - FadeTransitionActivity.onCreate() calls enableEdgeToEdge()
 * - Individual Fragments/Views apply their own content padding using SimpleWindowInsetsAndAnimationHandler
 *
 * Usage:
 * ```
 * HostActivity.start(context, Destination.Chat(intention))
 * HostActivity.start(context, Destination.SurveyScreen(survey))
 * ```
 *
 * Note: Fragment navigation methods in Navigator will be fully implemented
 * as individual screen migrations are completed in later phases.
 */
internal class HostActivity : FadeTransitionActivity() {

    private lateinit var navigator: Navigator

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            handleBack()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // FadeTransitionActivity handles edge-to-edge
        setContentView(R.layout.host_activity)

        navigator = Navigator(supportFragmentManager)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        if (savedInstanceState == null) {
            handleIntent(intent)
        }

        Logger.i(TAG, "HostActivity created")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val destination: Destination = BundleCompat.getParcelable(
            intent.extras ?: return,
            EXTRA_DESTINATION,
            Destination::class.java
        ) ?: return

        Logger.i(TAG, "Navigating to: ${destination::class.simpleName}")

        navigateToDestination(destination)
    }

    private fun navigateToDestination(destination: Destination) {
        when (destination) {
            is Destination.Chat -> navigator.showChat(destination.intention)
            is Destination.Call -> navigator.showCall(destination.mediaType)
            is Destination.SurveyScreen -> navigator.showSurvey(destination.survey)
            is Destination.MessageCenter -> navigator.showMessageCenter(destination.queueIds)
            Destination.MessageCenterConfirmation -> navigator.showMessageCenterConfirmation()
            is Destination.WebBrowser -> navigator.showWebBrowser(destination.title, destination.url)
            is Destination.ImagePreview -> navigator.showImagePreview(
                destination.imageId,
                destination.imageName,
                destination.localImageUri
            )
            Destination.VisitorCode -> navigator.showVisitorCode()
            Destination.EntryWidget -> navigator.showEntryWidget()
        }
    }

    private fun handleBack() {
        // First try to pop back stack
        if (navigator.popBackStack()) {
            return
        }

        // Then dismiss dialogs
        if (navigator.hasDialogs) {
            navigator.dismissAllDialogs()
            finishIfEmpty()
            return
        }

        // Finally finish activity
        finishAndRemoveTask()
    }

    /**
     * Called by Fragments when they complete (e.g., Survey submitted, engagement ended).
     * Finishes the activity if no content remains.
     */
    fun finishIfEmpty() {
        if (navigator.isEmpty) {
            Logger.i(TAG, "No content remaining, finishing")
            finishAndRemoveTask()
        }
    }

    /**
     * Called by Fragments to navigate to another destination.
     */
    fun navigateTo(destination: Destination) {
        navigateToDestination(destination)
    }

    override fun onDestroy() {
        super.onDestroy()
        onBackPressedCallback.remove()
        Logger.i(TAG, "HostActivity destroyed")
    }

    companion object {
        internal const val EXTRA_DESTINATION = "extra_destination"

        fun start(context: Context, destination: Destination) {
            val intent: Intent = Intent(context, HostActivity::class.java).apply {
                putExtra(EXTRA_DESTINATION, destination)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }

        fun createIntent(context: Context, destination: Destination): Intent {
            return Intent(context, HostActivity::class.java).apply {
                putExtra(EXTRA_DESTINATION, destination)
            }
        }
    }
}