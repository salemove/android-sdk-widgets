package com.glia.widgets.launcher

import android.content.Context
import com.glia.androidsdk.Engagement
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.SDK_TYPE
import com.glia.widgets.R
import com.glia.widgets.chat.Intention
import com.glia.widgets.engagement.domain.EngagementTypeUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.internal.secureconversations.domain.HasOngoingSecureConversationUseCase
import com.glia.widgets.view.dialog.UiComponentsDispatcher

/**
 * An interface for launching different types of engagements, such as chat,
 * audio calls, video calls, and secure messaging
 */
interface EngagementLauncher {

    /**
     * Starts a chat engagement.
     *
     * @param context Activity or Context used to launch the chat screen
     */
    fun startChat(context: Context)

    /**
     * Starts a chat engagement.
     *
     * @param context Activity or Context used to launch the chat screen
     * @param visitorContextAssetId Visitor context id from Glia Hub
     */
    fun startChat(context: Context, visitorContextAssetId: String)

    /**
     * Starts an audio engagement.
     *
     * @param context Activity or Context used to launch the call screen
     */
    fun startAudioCall(context: Context)

    /**
     * Starts an audio engagement.
     *
     * @param context Activity or Context used to launch the call screen
     * @param visitorContextAssetId Visitor context id from Glia Hub
     */
    fun startAudioCall(context: Context, visitorContextAssetId: String)

    /**
     * Starts a video engagement.
     *
     * @param context Activity or Context used to launch the call screen
     */
    fun startVideoCall(context: Context)

    /**
     * Starts a video engagement.
     *
     * @param context Activity or Context used to launch the call screen
     * @param visitorContextAssetId Visitor context id from Glia Hub
     */
    fun startVideoCall(context: Context, visitorContextAssetId: String)

    /**
     * Starts a secure messaging.
     *
     * @param context Activity or Context used to launch the secure messaging welcome or chat screen
     */
    fun startSecureMessaging(context: Context)

    /**
     * Starts a secure messaging.
     *
     * @param context Activity or Context used to launch the secure messaging welcome or chat screen
     * @param visitorContextAssetId Visitor context id from Glia Hub
     */
    fun startSecureMessaging(context: Context, visitorContextAssetId: String)
}

internal class EngagementLauncherImpl(
    private val activityLauncher: ActivityLauncher,
    private val configurationManager: ConfigurationManager,
    private val uiComponentsDispatcher: UiComponentsDispatcher,
    private val hasOngoingSecureConversationUseCase: HasOngoingSecureConversationUseCase,
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase,
    private val engagementTypeUseCase: EngagementTypeUseCase,
) : EngagementLauncher {

    /**
     * @return true if there is an ongoing engagement or enqueueing for any media type
     */
    private val isEngagementOrQueueing: Boolean
        get() = isQueueingOrLiveEngagementUseCase.isQueueing || isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement

    /**
     * @return true if there is an ongoing engagement or enqueueing for audio or video media type
     */
    private val isMediaEngagementOrMediaQueueing: Boolean
        get() = engagementTypeUseCase.isMediaEngagement || isQueueingOrLiveEngagementUseCase.isQueueingForMedia

    /**
     * @return true if there is an ongoing engagement or enqueueing for live chat
     */
    private val isChatEngagementOrChatQueueing: Boolean
        get() = engagementTypeUseCase.isChatEngagement || isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat

    override fun startChat(context: Context) {
        GliaLogger.logMethodUse(EngagementLauncher::class, "startChat", "context")
        when {
            engagementTypeUseCase.isCallVisualizer -> uiComponentsDispatcher.showSnackBar(R.string.entry_widget_call_visualizer_description)
            isMediaEngagementOrMediaQueueing -> uiComponentsDispatcher.showSnackBar(R.string.entry_widget_call_visualizer_description)
            isEngagementOrQueueing -> activityLauncher.launchChat(context, Intention.RETURN_TO_CHAT)
            else -> hasOngoingSecureConversationUseCase(
                onHasOngoingSecureConversation = {
                    activityLauncher.launchChat(
                        context,
                        Intention.SC_DIALOG_ENQUEUE_FOR_TEXT
                    )
                },
                onNoOngoingSecureConversation = {
                    activityLauncher.launchChat(
                        context,
                        Intention.LIVE_CHAT
                    )
                }
            )
        }
    }

    override fun startChat(context: Context, visitorContextAssetId: String) {
        GliaLogger.logMethodUse(EngagementLauncher::class, "startChat", "context", "visitorContextAssetId")
        configurationManager.setVisitorContextAssetId(visitorContextAssetId)
        startChat(context)
    }

    override fun startAudioCall(context: Context) {
        GliaLogger.logMethodUse(EngagementLauncher::class, "startAudioCall", "context")
        when {
            engagementTypeUseCase.isCallVisualizer -> uiComponentsDispatcher.showSnackBar(R.string.entry_widget_call_visualizer_description)
            isMediaEngagementOrMediaQueueing -> activityLauncher.launchCall(context, null, false)
            isChatEngagementOrChatQueueing -> uiComponentsDispatcher.showSnackBar(R.string.entry_widget_call_visualizer_description)
            else -> hasOngoingSecureConversationUseCase(
                onHasOngoingSecureConversation = {
                    activityLauncher.launchChat(
                        context,
                        Intention.SC_DIALOG_START_AUDIO
                    )
                },
                onNoOngoingSecureConversation = {
                    activityLauncher.launchCall(
                        context,
                        Engagement.MediaType.AUDIO,
                        false
                    )
                }
            )
        }
    }

    override fun startAudioCall(context: Context, visitorContextAssetId: String) {
        GliaLogger.logMethodUse(EngagementLauncher::class, "startAudioCall", "context", "visitorContextAssetId")
        configurationManager.setVisitorContextAssetId(visitorContextAssetId)
        startAudioCall(context)
    }

    override fun startVideoCall(context: Context) {
        GliaLogger.logMethodUse(EngagementLauncher::class, "startVideoCall", "context")
        when {
            engagementTypeUseCase.isCallVisualizer && !engagementTypeUseCase.hasVideo -> uiComponentsDispatcher.showSnackBar(R.string.entry_widget_call_visualizer_description)
            isMediaEngagementOrMediaQueueing -> activityLauncher.launchCall(context, null, false)
            isChatEngagementOrChatQueueing -> uiComponentsDispatcher.showSnackBar(R.string.entry_widget_call_visualizer_description)
            else -> hasOngoingSecureConversationUseCase(
                onHasOngoingSecureConversation = {
                    activityLauncher.launchChat(
                        context,
                        Intention.SC_DIALOG_START_VIDEO
                    )
                },
                onNoOngoingSecureConversation = {
                    activityLauncher.launchCall(
                        context,
                        Engagement.MediaType.VIDEO,
                        false
                    )
                }
            )
        }
    }

    override fun startVideoCall(context: Context, visitorContextAssetId: String) {
        GliaLogger.logMethodUse(EngagementLauncher::class, "startVideoCall", "context", "visitorContextAssetId")
        configurationManager.setVisitorContextAssetId(visitorContextAssetId)
        startVideoCall(context)
    }

    override fun startSecureMessaging(context: Context) {
        GliaLogger.logMethodUse(EngagementLauncher::class, "startSecureMessaging", "context")
        when {
            engagementTypeUseCase.isCallVisualizer -> uiComponentsDispatcher.showSnackBar(R.string.entry_widget_call_visualizer_description)
            isChatEngagementOrChatQueueing -> activityLauncher.launchChat(
                context,
                Intention.RETURN_TO_CHAT
            )

            isMediaEngagementOrMediaQueueing -> uiComponentsDispatcher.showSnackBar(R.string.entry_widget_call_visualizer_description)
            else -> hasOngoingSecureConversationUseCase(
                onHasOngoingSecureConversation = {
                    activityLauncher.launchChat(
                        context,
                        Intention.SC_CHAT
                    )
                },
                onNoOngoingSecureConversation = {
                    activityLauncher.launchSecureMessagingWelcomeScreen(
                        context
                    )
                }
            )
        }
    }

    override fun startSecureMessaging(context: Context, visitorContextAssetId: String) {
        GliaLogger.logMethodUse(EngagementLauncher::class, "startSecureMessaging", "context", "visitorContextAssetId")
        configurationManager.setVisitorContextAssetId(visitorContextAssetId)
        startSecureMessaging(context)
    }
}
