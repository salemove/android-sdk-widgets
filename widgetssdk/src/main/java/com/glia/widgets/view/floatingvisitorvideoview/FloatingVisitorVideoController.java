package com.glia.widgets.view.floatingvisitorvideoview;

import com.glia.androidsdk.comms.VisitorMediaState;
import com.glia.widgets.core.visitor.VisitorMediaUpdatesListener;
import com.glia.widgets.core.visitor.domain.AddVisitorMediaStateListenerUseCase;
import com.glia.widgets.core.visitor.domain.RemoveVisitorMediaStateListenerUseCase;
import com.glia.widgets.view.floatingvisitorvideoview.domain.IsShowOnHoldUseCase;
import com.glia.widgets.view.floatingvisitorvideoview.domain.IsShowVideoUseCase;

import io.reactivex.disposables.CompositeDisposable;

public class FloatingVisitorVideoController
        implements FloatingVisitorVideoContract.Controller, VisitorMediaUpdatesListener {
    private final AddVisitorMediaStateListenerUseCase addVisitorMediaStateListenerUseCase;
    private final RemoveVisitorMediaStateListenerUseCase removeVisitorMediaStateListenerUseCase;
    private final IsShowVideoUseCase isShowVideoUseCase;
    private final IsShowOnHoldUseCase isShowOnHoldUseCase;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private VisitorMediaState visitorMediaState = null;
    private boolean isOnHold = false;

    private FloatingVisitorVideoContract.View view;

    public FloatingVisitorVideoController(
            AddVisitorMediaStateListenerUseCase addVisitorMediaStateListenerUseCase,
            RemoveVisitorMediaStateListenerUseCase removeVisitorMediaStateListenerUseCase,
            IsShowVideoUseCase isShowVideoUseCase,
            IsShowOnHoldUseCase isShowOnHoldUseCase
    ) {
        this.addVisitorMediaStateListenerUseCase = addVisitorMediaStateListenerUseCase;
        this.removeVisitorMediaStateListenerUseCase = removeVisitorMediaStateListenerUseCase;
        this.isShowOnHoldUseCase = isShowOnHoldUseCase;
        this.isShowVideoUseCase = isShowVideoUseCase;
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
        disposables.dispose();
    }

    @Override
    public void setView(FloatingVisitorVideoContract.View view) {
        this.view = view;
    }

    @Override
    public void onNewVisitorMediaState(VisitorMediaState visitorMediaState) {
        this.visitorMediaState = visitorMediaState;
        disposables.add(
                isShowVideoUseCase
                        .execute(visitorMediaState, isOnHold)
                        .subscribe(
                                isShow -> showVisitorVideo(isShow, visitorMediaState),
                                error -> { // no-op
                                }
                        )
        );
    }

    private void showVisitorVideo(boolean isShow, VisitorMediaState mediaState) {
        if (isShow) {
            view.show(mediaState);
        } else {
            view.hide();
        }
    }

    @Override
    public void onHoldChanged(boolean isOnHold) {
        this.isOnHold = isOnHold;
        disposables.add(
            isShowVideoUseCase
                .execute(visitorMediaState, isOnHold)
                .subscribe(
                    isShow -> showVisitorVideo(isShow, null),
                    error -> { // no-op
                    }
                )
        );
        disposables.add(
                isShowOnHoldUseCase
                        .execute(isOnHold)
                        .subscribe(
                                this::showOnHold,
                                error -> { // no-op
                                }
                        )
        );
    }

    private void showOnHold(boolean isShow) {
        if (isShow) {
            view.showOnHold();
        } else {
            view.hideOnHold();
        }
    }
}
