package com.glia.widgets.core.configuration

import android.content.Intent
import com.glia.androidsdk.screensharing.ScreenSharing
import com.glia.widgets.GliaWidgets
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.ChatType
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger.logDeprecatedMethodUse
import com.glia.widgets.helper.TAG

internal class EngagementConfiguration private constructor(builder: Builder) {
    val companyName: String?
    val queueIds: List<String>?
    val contextAssetId: String?
    private val contextUrl: String?
    val runTimeTheme: UiTheme?
        get() = field ?: Dependencies.sdkConfigurationManager.uiTheme
    val screenSharingMode: ScreenSharing.Mode?
        get() = field ?: Dependencies.sdkConfigurationManager.screenSharingMode
    val chatType: ChatType?
    private val manualStringOverrideL: String?

    init {
        companyName = builder.companyName
        queueIds = builder.queueIds
        contextAssetId = builder.contextAssetId
        contextUrl = builder.contextUrl
        runTimeTheme = builder.runTimeTheme
        screenSharingMode = builder.screenSharingMode
        chatType = builder.chatType
        manualStringOverrideL = builder.manualLocaleOverride
    }

    @Deprecated("")
    fun getContextUrl(): String? {
        logDeprecatedMethodUse(TAG, "getContextUrl()")
        return contextUrl
    }

    class Builder {
        var companyName: String? = null
        var queueIds: List<String>? = null
        var contextAssetId: String? = null
        var contextUrl: String? = null
        var runTimeTheme: UiTheme? = null
        var screenSharingMode: ScreenSharing.Mode? = null
        var chatType: ChatType? = null
        var manualLocaleOverride: String? = null
        fun companyName(companyName: String?): Builder {
            this.companyName = companyName
            return this
        }

        @Deprecated("")
        fun queueId(queueId: String?): Builder {
            this.queueIds = queueId?.let { listOf(queueId) }
            return this
        }

        fun queueIds(queueIds: List<String>?): Builder {
            this.queueIds = queueIds
            return this
        }

        @Deprecated("")
        fun contextUrl(contextUrl: String?): Builder {
            logDeprecatedMethodUse(TAG, "contextUrl(String)")
            this.contextUrl = contextUrl
            return this
        }

        fun contextAssetId(contextAssetId: String?): Builder {
            this.contextAssetId = contextAssetId
            return this
        }

        @Deprecated(
            "While UiTheme can still be used for UI customization, we strongly encourage adopting remote configurations(GliaWidgetsConfig.Builder.setUiJsonRemoteConfig). " +
                "The remote configurations approach is more versatile and better suited for future development."
        )
        fun runTimeTheme(runTimeTheme: UiTheme?): Builder {
            this.runTimeTheme = runTimeTheme
            return this
        }

        fun screenSharingMode(screenSharingMode: ScreenSharing.Mode?): Builder {
            this.screenSharingMode = screenSharingMode
            return this
        }

        fun manualLocaleOverride(manualLocaleOverride: String?): Builder {
            this.manualLocaleOverride = manualLocaleOverride
            return this
        }

        fun intent(intent: Intent): Builder {
            companyName = Dependencies.sdkConfigurationManager.companyName
            val queueId = intent.getStringExtra(GliaWidgets.QUEUE_ID)
            queueIds = intent.getStringArrayListExtra(GliaWidgets.QUEUE_IDS)
            if (queueIds == null && queueId != null) {
                queueIds = listOf(queueId)
            }
            val tempTheme = intent.getParcelableExtra<UiTheme>(GliaWidgets.UI_THEME)
            runTimeTheme = tempTheme ?: Dependencies.sdkConfigurationManager.uiTheme
            contextAssetId = intent.getStringExtra(GliaWidgets.CONTEXT_ASSET_ID)
            val tempMode =
                if (intent.hasExtra(GliaWidgets.SCREEN_SHARING_MODE)) intent.getSerializableExtra(
                    GliaWidgets.SCREEN_SHARING_MODE
                ) as ScreenSharing.Mode? else Dependencies.sdkConfigurationManager.screenSharingMode
            screenSharingMode =
                tempMode ?: Dependencies.sdkConfigurationManager.screenSharingMode
            chatType =
                if (intent.hasExtra(GliaWidgets.CHAT_TYPE)) intent.getParcelableExtra(GliaWidgets.CHAT_TYPE) else DEFAULT_CHAT_TYPE
            return this
        }

        fun build(): EngagementConfiguration {
            return EngagementConfiguration(this)
        }
    }

    companion object {
        private val DEFAULT_CHAT_TYPE = ChatType.LIVE_CHAT
    }
}
