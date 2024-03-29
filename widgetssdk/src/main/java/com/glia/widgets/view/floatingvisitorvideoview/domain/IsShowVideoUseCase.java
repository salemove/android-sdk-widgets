package com.glia.widgets.view.floatingvisitorvideoview.domain;

import com.glia.androidsdk.comms.Media;
import com.glia.androidsdk.comms.MediaState;
import com.glia.androidsdk.comms.Video;
import com.glia.widgets.helper.rx.Schedulers;

import io.reactivex.Single;

/**
 * @hide
 */
public class IsShowVideoUseCase {
    private final Schedulers schedulers;

    public IsShowVideoUseCase(Schedulers schedulers) {
        this.schedulers = schedulers;
    }

    public Single<Boolean> execute(MediaState visitorMediaState, boolean isOnHold) {
        return Single.just(hasVideoAvailable(visitorMediaState, isOnHold))
                .subscribeOn(schedulers.getComputationScheduler())
                .observeOn(schedulers.getMainScheduler());
    }

    private boolean hasVideoAvailable(MediaState visitorMediaState, boolean isOnHold) {
        return visitorMediaState != null &&
                visitorMediaState.getVideo() != null &&
                isVideoFeedActiveStatus(visitorMediaState.getVideo(), isOnHold);
    }

    private boolean isVideoFeedActiveStatus(Video video, boolean isOnHold) {
        return video.getStatus() == Media.Status.PLAYING ||
            (video.getStatus() == Media.Status.PAUSED && isOnHold);
    }
}
