package com.glia.widgets.call

import android.os.Bundle
import com.glia.widgets.R
import com.glia.widgets.base.FadeTransitionActivity
import com.glia.widgets.call.CallView.OnNavigateToChatListener
import com.glia.widgets.call.CallView.OnNavigateToWebBrowserListener
import com.glia.widgets.chat.Intention
import com.glia.widgets.di.Dependencies
import com.glia.widgets.engagement.MediaType
import com.glia.widgets.helper.ExtraKeys
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.getEnumExtra
import com.glia.widgets.launcher.ActivityLauncher
import com.glia.widgets.locale.LocaleString
import kotlin.properties.Delegates

/**
 * This activity is used for engagements that include audio and/or video calls.
 *
 *
 * Main features:
 * - Requests required permissions and enqueues for audio and/or video engagements if no ongoing engagements are found.
 * - Provides video feeds from operator and visitor cameras.
 * - Provides controls for managing ongoing engagements, including video and audio.
 * - Allows switching between chat and call activities.
 *
 *
 * Before this activity is launched, make sure that Glia Widgets SDK is set up correctly.
 */
internal class CallActivity : FadeTransitionActivity() {
    private val activityLauncher: ActivityLauncher by lazy { Dependencies.activityLauncher }
    private var callView: CallView by Delegates.notNull()

    private var onBackClickedListener: CallView.OnBackClickedListener? = CallView.OnBackClickedListener { this.finishAfterTransition() }
    private var onNavigateToChatListener: OnNavigateToChatListener? = OnNavigateToChatListener {
        navigateToChat()
        finishAfterTransition()
    }
    private val onNavigateToWebBrowserListener = OnNavigateToWebBrowserListener(this::navigateToWebBrowser)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.i(TAG, "Create Call screen")
        setContentView(R.layout.call_activity)
        callView = findViewById(R.id.call_view)

        val isUpgradeToCall = intent.getBooleanExtra(ExtraKeys.IS_UPGRADE_TO_CALL, false)

        if (!callView.shouldShowMediaEngagementView(isUpgradeToCall)) {
            finishAfterTransition()
            return
        }

        callView.setOnTitleUpdatedListener(this::setTitle)
        onBackClickedListener?.also(callView::setOnBackClickedListener)

        callView.setOnEndListener { this.finishAfterTransition() }

        callView.setOnMinimizeListener { this.finishAfterTransition() }
        onNavigateToChatListener?.also(callView::setOnNavigateToChatListener)
        callView.setOnNavigateToWebBrowserListener(onNavigateToWebBrowserListener)

        if (savedInstanceState == null) {

            startCall(intent.getEnumExtra<MediaType>(ExtraKeys.MEDIA_TYPE), isUpgradeToCall)
        }
    }

    override fun onResume() {
        callView.onResume()
        super.onResume()
    }

    override fun onPause() {
        callView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.i(TAG, "Destroy Call screen")
        onBackClickedListener = null
        onNavigateToChatListener = null
        callView.onDestroy()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        callView.onUserInteraction()
    }

    private fun startCall(mediaType: MediaType?, isUpgradeToCall: Boolean) {
        callView.startCall(isUpgradeToCall, mediaType)
    }

    private fun navigateToChat() {
        Logger.d(TAG, "navigateToChat")
        activityLauncher.launchChat(this, Intention.RETURN_TO_CHAT)
    }

    private fun navigateToWebBrowser(title: LocaleString, url: String) = activityLauncher.launchWebBrowser(this, title, url)
}
