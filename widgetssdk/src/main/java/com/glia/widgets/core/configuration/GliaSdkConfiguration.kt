package com.glia.widgets.core.configuration

import android.content.Intent
import com.glia.androidsdk.screensharing.ScreenSharing
import com.glia.widgets.GliaWidgets
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.ChatType
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger.logDeprecatedMethodUse
import com.glia.widgets.helper.TAG

internal class GliaSdkConfiguration private constructor(builder: Builder) {
    val companyName: String?
    val queueIds: List<String>?
    val contextAssetId: String?
    private val contextUrl: String?
    val runTimeTheme: UiTheme?
        get() = field ?: Dependencies.getSdkConfigurationManager().uiTheme
    val useOverlay: Boolean?
    val screenSharingMode: ScreenSharing.Mode?
        get() = field ?: Dependencies.getSdkConfigurationManager().screenSharingMode
    val chatType: ChatType?
    private val manualStringOverrideL: String?

    init {
        companyName = builder.companyName
        queueIds = builder.queueIds
        contextAssetId = builder.contextAssetId
        contextUrl = builder.contextUrl
        runTimeTheme = builder.runTimeTheme
        useOverlay = builder.useOverlay
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
        var useOverlay: Boolean? = null
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

        fun useOverlay(useOverlay: Boolean): Builder {
            this.useOverlay = useOverlay
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
            companyName = Dependencies.getSdkConfigurationManager().companyName
            val queueId = intent.getStringExtra(GliaWidgets.QUEUE_ID)
            queueIds = intent.getStringArrayListExtra(GliaWidgets.QUEUE_IDS)
            if (queueIds == null && queueId != null) {
                queueIds = listOf(queueId)
            }
            val tempTheme = intent.getParcelableExtra<UiTheme>(GliaWidgets.UI_THEME)
            runTimeTheme = tempTheme ?: Dependencies.getSdkConfigurationManager().uiTheme
            contextAssetId = intent.getStringExtra(GliaWidgets.CONTEXT_ASSET_ID)
            useOverlay = intent.getBooleanExtra(
                GliaWidgets.USE_OVERLAY,
                Dependencies.getSdkConfigurationManager().isUseOverlay
            )
            val tempMode =
                if (intent.hasExtra(GliaWidgets.SCREEN_SHARING_MODE)) intent.getSerializableExtra(
                    GliaWidgets.SCREEN_SHARING_MODE
                ) as ScreenSharing.Mode? else Dependencies.getSdkConfigurationManager().screenSharingMode
            screenSharingMode =
                tempMode ?: Dependencies.getSdkConfigurationManager().screenSharingMode
            chatType =
                if (intent.hasExtra(GliaWidgets.CHAT_TYPE)) intent.getParcelableExtra(GliaWidgets.CHAT_TYPE) else DEFAULT_CHAT_TYPE
            return this
        }

        fun build(): GliaSdkConfiguration {
            return GliaSdkConfiguration(this)
        }
    }

    companion object {
        private val DEFAULT_CHAT_TYPE = ChatType.LIVE_CHAT
    }
}
