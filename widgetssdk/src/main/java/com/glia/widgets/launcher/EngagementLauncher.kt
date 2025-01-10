package com.glia.widgets.launcher

import android.content.Context
import com.glia.androidsdk.Engagement
import com.glia.widgets.chat.Intention
import com.glia.widgets.core.secureconversations.domain.HasOngoingSecureConversationUseCase
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.engagement.domain.EndEngagementUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

/**
 * An interface for launching different types of engagements, such as chat,
 * audio calls, video calls, and secure messaging
 */
interface EngagementLauncher {
    /**
     * Starts a chat engagement.
     *
     * @param context Activity or Context used to launch the chat screen
     * @param visitorContextAssetId Optional visitor context id from Glia Hub
     */
    fun startChat(context: Context, visitorContextAssetId: String? = null)

    /**
     * Starts an audio engagement.
     *
     * @param context Activity or Context used to launch the call screen
     * @param visitorContextAssetId Optional visitor context id from Glia Hub
     */
    fun startAudioCall(context: Context, visitorContextAssetId: String? = null)

    /**
     * Starts a video engagement.
     *
     * @param context Activity or Context used to launch the call screen
     * @param visitorContextAssetId Optional visitor context id from Glia Hub
     */
    fun startVideoCall(context: Context, visitorContextAssetId: String? = null)

    /**
     * Starts a secure messaging.
     *
     * @param context Activity or Context used to launch the secure messaging welcome or chat screen
     * @param visitorContextAssetId Optional visitor context id from Glia Hub
     */
    fun startSecureMessaging(context: Context, visitorContextAssetId: String? = null)
}

internal class EngagementLauncherImpl(
    private val activityLauncher: ActivityLauncher,
    private val hasOngoingSecureConversationUseCase: HasOngoingSecureConversationUseCase,
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase,
    private val endEngagementUseCase: EndEngagementUseCase,
    private val configurationManager: ConfigurationManager,
    private val controllerFactory: ControllerFactory
) : EngagementLauncher {

    override fun startChat(context: Context, visitorContextAssetId: String?) {
        if (isQueueingOrLiveEngagementUseCase.isQueueingForMedia) {
            Logger.i(TAG, "Canceling ongoing queue ticket to create a new one")
            endEngagementUseCase()
            controllerFactory.destroyChatController()
        }

        visitorContextAssetId?.let { configurationManager.setVisitorContextAssetId(it) }
        hasOngoingSecureConversationUseCase {
            if (it) {
                activityLauncher.launchChat(context, Intention.SC_DIALOG_ENQUEUE_FOR_TEXT)
            } else {
                activityLauncher.launchChat(context, Intention.LIVE_CHAT)
            }
        }
    }

    override fun startAudioCall(context: Context, visitorContextAssetId: String?) {
        if (isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat || isQueueingOrLiveEngagementUseCase.isQueueingForVideo) {
            Logger.i(TAG, "Canceling ongoing queue ticket to create a new one")
            endEngagementUseCase()
            controllerFactory.destroyCallController()
        }

        visitorContextAssetId?.let { configurationManager.setVisitorContextAssetId(it) }
        hasOngoingSecureConversationUseCase {
            if (it) {
                activityLauncher.launchChat(context, Intention.SC_DIALOG_START_AUDIO)
            } else {
                activityLauncher.launchCall(context, Engagement.MediaType.AUDIO, false)
            }
        }
    }

    override fun startVideoCall(context: Context, visitorContextAssetId: String?) {
        if (isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat || isQueueingOrLiveEngagementUseCase.isQueueingForAudio) {
            Logger.i(TAG, "Canceling ongoing queue ticket to create a new one")
            endEngagementUseCase()
            controllerFactory.destroyCallController()
        }

        visitorContextAssetId?.let { configurationManager.setVisitorContextAssetId(it) }
        hasOngoingSecureConversationUseCase {
            if (it) {
                activityLauncher.launchChat(context, Intention.SC_DIALOG_START_VIDEO)
            } else {
                activityLauncher.launchCall(context, Engagement.MediaType.VIDEO, false)
            }
        }
    }

    override fun startSecureMessaging(context: Context, visitorContextAssetId: String?) {
        visitorContextAssetId?.let { configurationManager.setVisitorContextAssetId(it) }
        hasOngoingSecureConversationUseCase {
            if (it) {
                activityLauncher.launchChat(context, Intention.SC_CHAT)
            } else {
                activityLauncher.launchSecureMessagingWelcomeScreen(context)
            }
        }
    }
}
