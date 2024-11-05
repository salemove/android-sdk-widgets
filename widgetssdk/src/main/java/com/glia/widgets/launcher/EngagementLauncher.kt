package com.glia.widgets.launcher

import android.app.Activity
import com.glia.androidsdk.Engagement
import com.glia.widgets.chat.Intention
import com.glia.widgets.core.secureconversations.domain.HasPendingSecureConversationsWithTimeoutUseCase
import com.glia.widgets.helper.unSafeSubscribe

/**
 * An interface for launching different types of engagements, such as chat,
 * audio calls, video calls, and secure messaging
 */
interface EngagementLauncher {
    /**
     * Starts a chat engagement.
     *
     * @param activity The Activity used to launch the chat screen
     * @param visitorContextAssetId Optional visitor context id from Glia Hub
     */
    fun startChat(activity: Activity, visitorContextAssetId: String? = null)

    /**
     * Starts an audio engagement.
     *
     * @param activity The Activity used to launch the call screen
     * @param visitorContextAssetId Optional visitor context id from Glia Hub
     */
    fun startAudioCall(activity: Activity, visitorContextAssetId: String? = null)

    /**
     * Starts a video engagement.
     *
     * @param activity The Activity used to launch the call screen
     * @param visitorContextAssetId Optional visitor context id from Glia Hub
     */
    fun startVideoCall(activity: Activity, visitorContextAssetId: String? = null)

    /**
     * Starts a secure messaging.
     *
     * @param activity The Activity used to launch the secure messaging welcome or chat screen
     * @param visitorContextAssetId Optional visitor context id from Glia Hub
     */
    fun startSecureMessaging(activity: Activity, visitorContextAssetId: String? = null)
}

internal class EngagementLauncherImpl(
    private val activityLauncher: ActivityLauncher,
    private val hasPendingSecureConversationsWithTimeoutUseCase: HasPendingSecureConversationsWithTimeoutUseCase,
    private val configurationManager: ConfigurationManager
) : EngagementLauncher {

    /**
     * Starts a chat engagement.
     *
     * @param activity The Activity used to launch the chat screen
     * @param visitorContextAssetId Optional visitor context id from Glia Hub
     */
    override fun startChat(activity: Activity, visitorContextAssetId: String?) {
        visitorContextAssetId?.let { configurationManager.setVisitorContextAssetId(it) }
        hasPendingSecureConversationsWithTimeoutUseCase().unSafeSubscribe {
            if (it) {
                activityLauncher.launchChat(activity, Intention.SC_DIALOG_ENQUEUE_FOR_TEXT)
            } else {
                activityLauncher.launchChat(activity, Intention.LIVE_CHAT)
            }
        }
    }

    /**
     * Starts an audio engagement.
     *
     * @param activity The Activity used to launch the call screen
     * @param visitorContextAssetId Optional visitor context id from Glia Hub
     */
    override fun startAudioCall(activity: Activity, visitorContextAssetId: String?) {
        visitorContextAssetId?.let { configurationManager.setVisitorContextAssetId(it) }
        hasPendingSecureConversationsWithTimeoutUseCase().unSafeSubscribe {
            if (it) {
                activityLauncher.launchChat(activity, Intention.SC_DIALOG_START_AUDIO)
            } else {
                activityLauncher.launchCall(activity, Engagement.MediaType.AUDIO, false)
            }
        }
    }

    /**
     * Starts a video engagement.
     *
     * @param activity The Activity used to launch the call screen
     * @param visitorContextAssetId Optional visitor context id from Glia Hub
     */
    override fun startVideoCall(activity: Activity, visitorContextAssetId: String?) {
        visitorContextAssetId?.let { configurationManager.setVisitorContextAssetId(it) }
        hasPendingSecureConversationsWithTimeoutUseCase().unSafeSubscribe {
            if (it) {
                activityLauncher.launchChat(activity, Intention.SC_DIALOG_START_VIDEO)
            } else {
                activityLauncher.launchCall(activity, Engagement.MediaType.VIDEO, false)
            }
        }
    }

    /**
     * Starts a secure messaging engagement.
     *
     * @param activity The Activity used to launch the secure messaging screen
     * @param visitorContextAssetId Optional visitor context id from Glia Hub
     */
    override fun startSecureMessaging(activity: Activity, visitorContextAssetId: String?) {
        visitorContextAssetId?.let { configurationManager.setVisitorContextAssetId(it) }
        hasPendingSecureConversationsWithTimeoutUseCase().unSafeSubscribe {
            if (it) {
                activityLauncher.launchChat(activity, Intention.SC_CHAT)
            } else {
                activityLauncher.launchSecureMessagingWelcomeScreen(activity)
            }
    }
}
