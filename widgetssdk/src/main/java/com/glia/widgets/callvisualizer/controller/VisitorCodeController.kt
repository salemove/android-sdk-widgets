package com.glia.widgets.callvisualizer.controller

import com.glia.androidsdk.omnibrowse.VisitorCode
import com.glia.telemetry_lib.ButtonNames
import com.glia.telemetry_lib.EventAttribute
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.telemetry_lib.ViewType
import com.glia.telemetry_lib.VisitorCodeState
import com.glia.widgets.callvisualizer.VisitorCodeContract
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.helper.orNotApplicable
import com.glia.widgets.internal.callvisualizer.domain.VisitorCodeRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

internal class VisitorCodeController(
    private val callVisualizerController: CallVisualizerContract.Controller,
    private val visitorCodeRepository: VisitorCodeRepository,
    private val engagementStateUseCase: EngagementStateUseCase,
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase
) : VisitorCodeContract.Controller {

    private var disposable: Disposable? = null

    private var engagementStateDisposable: Disposable? = null

    private val defaultRefreshDurationMilliseconds: Long = 30 * 60 * 1000L

    private var view: VisitorCodeContract.View? = null

    private var isClosable: Boolean? = null

    override fun setView(view: VisitorCodeContract.View, isClosable: Boolean) {
        this.view = view
        this.isClosable = isClosable
        this.view?.notifySetupComplete()
        this.autoCloseOnEngagement()

        GliaLogger.i(LogEvents.VISITOR_CODE_SHOWN) {
            put(EventAttribute.ViewType, telemetryViewType().orNotApplicable)
        }
    }

    override fun onCloseButtonClicked() {
        GliaLogger.i(LogEvents.VISITOR_CODE_BUTTON_CLICKED) {
            put(EventAttribute.ViewType, telemetryViewType().orNotApplicable)
            put(EventAttribute.ButtonName, ButtonNames.CLOSE_VISITOR_CODE)
        }
        callVisualizerController.dismissVisitorCodeDialog()
        view?.destroyTimer()
    }

    override fun onRefreshButtonClicked() {
        GliaLogger.i(LogEvents.VISITOR_CODE_BUTTON_CLICKED) {
            put(EventAttribute.ViewType, telemetryViewType().orNotApplicable)
            put(EventAttribute.ButtonName, ButtonNames.REFRESH_VISITOR_CODE)
        }
        onLoadVisitorCode()
    }

    override fun onLoadVisitorCode() {
        GliaLogger.i(LogEvents.VISITOR_CODE_STATE_CHANGED) {
            put(EventAttribute.ViewType, telemetryViewType().orNotApplicable)
            put(EventAttribute.VisitorCodeState, VisitorCodeState.LOADING)
        }
        view?.startLoading()
        disposable = visitorCodeRepository.getVisitorCode()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { visitorCode ->
                    view?.showVisitorCode(visitorCode)
                    view?.setTimer(failGuardDuration(visitorCode))
                    GliaLogger.i(LogEvents.VISITOR_CODE_STATE_CHANGED) {
                        put(EventAttribute.ViewType, telemetryViewType().orNotApplicable)
                        put(EventAttribute.VisitorCodeState, VisitorCodeState.CODE_SHOWN)
                    }
                },
                { error ->
                    view?.showError(error)
                    GliaLogger.i(LogEvents.VISITOR_CODE_STATE_CHANGED) {
                        put(EventAttribute.ViewType, telemetryViewType().orNotApplicable)
                        put(EventAttribute.VisitorCodeState, VisitorCodeState.ERROR)
                    }
                }
            )
    }

    private fun failGuardDuration(visitorCode: VisitorCode): Long {
        return visitorCode.duration.run {
            if (this <= 0) {
                defaultRefreshDurationMilliseconds
            } else {
                this
            }
        }
    }

    private fun autoCloseOnEngagement() {
        if (isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement) {
            // Normally, there is no need for visitor code if there is already ongoing engagement.
            // But it also doesn't seem right to just close it if right away when it was open
            return
        }
        engagementStateDisposable?.dispose()
        engagementStateDisposable = engagementStateUseCase().filter { it is State.EngagementStarted && it.isCallVisualizer }.subscribe {
            view?.destroyTimer()
        }
    }

    override fun onDestroy() {
        engagementStateDisposable?.dispose()
        view?.destroyTimer()
        view = null

        GliaLogger.i(LogEvents.VISITOR_CODE_CLOSED) {
            put(EventAttribute.ViewType, telemetryViewType().orNotApplicable)
        }
    }

    private fun telemetryViewType(): String? = when (isClosable) {
        true -> ViewType.DIALOG
        false -> ViewType.EMBEDDED
        else -> null
    }
}
