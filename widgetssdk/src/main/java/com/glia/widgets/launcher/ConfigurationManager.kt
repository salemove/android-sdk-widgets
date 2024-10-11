package com.glia.widgets.launcher

import com.glia.androidsdk.screensharing.ScreenSharing
import com.glia.widgets.GliaWidgets
import com.glia.widgets.GliaWidgetsConfig
import com.glia.widgets.helper.Data
import com.glia.widgets.helper.from
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.BehaviorProcessor

internal interface ConfigurationManager {
    val screenSharingMode: ScreenSharing.Mode

    val enableBubbleOutsideApp: Boolean
    val enableBubbleInsideApp: Boolean

    val queueIdsObservable: Flowable<Data<List<String>>>
    val queueIds: List<String>?

    /**
     * Applies [GliaWidgetsConfig] from the [GliaWidgets.init] configuration step
     */
    fun applyConfiguration(config: GliaWidgetsConfig)

    /**
     * Retrives queue IDs
     * [queueIds] A list of queue IDs to be used for the engagement launcher.
     * When null, the default queues will be used.
     */
    fun setQueueIds(queueIds: List<String>?)
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

    private var _queueIdsObservable: BehaviorProcessor<Data<List<String>>> = BehaviorProcessor.create()
    override val queueIdsObservable: Flowable<Data<List<String>>> = _queueIdsObservable.onBackpressureLatest()

    override val queueIds: List<String>?
        get() = _queueIdsObservable.value?.valueOrNull

    override fun applyConfiguration(config: GliaWidgetsConfig) {
        config.screenSharingMode?.also { _screenSharingMode = it }
        config.enableBubbleInsideApp?.also { _enableBubbleInsideApp = it }
        config.enableBubbleOutsideApp?.also { _enableBubbleOutsideApp = it }
    }

    override fun setQueueIds(queueIds: List<String>?) {
        _queueIdsObservable.onNext(Data.from(queueIds?.takeIf { it.isNotEmpty() }))
    }
}
