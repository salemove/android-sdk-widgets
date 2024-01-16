package com.glia.widgets.view.floatingvisitorvideoview.domain;

import com.glia.widgets.helper.rx.Schedulers;

import io.reactivex.Single;

public class IsShowOnHoldUseCase {
    private final Schedulers schedulers;

    public IsShowOnHoldUseCase(Schedulers schedulers) {
        this.schedulers = schedulers;
    }

    public Single<Boolean> execute(boolean isOnHold) {
        return Single.just(isOnHold)
                .subscribeOn(schedulers.getComputationScheduler())
                .observeOn(schedulers.getMainScheduler());
    }
}