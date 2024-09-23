package com.glia.widgets.chat

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import com.glia.widgets.GliaWidgets
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.base.FadeTransitionActivity
import com.glia.widgets.call.CallConfiguration
import com.glia.widgets.core.configuration.EngagementConfiguration
import com.glia.widgets.di.Dependencies
import com.glia.widgets.di.Dependencies.sdkConfigurationManager
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.Utils
import com.glia.widgets.launcher.ActivityLauncher
import kotlin.properties.Delegates

/**
 * This activity is used to handle chat engagements.
 *
 *
 * Main features:
 * - Shows chat history to authenticated visitors before enqueuing for new engagements.
 * - Requests required permissions and enqueues for new chat engagements if no ongoing engagements are found.
 * - Provides controls for managing ongoing engagements.
 * - Enables message exchange between the visitor and the operator during ongoing engagements.
 * - Allows the operator to upgrade engagements.
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
 * Intent intent = new Intent(requireContext(), ChatActivity.class);
 * intent.putExtra(GliaWidgets.QUEUE_IDS, new ArrayList<>(List.of("CHAT_QUEUE_ID")));
 * startActivity(intent);
 * <pre></pre>
</pre> */
class ChatActivity : FadeTransitionActivity() {
    private val activityLauncher: ActivityLauncher by lazy { Dependencies.activityLauncher }

    private var chatView: ChatView by Delegates.notNull()
    private var engagementConfiguration: EngagementConfiguration by Delegates.notNull()

    private val defaultCallConfiguration: CallConfiguration
        get() = CallConfiguration(engagementConfiguration)

    override fun onCreate(savedInstanceState: Bundle?) {
        this.enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Logger.i(TAG, "Create Chat screen")
        setContentView(R.layout.chat_activity)
        chatView = findViewById(R.id.chat_view)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                chatView.onBackPressed()
            }
        })

        // Legacy company name support
        sdkConfigurationManager.setLegacyCompanyName(intent.getStringExtra(GliaWidgets.COMPANY_NAME))

        chatView.setOnTitleUpdatedListener(this::setTitle)
        engagementConfiguration = createEngagementConfiguration(intent)
        if (intent.hasExtra(GliaWidgets.USE_OVERLAY)) {
            // Integrator has passed a deprecated GliaWidgets.USE_OVERLAY parameter with Intent
            // Override bubble configuration with USE_OVERLAY value
            val useOverlay = intent.getBooleanExtra(GliaWidgets.USE_OVERLAY, true)
            sdkConfigurationManager.setLegacyUseOverlay(useOverlay)
        }

        if (!chatView.shouldShow()) {
            finishAndRemoveTask()
            return
        }

        chatView.setConfiguration(engagementConfiguration)
        chatView.setUiTheme(engagementConfiguration.runTimeTheme)
        chatView.setOnBackClickedListener(::finish)
        chatView.setOnBackToCallListener(::backToCallScreen)

        // In case the engagement ends, Activity is removed from the device's Recents menu
        // to avoid app users to accidentally start queueing for another call when they resume
        // the app from the Recents menu and the app's backstack was empty.
        chatView.setOnEndListener(::finishAndRemoveTask)

        chatView.setOnMinimizeListener(::finish)
        chatView.setOnNavigateToCallListener(this::startCallScreen)
        chatView.startChat(
            engagementConfiguration.companyName,
            engagementConfiguration.queueIds,
            engagementConfiguration.contextAssetId,
            engagementConfiguration.screenSharingMode,
            engagementConfiguration.chatType ?: ChatType.LIVE_CHAT
        )
    }

    override fun onResume() {
        chatView.onResume()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        chatView.onPause()
    }

    override fun onDestroy() {
        chatView.onDestroyView()
        Logger.i(TAG, "Destroy Chat screen")
        super.onDestroy()
    }

    private fun createEngagementConfiguration(intent: Intent): EngagementConfiguration = EngagementConfiguration(intent)

    //TODO: Check why theme attribute is not anymore used
    private fun startCallScreen(theme: UiTheme, mediaType: String) {
        activityLauncher.launchCall(this, defaultCallConfiguration.copy(mediaType = Utils.toMediaType(mediaType), isUpgradeToCall = true))
        finish()
    }

    private fun backToCallScreen() {
        activityLauncher.launchCall(this, defaultCallConfiguration)
        finish()
    }
}
