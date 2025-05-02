package com.glia.widgets.launcher

import com.glia.androidsdk.screensharing.ScreenSharing
import com.glia.widgets.GliaWidgets
import com.glia.widgets.GliaWidgetsConfig
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.BehaviorProcessor

internal interface ConfigurationManager {
    val screenSharingMode: ScreenSharing.Mode

    val enableBubbleOutsideApp: Boolean
    val enableBubbleInsideApp: Boolean
    val suppressPushNotificationsPermissionRequestDuringAuthentication: Boolean
    val queueIdsObservable: Flowable<List<String>>
    val visitorContextAssetId: String?

    /**
     * Applies [GliaWidgetsConfig] from the [GliaWidgets.init] configuration step
     */
    fun applyConfiguration(config: GliaWidgetsConfig)

    /**
     * Sets queue IDs
     * [queueIds] A list of queue IDs to be used for the engagement launcher.
     * When empty or invalid, the default queues will be used.
     */
    fun setQueueIds(queueIds: List<String>)

    /**
     * Sets the visitor context for the engagement.
     *
     * @param visitorContextAssetId a visitor context id from Glia Hub.
     */
    fun setVisitorContextAssetId(visitorContextAssetId: String)
}

internal class ConfigurationManagerImpl : ConfigurationManager {
    private var _screenSharingMode: ScreenSharing.Mode = ScreenSharing.Mode.APP_BOUNDED
    override val screenSharingMode: ScreenSharing.Mode
        get() = _screenSharingMode

    private var _enableBubbleOutsideApp: Boolean = true
    override val enableBubbleOutsideApp: Boolean
        get() = _enableBubbleOutsideApp

    private var _enableBubbleInsideApp: Boolean = true
    override val enableBubbleInsideApp: Boolean
        get() = _enableBubbleInsideApp

    private var _queueIdsObservable: BehaviorProcessor<List<String>> = BehaviorProcessor.create()
    override val queueIdsObservable: Flowable<List<String>> = _queueIdsObservable.onBackpressureLatest()

    private var _visitorContextAssetId: String? = null
    override val visitorContextAssetId: String?
        get() = _visitorContextAssetId

    private var _suppressPushNotificationsPermissionRequestDuringAuthentication: Boolean = false
    override val suppressPushNotificationsPermissionRequestDuringAuthentication: Boolean
        get() = _suppressPushNotificationsPermissionRequestDuringAuthentication

    override fun applyConfiguration(config: GliaWidgetsConfig) {
        config.screenSharingMode?.also { _screenSharingMode = it }
        config.enableBubbleInsideApp?.also { _enableBubbleInsideApp = it }
        config.enableBubbleOutsideApp?.also { _enableBubbleOutsideApp = it }
        config.suppressPushNotificationsPermissionRequestDuringAuthentication?.also {
            _suppressPushNotificationsPermissionRequestDuringAuthentication = it
        }
    }

    override fun setQueueIds(queueIds: List<String>) {
        _queueIdsObservable.onNext(queueIds)
    }

    override fun setVisitorContextAssetId(visitorContextAssetId: String) {
        _visitorContextAssetId = visitorContextAssetId
    }
}
