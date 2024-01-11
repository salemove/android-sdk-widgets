package com.glia.widgets.core.visitor;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.comms.Media;
import com.glia.androidsdk.comms.VisitorMediaState;
import com.glia.widgets.helper.Logger;

import java.util.HashSet;
import java.util.Set;
import io.reactivex.Completable;
import io.reactivex.Single;

public class GliaVisitorMediaRepository {
    private final Set<VisitorMediaUpdatesListener> visitorMediaUpdatesListeners = new HashSet<>();

    private VisitorMediaState currentMediaState = null;
    private Media.Status savedVideoStatus = null;
    private Media.Status savedAudioStatus = null;
    private boolean isOnHold = false;

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
        currentMediaState = null;
        isOnHold = false;
        notifyVisitorMediaStateChanged(null);
        visitorMediaUpdatesListeners.clear();
    }

    public void addVisitorMediaStateListener(VisitorMediaUpdatesListener listener) {
        visitorMediaUpdatesListeners.add(listener);
        notifyVisitorMediaStateChanged(currentMediaState);
        notifyOnHoldStateChange(isOnHold);
    }

    public void removeVisitorMediaStateListener(VisitorMediaUpdatesListener listener) {
        visitorMediaUpdatesListeners.remove(listener);
    }

    public Completable muteVisitorAudio() {
        currentMediaState.getAudio().mute();
        notifyVisitorMediaStateChanged(currentMediaState);
        return Completable.complete();
    }

    public Completable unMuteVisitorAudio() {
        currentMediaState.getAudio().unmute();
        notifyVisitorMediaStateChanged(currentMediaState);
        return Completable.complete();
    }

    public Completable resumeVisitorVideo() {
        currentMediaState.getVideo().resume();
        notifyVisitorMediaStateChanged(currentMediaState);
        return Completable.complete();
    }

    public Completable pauseVisitorVideo() {
        currentMediaState.getVideo().pause();
        notifyVisitorMediaStateChanged(currentMediaState);
        return Completable.complete();
    }

    public Single<Media.Status> getVisitorAudioStatus() {
        return Single.just(currentMediaState.getAudio().getStatus());
    }

    public Single<Media.Status> getVisitorVideoStatus() {
        return Single.just(currentMediaState.getVideo().getStatus());
    }

    public Single<Boolean> hasVisitorVideoMedia() {
        return Single.just(currentMediaState != null && currentMediaState.getVideo() != null);
    }

    public Single<Boolean> hasVisitorAudioMedia() {
        return Single.just(currentMediaState != null && currentMediaState.getAudio() != null);
    }

    private void setVideoOnHoldListener() {
        if (hasVisitorVideoMedia().blockingGet()) {
            currentMediaState.getVideo().setOnHoldHandler(this::onVideoHoldChanged);
        }
    }

    private void setAudioOnHoldListener() {
        if (hasVisitorAudioMedia().blockingGet()) {
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
        } else {
            restoreVisitorAudioStatus();
        }
    }

    private void notifyVisitorMediaStateChanged(VisitorMediaState state) {
        currentMediaState = state;
        visitorMediaUpdatesListeners.forEach(listener ->
                listener.onNewVisitorMediaState(currentMediaState)
        );
    }

    private void notifyOnHoldStateChange(boolean newOnHold) {
        isOnHold = newOnHold;
        visitorMediaUpdatesListeners.forEach(listener -> listener.onHoldChanged(isOnHold));
    }

    private void saveVisitorVideoStatus() {
        if (currentMediaState.getVideo() != null) {
            savedVideoStatus = currentMediaState.getVideo().getStatus();
        }
    }

    private void restoreVisitorVideoStatus() {
        if (savedVideoStatus != null) {
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
        if (currentMediaState.getAudio() != null) {
            savedAudioStatus = currentMediaState.getAudio().getStatus();
        }
    }

    private void restoreVisitorAudioStatus() {
        if (savedAudioStatus != null) {
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
