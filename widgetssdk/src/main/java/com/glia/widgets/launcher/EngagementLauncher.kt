package com.glia.widgets.launcher

import android.content.Context
import com.glia.androidsdk.Engagement
import com.glia.widgets.callvisualizer.controller.CallVisualizerContract
import com.glia.widgets.chat.Intention
import com.glia.widgets.core.secureconversations.domain.HasOngoingSecureConversationUseCase
import com.glia.widgets.engagement.domain.EndEngagementUseCase
import com.glia.widgets.engagement.domain.EngagementTypeUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This feature is under consideration for removal in a future releases. Use with caution."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
/**
 * Marks the feature as under consideration for removal in a future releases.
 */
annotation class FeatureUnderConsiderationForRemoval

/**
 * An interface for launching different types of engagements, such as chat,
 * audio calls, video calls, and secure messaging
 */
@OptIn(FeatureUnderConsiderationForRemoval::class)
interface EngagementLauncher {

    /**
     * Starts a chat engagement.
     *
     * @param context Activity or Context used to launch the chat screen
     */
    fun startChat(context: Context) = startChat(context, null)

    /**
     * Starts a chat engagement.
     *
     * @param context Activity or Context used to launch the chat screen
     * @param visitorContextAssetId Optional visitor context id from Glia Hub
     */
    @FeatureUnderConsiderationForRemoval
    fun startChat(context: Context, visitorContextAssetId: String?)

    /**
     * Starts an audio engagement.
     *
     * @param context Activity or Context used to launch the call screen
     */
    fun startAudioCall(context: Context) = startAudioCall(context, null)

    /**
     * Starts an audio engagement.
     *
     * @param context Activity or Context used to launch the call screen
     * @param visitorContextAssetId Optional visitor context id from Glia Hub
     */
    @FeatureUnderConsiderationForRemoval
    fun startAudioCall(context: Context, visitorContextAssetId: String?)

    /**
     * Starts a video engagement.
     *
     * @param context Activity or Context used to launch the call screen
     */
    fun startVideoCall(context: Context) = startVideoCall(context, null)

    /**
     * Starts a video engagement.
     *
     * @param context Activity or Context used to launch the call screen
     * @param visitorContextAssetId Optional visitor context id from Glia Hub
     */
    @FeatureUnderConsiderationForRemoval
    fun startVideoCall(context: Context, visitorContextAssetId: String?)

    /**
     * Starts a secure messaging.
     *
     * @param context Activity or Context used to launch the secure messaging welcome or chat screen
     */
    fun startSecureMessaging(context: Context) = startSecureMessaging(context, null)

    /**
     * Starts a secure messaging.
     *
     * @param context Activity or Context used to launch the secure messaging welcome or chat screen
     * @param visitorContextAssetId Optional visitor context id from Glia Hub
     */
    @FeatureUnderConsiderationForRemoval
    fun startSecureMessaging(context: Context, visitorContextAssetId: String?)
}

internal class EngagementLauncherImpl(
    private val activityLauncher: ActivityLauncher,
    private val hasOngoingSecureConversationUseCase: HasOngoingSecureConversationUseCase,
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase,
    private val endEngagementUseCase: EndEngagementUseCase,
    private val configurationManager: ConfigurationManager,
    private val engagementTypeUseCase: EngagementTypeUseCase,
    private val callVisualizerController: CallVisualizerContract.Controller,
    private val destroyChatController: () -> Unit,
    private val destroyCallController: () -> Unit
) : EngagementLauncher {

    @FeatureUnderConsiderationForRemoval
    override fun startChat(context: Context, visitorContextAssetId: String?) {
        if (isQueueingOrLiveEngagementUseCase.isQueueing) {
            Logger.i(TAG, "Canceling ongoing queue ticket to create a new one")
            endEngagementUseCase()
            destroyChatController()
        }

        visitorContextAssetId?.let { configurationManager.setVisitorContextAssetId(it) }

        when {
            engagementTypeUseCase.isCallVisualizer -> callVisualizerController.showAlreadyInCallSnackBar()
            isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement -> activityLauncher.launchChat(context, Intention.RETURN_TO_CHAT)
            else -> hasOngoingSecureConversationUseCase(
                onHasOngoingSecureConversation = { activityLauncher.launchChat(context, Intention.SC_DIALOG_ENQUEUE_FOR_TEXT) },
                onNoOngoingSecureConversation = { activityLauncher.launchChat(context, Intention.LIVE_CHAT) }
            )
        }
    }

    @FeatureUnderConsiderationForRemoval
    override fun startAudioCall(context: Context, visitorContextAssetId: String?) {
        if (isQueueingOrLiveEngagementUseCase.isQueueing) {
            Logger.i(TAG, "Canceling ongoing queue ticket to create a new one")
            endEngagementUseCase()
            destroyCallController()
        }

        visitorContextAssetId?.let { configurationManager.setVisitorContextAssetId(it) }

        when {
            engagementTypeUseCase.isCallVisualizer -> callVisualizerController.showAlreadyInCallSnackBar()
            engagementTypeUseCase.isMediaEngagement -> activityLauncher.launchCall(context, null, false)
            isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement -> activityLauncher.launchChat(context, Intention.RETURN_TO_CHAT)
            else -> hasOngoingSecureConversationUseCase(
                onHasOngoingSecureConversation = { activityLauncher.launchChat(context, Intention.SC_DIALOG_START_AUDIO) },
                onNoOngoingSecureConversation = { activityLauncher.launchCall(context, Engagement.MediaType.AUDIO, false) }
            )
        }
    }

    @FeatureUnderConsiderationForRemoval
    override fun startVideoCall(context: Context, visitorContextAssetId: String?) {
        if (isQueueingOrLiveEngagementUseCase.isQueueing) {
            Logger.i(TAG, "Canceling ongoing queue ticket to create a new one")
            endEngagementUseCase()
            destroyCallController()
        }

        visitorContextAssetId?.let { configurationManager.setVisitorContextAssetId(it) }

        when {
            engagementTypeUseCase.isCallVisualizer && !engagementTypeUseCase.hasVideo -> callVisualizerController.showAlreadyInCallSnackBar()
            engagementTypeUseCase.isMediaEngagement -> activityLauncher.launchCall(context, null, false)
            isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement -> activityLauncher.launchChat(context, Intention.RETURN_TO_CHAT)
            else -> hasOngoingSecureConversationUseCase(
                onHasOngoingSecureConversation = { activityLauncher.launchChat(context, Intention.SC_DIALOG_START_VIDEO) },
                onNoOngoingSecureConversation = { activityLauncher.launchCall(context, Engagement.MediaType.VIDEO, false) }
            )
        }
    }

    @FeatureUnderConsiderationForRemoval
    override fun startSecureMessaging(context: Context, visitorContextAssetId: String?) {
        visitorContextAssetId?.let { configurationManager.setVisitorContextAssetId(it) }

        when {
            engagementTypeUseCase.isCallVisualizer -> callVisualizerController.showAlreadyInCallSnackBar()
            isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement -> activityLauncher.launchChat(context, Intention.RETURN_TO_CHAT)
            else -> hasOngoingSecureConversationUseCase(
                onHasOngoingSecureConversation = { activityLauncher.launchChat(context, Intention.SC_CHAT) },
                onNoOngoingSecureConversation = { activityLauncher.launchSecureMessagingWelcomeScreen(context) }
            )
        }
    }
}
