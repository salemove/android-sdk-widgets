package com.glia.widgets.core.visitor;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.comms.Media;
import com.glia.androidsdk.comms.VisitorMediaState;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

public class GliaVisitorMediaRepository {
    private final Set<VisitorMediaUpdatesListener> visitorMediaUpdatesListeners = new HashSet<>();
    private Media.Status savedVideoStatus = null;
    private Media.Status savedAudioStatus = null;
    private final BehaviorSubject<Boolean> onHoldObserver = BehaviorSubject.createDefault(false);
    private final BehaviorSubject<Optional<VisitorMediaState>> visitorMediaStateObserver = BehaviorSubject.create();

    public void onNewVisitorMediaState(VisitorMediaState state) {
        notifyVisitorMediaStateChanged(state);
        setAudioOnHoldListener();
        setVideoOnHoldListener();
    }

    public void onAudioHoldChanged(boolean isOnHold) {
        notifyOnHoldStateChange(isOnHold);
        visitorAudioMediaStatusOnHold(isOnHold);
    }

    public void onVideoHoldChanged(boolean isOnHold) {
        notifyOnHoldStateChange(isOnHold);
        visitorVideoMediaStatusOnHold(isOnHold);
    }

    public void onEngagementStarted(Engagement engagement) {
        engagement.getMedia().on(Media.Events.VISITOR_STATE_UPDATE, this::onNewVisitorMediaState);
    }

    public void onEngagementEnded(Engagement engagement) {
        engagement.getMedia().off(Media.Events.VISITOR_STATE_UPDATE);
        visitorMediaStateObserver.onNext(Optional.empty());
        onHoldObserver.onNext(false);
        notifyVisitorMediaStateChanged(null);
        visitorMediaUpdatesListeners.clear();
    }

    public void addVisitorMediaStateListener(VisitorMediaUpdatesListener listener) {
        visitorMediaUpdatesListeners.add(listener);
        notifyVisitorMediaStateChanged(getCurrentMediaState());
        notifyOnHoldStateChange(onHoldObserver.getValue());
    }

    public void removeVisitorMediaStateListener(VisitorMediaUpdatesListener listener) {
        visitorMediaUpdatesListeners.remove(listener);
    }

    public Completable muteVisitorAudio() {
        VisitorMediaState currentMediaState = getCurrentMediaState();
        currentMediaState.getAudio().mute();
        notifyVisitorMediaStateChanged(currentMediaState);
        return Completable.complete();
    }

    public Completable unMuteVisitorAudio() {
        VisitorMediaState currentMediaState = getCurrentMediaState();
        currentMediaState.getAudio().unmute();
        notifyVisitorMediaStateChanged(currentMediaState);
        return Completable.complete();
    }

    public Completable resumeVisitorVideo() {
        VisitorMediaState currentMediaState = getCurrentMediaState();
        currentMediaState.getVideo().resume();
        notifyVisitorMediaStateChanged(currentMediaState);
        return Completable.complete();
    }

    public Completable pauseVisitorVideo() {
        VisitorMediaState currentMediaState = getCurrentMediaState();
        currentMediaState.getVideo().pause();
        notifyVisitorMediaStateChanged(currentMediaState);
        return Completable.complete();
    }

    public Single<Media.Status> getVisitorAudioStatus() {
        VisitorMediaState currentMediaState = getCurrentMediaState();
        return Single.just(currentMediaState.getAudio().getStatus());
    }

    public Single<Media.Status> getVisitorVideoStatus() {
        VisitorMediaState currentMediaState = getCurrentMediaState();
        return Single.just(currentMediaState.getVideo().getStatus());
    }

    public Single<Boolean> hasVisitorVideoMedia() {
        VisitorMediaState currentMediaState = getCurrentMediaState();
        return Single.just(currentMediaState != null && currentMediaState.getVideo() != null);
    }

    public Single<Boolean> hasVisitorAudioMedia() {
        VisitorMediaState currentMediaState = getCurrentMediaState();
        return Single.just(currentMediaState != null && currentMediaState.getAudio() != null);
    }

    public Observable<Boolean> getOnHoldObserver() {
        return onHoldObserver;
    }

    public Observable<Optional<VisitorMediaState>> getVisitorMediaStateObserver() {
        return visitorMediaStateObserver;
    }

    private VisitorMediaState getCurrentMediaState() {
        Optional<VisitorMediaState> value = visitorMediaStateObserver.getValue();
        if (value == null) {
            return null;
        }
        return value.orElse(null);
    }

    private void setVideoOnHoldListener() {
        if (hasVisitorVideoMedia().blockingGet()) {
            VisitorMediaState currentMediaState = getCurrentMediaState();
            currentMediaState.getVideo().setOnHoldHandler(this::onVideoHoldChanged);
        }
    }

    private void setAudioOnHoldListener() {
        if (hasVisitorAudioMedia().blockingGet()) {
            VisitorMediaState currentMediaState = getCurrentMediaState();
            currentMediaState.getAudio().setOnHoldHandler(this::onAudioHoldChanged);
        }
    }

    private void visitorVideoMediaStatusOnHold(boolean isOnHold) {
        if (isOnHold) {
            saveVisitorVideoStatus();
            pauseVisitorVideo();
        } else {
            restoreVisitorVideoStatus();
        }
    }

    private void visitorAudioMediaStatusOnHold(boolean isOnHold) {
        if (isOnHold) {
            saveVisitorAudioStatus();
            muteVisitorAudio();
        } else {
            restoreVisitorAudioStatus();
        }
    }

    private void notifyVisitorMediaStateChanged(VisitorMediaState state) {
        visitorMediaStateObserver.onNext(Optional.ofNullable(state));
        visitorMediaUpdatesListeners.forEach(listener ->
                listener.onNewVisitorMediaState(state)
        );
    }

    private void notifyOnHoldStateChange(boolean newOnHold) {
        onHoldObserver.onNext(newOnHold);
        visitorMediaUpdatesListeners.forEach(listener -> listener.onHoldChanged(newOnHold));
    }

    private void saveVisitorVideoStatus() {
        VisitorMediaState currentMediaState = getCurrentMediaState();
        if (currentMediaState.getVideo() != null) {
            savedVideoStatus = currentMediaState.getVideo().getStatus();
        }
    }

    private void restoreVisitorVideoStatus() {
        if (savedVideoStatus != null) {
            VisitorMediaState currentMediaState = getCurrentMediaState();
            if (currentMediaState.getVideo() != null) {
                if (savedVideoStatus.equals(Media.Status.PAUSED)) {
                    currentMediaState.getVideo().pause();
                    notifyVisitorMediaStateChanged(currentMediaState);
                } else if (savedVideoStatus.equals(Media.Status.PLAYING)) {
                    currentMediaState.getVideo().resume();
                    notifyVisitorMediaStateChanged(currentMediaState);
                }
            }
            savedVideoStatus = null;
        }
    }

    private void saveVisitorAudioStatus() {
        VisitorMediaState currentMediaState = getCurrentMediaState();
        if (currentMediaState.getAudio() != null) {
            savedAudioStatus = currentMediaState.getAudio().getStatus();
        }
    }

    private void restoreVisitorAudioStatus() {
        if (savedAudioStatus != null) {
            VisitorMediaState currentMediaState = getCurrentMediaState();
            if (currentMediaState.getAudio() != null) {
                if (savedAudioStatus.equals(Media.Status.PAUSED)) {
                    muteVisitorAudio();
                } else if (savedAudioStatus.equals(Media.Status.PLAYING)) {
                    unMuteVisitorAudio();
                }
            }
            savedAudioStatus = null;
        }
    }
}
