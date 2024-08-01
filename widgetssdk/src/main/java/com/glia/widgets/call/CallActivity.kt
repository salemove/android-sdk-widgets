package com.glia.widgets.call

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.glia.widgets.GliaWidgets
import com.glia.widgets.R
import com.glia.widgets.base.FadeTransitionActivity
import com.glia.widgets.call.CallActivityIntentHelper.createIntent
import com.glia.widgets.call.CallActivityIntentHelper.readConfiguration
import com.glia.widgets.call.CallView.OnNavigateToChatListener
import com.glia.widgets.call.CallView.OnNavigateToWebBrowserListener
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.core.configuration.GliaSdkConfiguration
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger.d
import com.glia.widgets.helper.Logger.i
import com.glia.widgets.helper.Logger.logDeprecatedMethodUse
import com.glia.widgets.helper.Utils.toMediaType
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.webbrowser.WebBrowserActivity.Companion.intent
import java.util.Objects

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
internal class CallActivity : FadeTransitionActivity() { // TODO: review this class more carefully - automatic Kotlin migration need to be reviewed
    private lateinit var configuration: Configuration

    private var callView: CallView? = null
    private var onBackClickedListener: CallView.OnBackClickedListener? =
        CallView.OnBackClickedListener { this.finish() }
    private var onNavigateToChatListener: OnNavigateToChatListener? = OnNavigateToChatListener {
        navigateToChat()
        finish()
    }
    private val onNavigateToWebBrowserListener =
        OnNavigateToWebBrowserListener { title: LocaleString, url: String ->
            this.navigateToWebBrowser(
                title,
                url
            )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        i(TAG, "Create Call screen")
        setContentView(R.layout.call_activity)
        callView = findViewById(R.id.call_view) as? CallView

        // Legacy company name support
        Dependencies.getSdkConfigurationManager()
            .setLegacyCompanyName(intent.getStringExtra(GliaWidgets.COMPANY_NAME))

        configuration = readConfiguration(this)

        if (callView?.shouldShowMediaEngagementView(configuration.isUpgradeToCall) == false) {
            finishAndRemoveTask()
            return
        }

        callView?.setOnTitleUpdatedListener(CallView.OnTitleUpdatedListener { locale: LocaleString? ->
            this.setTitle(
                locale
            )
        })
        callView?.let {
            it.setConfiguration(configuration.sdkConfiguration)
            it.setUiTheme(configuration.sdkConfiguration?.runTimeTheme)
            onBackClickedListener?.let { listener -> it.setOnBackClickedListener(listener) }

            // In case the engagement ends, Activity is removed from the device's Recents menu
            // to avoid app users to accidentally start queueing for another call when they resume
            // the app from the Recents menu and the app's backstack was empty.
            it.setOnEndListener(CallView.OnEndListener { this.finishAndRemoveTask() })

            it.setOnMinimizeListener(CallView.OnMinimizeListener { this.finish() })
            onNavigateToChatListener?.let { listener -> it.setOnNavigateToChatListener(listener) }
            it.setOnNavigateToWebBrowserListener(onNavigateToWebBrowserListener)
        }

        if (savedInstanceState == null) {
            startCall()
        }
    }

    override fun onResume() {
        callView?.onResume()
        super.onResume()
    }

    override fun onPause() {
        callView?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        i(TAG, "Destroy Call screen")
        onBackClickedListener = null
        onNavigateToChatListener = null
        callView?.onDestroy()
        super.onDestroy()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        callView?.onUserInteraction()
    }

    private fun startCall() {
        if (configuration.sdkConfiguration == null) return

        configuration.sdkConfiguration?.let {
            callView?.startCall(
                it.companyName!!, // TODO: get rid of force unwrap
                it.queueIds,
                it.contextAssetId,
                it.useOverlay!!, // TODO: get rid of force unwrap
                it.screenSharingMode!!, // TODO: get rid of force unwrap
                configuration.isUpgradeToCall,
                configuration.mediaType
            )
        }
    }

    private fun navigateToChat() {
        d(TAG, "navigateToChat")
        val sdkConfiguration = Objects.requireNonNull(
            configuration.sdkConfiguration
        )
        val queueIds = if (sdkConfiguration?.queueIds != null) {
            ArrayList(sdkConfiguration.queueIds)
        } else {
            null
        }
        val newIntent = Intent(applicationContext, ChatActivity::class.java)
            .putExtra(GliaWidgets.QUEUE_IDS, queueIds)
            .putExtra(GliaWidgets.CONTEXT_ASSET_ID, sdkConfiguration?.contextAssetId)
            .putExtra(GliaWidgets.UI_THEME, sdkConfiguration?.runTimeTheme)
            .putExtra(GliaWidgets.USE_OVERLAY, sdkConfiguration?.useOverlay)
            .putExtra(GliaWidgets.SCREEN_SHARING_MODE, sdkConfiguration?.screenSharingMode)
        startActivity(newIntent)
    }

    private fun navigateToWebBrowser(title: LocaleString, url: String) {
        val newIntent = intent(this, title, url)
        startActivity(newIntent)
    }

    companion object {
        private val TAG: String = CallActivity::class.java.simpleName

        /**
         * Creates and fills out Intent for starting CallActivity
         * @param applicationContext - application context
         * @param sdkConfiguration - widgets sdk configuration
         * @param mediaType - media type that should be started (in case media engagement not ongoing)
         * @return Intent for starting CallActivity
         */
        @Deprecated(
            """use {@link #getIntent(Context, Configuration)} since 1.8.2
      """
        )
        fun getIntent(
            applicationContext: Context,
            sdkConfiguration: GliaSdkConfiguration?,
            mediaType: String?
        ): Intent {
            logDeprecatedMethodUse(TAG, "getIntent(Context, GliaSdkConfiguration, String)")
            return getIntent(
                applicationContext,
                Configuration.Builder()
                    .setWidgetsConfiguration(sdkConfiguration)
                    .setMediaType(toMediaType(mediaType!!)) // TODO: get rid of force unwrap
                    .build()
            )
        }

        /**
         * Creates and fills out Intent for starting CallActivity
         * @param context - Context object
         * @param configuration - CallActivity configuration
         * @return - Intent for Starting CallActivity
         */
        // TODO: 31.07.2024 make deprecated
        private fun getIntent(
            context: Context,
            configuration: Configuration
        ): Intent {
            return createIntent(context, configuration)
        }
    }
}
