package com.glia.widgets.view.floatingvisitorvideoview;

import com.glia.androidsdk.comms.Media;
import com.glia.androidsdk.comms.Video;
import com.glia.androidsdk.comms.VisitorMediaState;
import com.glia.widgets.core.visitor.VisitorMediaUpdatesListener;
import com.glia.widgets.core.visitor.domain.AddVisitorMediaStateListenerUseCase;
import com.glia.widgets.core.visitor.domain.RemoveVisitorMediaStateListenerUseCase;

public class FloatingVisitorVideoController
        implements FloatingVisitorVideoContract.Controller, VisitorMediaUpdatesListener {
    private final AddVisitorMediaStateListenerUseCase addVisitorMediaStateListenerUseCase;
    private final RemoveVisitorMediaStateListenerUseCase removeVisitorMediaStateListenerUseCase;

    private FloatingVisitorVideoContract.View view;

    public FloatingVisitorVideoController(
            AddVisitorMediaStateListenerUseCase addVisitorMediaStateListenerUseCase,
            RemoveVisitorMediaStateListenerUseCase removeVisitorMediaStateListenerUseCase
    ) {
        this.addVisitorMediaStateListenerUseCase = addVisitorMediaStateListenerUseCase;
        this.removeVisitorMediaStateListenerUseCase = removeVisitorMediaStateListenerUseCase;
    }

    @Override
    public void onResume() {
        addVisitorMediaStateListenerUseCase.execute(this);
    }

    @Override
    public void onPause() {
        removeVisitorMediaStateListenerUseCase.execute(this);
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void setView(FloatingVisitorVideoContract.View view) {
        this.view = view;
    }

    @Override
    public void onNewVisitorMediaState(VisitorMediaState visitorMediaState) {
        if (hasVideoAvailable(visitorMediaState)) {
            view.show(visitorMediaState);
        } else {
            view.hide();
        }
    }

    @Override
    public void onHoldChanged(boolean isOnHold) {
        if (isOnHold) {
            view.showOnHold();
        } else {
            view.hideOnHold();
        }
    }

    private boolean hasVideoAvailable(VisitorMediaState visitorMediaState) {
        return visitorMediaState != null &&
                visitorMediaState.getVideo() != null &&
                isVideoFeedActiveStatus(visitorMediaState.getVideo());
    }

    private boolean isVideoFeedActiveStatus(Video video) {
        return video.getStatus() == Media.Status.PLAYING ||
                video.getStatus() == Media.Status.PAUSED;
    }
}
