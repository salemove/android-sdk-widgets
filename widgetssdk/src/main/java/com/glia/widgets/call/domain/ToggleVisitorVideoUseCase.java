package com.glia.widgets.call.domain;

import com.glia.androidsdk.comms.Media;
import com.glia.widgets.call.domain.exception.UnexpectedVideoStatusException;
import com.glia.widgets.call.domain.exception.VisitorVideoMissingException;
import com.glia.widgets.core.visitor.GliaVisitorMediaRepository;
import com.glia.widgets.helper.rx.Schedulers;

import io.reactivex.Completable;
import io.reactivex.Single;

public class ToggleVisitorVideoUseCase {
    private final GliaVisitorMediaRepository repository;
    private final Schedulers schedulers;

    public ToggleVisitorVideoUseCase(
            Schedulers schedulers,
            GliaVisitorMediaRepository repository
    ) {
        this.schedulers = schedulers;
        this.repository = repository;
    }

    public Completable execute() {
        return getVisitorVideoStatusIfHasMedia()
                .flatMapCompletable(this::toggleVisitorVideo)
                .subscribeOn(schedulers.getComputationScheduler())
                .observeOn(schedulers.getMainScheduler());
    }

    private Single<Media.Status> getVisitorVideoStatusIfHasMedia() {
        return repository
                .hasVisitorVideoMedia()
                .flatMap(this::getVisitorVideoStatus);
    }

    private Single<Media.Status> getVisitorVideoStatus(boolean hasMedia) {
        if (hasMedia) {
            return repository.getVisitorVideoStatus();
        } else {
            return Single.error(new VisitorVideoMissingException());
        }
    }

    private Completable toggleVisitorVideo(Media.Status status) {
        switch (status) {
            case PLAYING:
                return repository.pauseVisitorVideo();
            case PAUSED:
                return repository.resumeVisitorVideo();
            case DISCONNECTED:
            default:
                return Completable.error(new UnexpectedVideoStatusException(status));
        }
    }
}
