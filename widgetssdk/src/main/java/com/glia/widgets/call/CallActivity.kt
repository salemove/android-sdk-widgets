package com.glia.widgets.call

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.glia.widgets.GliaWidgets
import com.glia.widgets.R
import com.glia.widgets.base.FadeTransitionActivity
import com.glia.widgets.call.CallView.OnNavigateToChatListener
import com.glia.widgets.call.CallView.OnNavigateToWebBrowserListener
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.webbrowser.WebBrowserActivity.Companion.intent
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
 *
 *
 * Data that can be passed together with the Activity intent:
 * - [GliaWidgets.QUEUE_IDS]: IDs list of the queues you would like to use for your engagements.
 * For a full list of optional parameters, see the constants defined in [GliaWidgets].
 *
 *
 * Code example:
 * <pre>
 * Intent intent = new Intent(requireContext(), CallActivity.class);
 * intent.putExtra(GliaWidgets.QUEUE_IDS, new ArrayList<>(List.of("AUDIO_QUEUE_ID")));
 * intent.putExtra(GliaWidgets.MEDIA_TYPE, Engagement.MediaType.VIDEO);
 * startActivity(intent);
</pre> *
 */
class CallActivity : FadeTransitionActivity() {
    private var callConfiguration: CallConfiguration by Delegates.notNull()
    private var callView: CallView by Delegates.notNull()

    private var onBackClickedListener: CallView.OnBackClickedListener? = CallView.OnBackClickedListener { this.finish() }
    private var onNavigateToChatListener: OnNavigateToChatListener? = OnNavigateToChatListener {
        navigateToChat()
        finish()
    }
    private val onNavigateToWebBrowserListener = OnNavigateToWebBrowserListener(this::navigateToWebBrowser)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.i(TAG, "Create Call screen")
        setContentView(R.layout.call_activity)
        callView = findViewById(R.id.call_view)

        // Legacy company name support
        Dependencies.sdkConfigurationManager.setLegacyCompanyName(intent.getStringExtra(GliaWidgets.COMPANY_NAME))

        callConfiguration = CallActivityIntentHelper.readConfiguration(this)
        if (this.intent.hasExtra(GliaWidgets.USE_OVERLAY)) {
            // Integrator has passed a deprecated GliaWidgets.USE_OVERLAY parameter with Intent
            // Override bubble configuration with USE_OVERLAY value
            val useOverlay = this.intent.getBooleanExtra(GliaWidgets.USE_OVERLAY, true)
            Dependencies.sdkConfigurationManager.setLegacyUseOverlay(useOverlay)
        }

        if (!callView.shouldShowMediaEngagementView(callConfiguration.isUpgradeToCall)) {
            finishAndRemoveTask()
            return
        }

        callView.setOnTitleUpdatedListener(this::setTitle)
        callView.setEngagementConfiguration(callConfiguration.engagementConfiguration)
        callView.setUiTheme(callConfiguration.engagementConfiguration?.runTimeTheme)
        onBackClickedListener?.also(callView::setOnBackClickedListener)

        // In case the engagement ends, Activity is removed from the device's Recents menu
        // to avoid app users to accidentally start queueing for another call when they resume
        // the app from the Recents menu and the app's backstack was empty.
        callView.setOnEndListener { this.finishAndRemoveTask() }

        callView.setOnMinimizeListener { this.finish() }
        onNavigateToChatListener?.also(callView::setOnNavigateToChatListener)
        callView.setOnNavigateToWebBrowserListener(onNavigateToWebBrowserListener)

        if (savedInstanceState == null) {
            startCall()
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
        Logger.i(TAG, "Destroy Call screen")
        onBackClickedListener = null
        onNavigateToChatListener = null
        callView.onDestroy()
        super.onDestroy()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        callView.onUserInteraction()
    }

    private fun startCall() {
        val engagementConfiguration = callConfiguration.engagementConfiguration!!
        callView.startCall(
            engagementConfiguration.companyName!!,
            engagementConfiguration.queueIds,
            engagementConfiguration.contextAssetId,
            engagementConfiguration.screenSharingMode!!,
            callConfiguration.isUpgradeToCall,
            callConfiguration.mediaType
        )
    }

    private fun navigateToChat() {
        Logger.d(TAG, "navigateToChat")
        val engagementConfiguration = callConfiguration.engagementConfiguration
        val queueIds = engagementConfiguration?.queueIds?.let { ArrayList(it) }
        val newIntent = Intent(applicationContext, ChatActivity::class.java)
            .putExtra(GliaWidgets.QUEUE_IDS, queueIds)
            .putExtra(GliaWidgets.CONTEXT_ASSET_ID, engagementConfiguration?.contextAssetId)
            .putExtra(GliaWidgets.UI_THEME, engagementConfiguration?.runTimeTheme)
            .putExtra(GliaWidgets.SCREEN_SHARING_MODE, engagementConfiguration?.screenSharingMode)
        startActivity(newIntent)
    }

    private fun navigateToWebBrowser(title: LocaleString, url: String) {
        val newIntent = intent(this, title, url)
        startActivity(newIntent)
    }

    internal companion object {

        /**
         * Creates and fills out Intent for starting CallActivity
         * @param context - Context object
         * @param callConfiguration - CallActivity configuration
         * @return - Intent for Starting CallActivity
         */
        fun getIntent(context: Context, callConfiguration: CallConfiguration): Intent {
            return CallActivityIntentHelper.createIntent(context, callConfiguration)
        }
    }
}
