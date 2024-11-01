package com.glia.widgets.launcher

import android.app.Activity
import com.glia.androidsdk.Engagement
import com.glia.widgets.chat.Intention

/**
 * An interface for launching different types of engagements, such as chat,
 * audio calls, video calls, and secure messaging
 */
interface EngagementLauncher {
    /**
     * Starts a chat engagement.
     *
     * @param activity The Activity used to launch the chat screen
     */
    fun startChat(activity: Activity)

    /**
     * Starts an audio engagement.
     *
     * @param activity The Activity used to launch the call screen
     */
    fun startAudioCall(activity: Activity)

    /**
     * Starts a video engagement.
     *
     * @param activity The Activity used to launch the call screen
     */
    fun startVideoCall(activity: Activity)

    /**
     * Starts a secure messaging.
     *
     * @param activity The Activity used to launch the secure messaging welcome or chat screen
     */
    fun startSecureMessaging(activity: Activity)
}

internal class EngagementLauncherImpl(private val activityLauncher: ActivityLauncher) : EngagementLauncher {

    override fun startChat(activity: Activity) {
        activityLauncher.launchChat(activity, Intention.LIVE_CHAT_UNAUTHENTICATED)
    }

    override fun startAudioCall(activity: Activity) {
        activityLauncher.launchCall(activity, Engagement.MediaType.AUDIO, false)
    }

    override fun startVideoCall(activity: Activity) {
        activityLauncher.launchCall(activity, Engagement.MediaType.VIDEO, false)
    }

    override fun startSecureMessaging(activity: Activity) {
        activityLauncher.launchSecureMessagingWelcomeScreen(activity)
    }
}
