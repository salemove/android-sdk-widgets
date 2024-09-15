package com.glia.widgets

import android.content.Context
import android.content.Intent
import com.glia.androidsdk.Engagement
import com.glia.widgets.call.CallActivity
import com.glia.widgets.di.Dependencies

/**
 * This class is responsible for starting engagement (chat, audio call, video call, messaging)
 */
class Navigator(private val queueIds: ArrayList<String>?, private val contextAssetId: String?) {

    private val gliaWidgetsConfig = Dependencies.sdkConfigurationManager.buildEngagementConfiguration()

    fun startChat(context: Context) {
        // Navigate to chat
    }

    fun startAudioCall(context: Context) {
        // Navigate to voice call
        val intent = Intent(context, CallActivity::class.java)
            .putExtra(GliaWidgets.QUEUE_IDS, queueIds)
            .putExtra(GliaWidgets.CONTEXT_ASSET_ID, contextAssetId)
            .putExtra(GliaWidgets.UI_THEME, Dependencies.sdkConfigurationManager.uiTheme)
            .putExtra(GliaWidgets.SCREEN_SHARING_MODE, gliaWidgetsConfig?.screenSharingMode)
            .putExtra(GliaWidgets.MEDIA_TYPE, Engagement.MediaType.AUDIO)
        context.startActivity(intent)
    }

    fun startVideoCall(context: Context) {
        // Navigate to video call
    }

    fun startSecureConversations(context: Context) {
        // Navigate to video call
    }
}
