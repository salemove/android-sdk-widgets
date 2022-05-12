package com.glia.widgets.call.domain;

import com.glia.androidsdk.comms.Media;
import com.glia.widgets.call.domain.exception.UnexpectedAudioStatusException;
import com.glia.widgets.call.domain.exception.VisitorAudioMissingException;
import com.glia.widgets.core.visitor.GliaVisitorMediaRepository;
import com.glia.widgets.helper.rx.Schedulers;

import io.reactivex.Completable;
import io.reactivex.Single;

public class ToggleVisitorAudioMediaMuteUseCase {
    private final Schedulers schedulers;
    private final GliaVisitorMediaRepository repository;

    public ToggleVisitorAudioMediaMuteUseCase(
            Schedulers schedulers,
            GliaVisitorMediaRepository repository
    ) {
        this.schedulers = schedulers;
        this.repository = repository;
    }

    public Completable execute() {
        return getVisitorAudioMediaStatusIfHasMedia()
                .flatMapCompletable(this::toggleVisitorAudio)
                .subscribeOn(schedulers.getComputationScheduler())
                .observeOn(schedulers.getMainScheduler());
    }

    private Single<Media.Status> getVisitorAudioMediaStatusIfHasMedia() {
        return repository.hasVisitorVideoMedia()
                .flatMap(this::getVisitorAudioMediaStatus);
    }

    private Single<Media.Status> getVisitorAudioMediaStatus(boolean hasMedia) {
        if (hasMedia) {
            return repository.getVisitorAudioStatus();
        } else {
            return Single.error(new VisitorAudioMissingException());
        }
    }

    private Completable toggleVisitorAudio(Media.Status status) {
        switch (status) {
            case PLAYING:
                return repository.muteVisitorAudio();
            case PAUSED:
                return repository.unMuteVisitorAudio();
            case DISCONNECTED:
            default:
                return Completable.error(new UnexpectedAudioStatusException(status));
        }
    }
}
