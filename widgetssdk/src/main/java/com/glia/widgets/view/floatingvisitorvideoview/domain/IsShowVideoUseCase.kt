package com.glia.widgets.view.floatingvisitorvideoview.domain

import com.glia.androidsdk.comms.Media
import com.glia.androidsdk.comms.Video
import com.glia.androidsdk.comms.VisitorMediaState
import com.glia.widgets.core.visitor.GliaVisitorMediaRepository
import com.glia.widgets.helper.rx.Schedulers
import io.reactivex.Observable
import java.util.Optional

internal class IsShowVideoUseCase(
    private val schedulers: Schedulers,
    private val visitorMediaRepository: GliaVisitorMediaRepository
) {
    operator fun invoke(): Observable<Pair<Boolean, VisitorMediaState>> = Observable.combineLatest(
        arrayOf(
            visitorMediaRepository.onHoldObserver,
            visitorMediaRepository.visitorMediaStateObserver
        )
    ) {
        val isOnHold = it[0] as Boolean
        val visitorMediaState = (it[1] as Optional<VisitorMediaState>).orElse(null)
        val hasVideoAvailable = hasVideoAvailable(visitorMediaState, isOnHold)
        return@combineLatest Pair(hasVideoAvailable, visitorMediaState)
    }
        .distinctUntilChanged()
        .subscribeOn(schedulers.computationScheduler)
        .observeOn(schedulers.mainScheduler)
        .doOnError { it.printStackTrace() }
        .share()

    private fun hasVideoAvailable(visitorMediaState: VisitorMediaState?, isOnHold: Boolean): Boolean =
        visitorMediaState != null && visitorMediaState.video != null
            && isVideoFeedActiveStatus(visitorMediaState.video, isOnHold)

    private fun isVideoFeedActiveStatus(video: Video, isOnHold: Boolean): Boolean =
        video.status == Media.Status.PLAYING
            || (video.status == Media.Status.PAUSED && isOnHold)
}
