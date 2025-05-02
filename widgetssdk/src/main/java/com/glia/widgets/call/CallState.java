package com.glia.widgets.call;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.comms.Media;
import com.glia.androidsdk.comms.MediaState;
import com.glia.androidsdk.comms.Video;
import com.glia.widgets.engagement.MediaType;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.view.floatingvisitorvideoview.FloatingVisitorVideoContract.FlipButtonState;

import java.util.Objects;

class CallState {
    private static final String TAG = CallState.class.getSimpleName();

    public final boolean integratorCallStarted;
    public final boolean isVisible;
    public final int messagesNotSeen;
    public final CallStatus callStatus;
    public final boolean landscapeLayoutControlsVisible;
    public final boolean isMuted;
    public final boolean hasVideo;
    @Nullable
    public final MediaType requestedMediaType;
    public final boolean isSpeakerOn;
    public final boolean isOnHold;
    //    Need this to not update all views when only time is changed.
    public final boolean isOnlyTimeChanged;
    public final boolean isCallVisualizer;

    public final boolean isSharingScreen;
    public final FlipButtonState flipButtonState;

    private CallState(Builder builder) {
        this.integratorCallStarted = builder.integratorCallStarted;
        this.isVisible = builder.isVisible;
        this.messagesNotSeen = builder.messagesNotSeen;
        this.callStatus = builder.callStatus;
        this.landscapeLayoutControlsVisible = builder.landscapeLayoutControlsVisible;
        this.isMuted = builder.isMuted;
        this.hasVideo = builder.hasVideo;
        this.requestedMediaType = builder.requestedMediaType;
        this.isSpeakerOn = builder.isSpeakerOn;
        this.isOnHold = builder.isOnHold;
        this.isOnlyTimeChanged = builder.isOnlyTimeChanged;
        this.isCallVisualizer = builder.isCallVisualizer;
        this.isSharingScreen = builder.isSharingScreen;
        this.flipButtonState = builder.flipButtonState;
    }

    public static CallState initial(Boolean isCallVisualizer) {
        return new Builder()
            .setIntegratorCallStarted(false)
            .setVisible(false)
            .setMessagesNotSeen(0)
            .setCallStatus(new CallStatus.EngagementNotOngoing(null))
            .setLandscapeLayoutControlsVisible(false)
            .setIsSpeakerOn(false)
            .setIsMuted(false)
            .setHasVideo(false)
            .setIsCallVisualizer(isCallVisualizer)
            .createCallState();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CallState callState = (CallState) o;
        return integratorCallStarted == callState.integratorCallStarted &&
            isVisible == callState.isVisible &&
            messagesNotSeen == callState.messagesNotSeen &&
            landscapeLayoutControlsVisible == callState.landscapeLayoutControlsVisible &&
            isMuted == callState.isMuted &&
            hasVideo == callState.hasVideo &&
            Objects.equals(callStatus, callState.callStatus) &&
            Objects.equals(requestedMediaType, callState.requestedMediaType) &&
            isSpeakerOn == callState.isSpeakerOn &&
            isOnHold == callState.isOnHold &&
            isOnlyTimeChanged == callState.isOnlyTimeChanged &&
            isCallVisualizer == callState.isCallVisualizer &&
            isSharingScreen == callState.isSharingScreen &&
            flipButtonState == callState.flipButtonState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(integratorCallStarted, isVisible, messagesNotSeen,
            callStatus, landscapeLayoutControlsVisible, isMuted, hasVideo,
            requestedMediaType, isSpeakerOn, isOnHold, isOnlyTimeChanged,
            isCallVisualizer, isSharingScreen, flipButtonState);
    }

    public boolean showOperatorStatusView() {
        return isCallNotOngoing() || isCallOngoingAndOperatorIsConnecting() || isAudioCall() || showOnHold() || isTransferring();
    }

    public boolean isMediaEngagementStarted() {
        return isCallOngoingAndOperatorIsConnecting() || isCallOngoingAndOperatorConnected() || isTransferring();
    }

    public boolean isCallNotOngoing() {
        return callStatus instanceof CallStatus.EngagementNotOngoing;
    }

    public boolean isCallOngoingAndOperatorIsConnecting() {
        return callStatus instanceof CallStatus.EngagementOngoingOperatorIsConnecting;
    }

    public boolean isCallOngoingAndOperatorConnected() {
        return isAudioCall() || isVideoCall();
    }

    public boolean isTransferring() {
        return callStatus instanceof CallStatus.EngagementOngoingTransferring;
    }

    public boolean isVideoCall() {
        return callStatus instanceof CallStatus.EngagementOngoingVideoCallStarted;
    }

    public boolean is2WayVideoCall() {
        return callStatus instanceof CallStatus.EngagementOngoingVideoCallStarted && isVisitorVideoAvailable();
    }

    public boolean isVideoCallAndOperatorVideoIsConnected() {
        if (isVideoCall() && isOperatorVideoAvailable()) {
            Video operatorVideo = callStatus.getOperatorMediaState().getVideo();
            return operatorVideo.getStatus() == Media.Status.PLAYING || operatorVideo.getStatus() == Media.Status.PAUSED;
        } else {
            return false;
        }
    }

    public boolean isAudioCall() {
        return callStatus instanceof CallStatus.EngagementOngoingAudioCallStarted;
    }

    public Boolean isCurrentCallVideo() {
        if (requestedMediaType == null && callStatus.getOperatorMediaState() == null) return null;

        return callStatus.getOperatorMediaState() == null ? requestedMediaType == MediaType.VIDEO : callStatus.getOperatorMediaState().getVideo() != null;
    }

    @NonNull
    @Override
    public String toString() {
        return "CallState{" +
            "integratorCallStarted=" + integratorCallStarted +
            ", isVisible=" + isVisible +
            ", messagesNotSeen=" + messagesNotSeen +
            ", callStatus=" + callStatus +
            ", landscapeLayoutControlsVisible=" + landscapeLayoutControlsVisible +
            ", isMuted=" + isMuted +
            ", hasVideo=" + hasVideo +
            ", requestedMediaType: " + requestedMediaType +
            ", isSpeakerOn: " + isSpeakerOn +
            ", isOnHold: " + isOnHold +
            ", isCallVisualizer: " + isCallVisualizer +
            ", isSharingScreen: " + isSharingScreen +
            '}';
    }

    public CallState stop() {
        return new Builder()
            .copyFrom(this)
            .setIntegratorCallStarted(false)
            .setVisible(false)
            .setIsOnHold(false)
            .setIsSharingScreen(false)
            .setCallStatus(new CallStatus.EngagementNotOngoing(callStatus.getVisitorMediaState()))
            .createCallState();
    }

    public CallState changeNumberOfMessages(int numberOfMessages) {
        return new Builder()
            .copyFrom(this)
            .setMessagesNotSeen(numberOfMessages)
            .createCallState();
    }

    public CallState changeRequestedMediaType(MediaType requestedMediaType) {
        return new Builder()
            .copyFrom(this)
            .setRequestedMediaType(requestedMediaType)
            .createCallState();
    }

    public CallState operatorConnecting(String name, String url) {
        return new Builder()
            .copyFrom(this)
            .setCallStatus(
                new CallStatus.EngagementOngoingOperatorIsConnecting(
                    name,
                    "0",
                    url,
                    callStatus.getVisitorMediaState()
                )
            ).createCallState();
    }

    public CallState engagementStarted() {
        Logger.i(TAG, "Engagement started");
        return new Builder()
            .copyFrom(this)
            .setIsOnHold(false)
            .createCallState();
    }

    public CallState backToOngoing() {
        return new Builder()
            .copyFrom(this)
            .setCallStatus(
                new CallStatus.EngagementOngoingOperatorIsConnecting(
                    callStatus.getFormattedOperatorName(),
                    "0",
                    callStatus.getOperatorProfileImageUrl(),
                    callStatus.getVisitorMediaState()
                )
            )
            .createCallState();
    }

    public CallState changeVisibility(boolean isVisible) {
        return new Builder()
            .copyFrom(this)
            .setVisible(isVisible)
            .createCallState();
    }

    public CallState videoCallOperatorVideoStarted(
        MediaState operatorMediaState,
        String formattedTime
    ) {
        return new Builder()
            .copyFrom(this)
            .setCallStatus(
                new CallStatus.EngagementOngoingVideoCallStarted(
                    callStatus.getFormattedOperatorName(),
                    formattedTime,
                    callStatus.getOperatorProfileImageUrl(),
                    operatorMediaState,
                    callStatus.getVisitorMediaState()))
            .setLandscapeLayoutControlsVisible(true)
            .createCallState();
    }

    public CallState visitorMediaStateChanged(MediaState visitorMediaState) {
        callStatus.setVisitorMediaState(visitorMediaState);
        return new Builder()
            .copyFrom(this)
            .setHasVideo(isVisitorVideoPlaying(visitorMediaState))
            .setIsMuted(isMuted(visitorMediaState))
            .createCallState();
    }

    public CallState flipButtonStateChanged(FlipButtonState showFlipVisitorCameraButton) {
        return new Builder()
            .copyFrom(this)
            .setFlipButtonState(showFlipVisitorCameraButton)
            .createCallState();
    }

    public CallState audioCallStarted(MediaState operatorMediaState, String formattedTime) {
        Logger.i(TAG, "Audio or video call started");
        return new Builder()
            .copyFrom(this)
            .setCallStatus(
                new CallStatus.EngagementOngoingAudioCallStarted(
                    callStatus.getFormattedOperatorName(),
                    formattedTime,
                    callStatus.getOperatorProfileImageUrl(),
                    operatorMediaState,
                    callStatus.getVisitorMediaState())
            )
            .setLandscapeLayoutControlsVisible(true)
            .createCallState();
    }

    public CallState newStartedCallTimerValue(String formattedTimeValue) {
        if (isAudioCall()) {
            return new Builder()
                .copyFrom(this)
                .setCallStatus(
                    new CallStatus.EngagementOngoingAudioCallStarted(
                        callStatus.getFormattedOperatorName(),
                        formattedTimeValue,
                        callStatus.getOperatorProfileImageUrl(),
                        callStatus.getOperatorMediaState(),
                        callStatus.getVisitorMediaState()
                    )
                )
                .setOnlyTimeChanged(true)
                .createCallState();
        } else if (isVideoCall()) {
            return new Builder()
                .copyFrom(this)
                .setCallStatus(
                    new CallStatus.EngagementOngoingVideoCallStarted(
                        callStatus.getFormattedOperatorName(),
                        formattedTimeValue,
                        callStatus.getOperatorProfileImageUrl(),
                        callStatus.getOperatorMediaState(),
                        callStatus.getVisitorMediaState()
                    )
                )
                .setOnlyTimeChanged(true)
                .createCallState();
        } else if (isTransferring()) {
            return new Builder()
                .copyFrom(this)
                .setCallStatus(
                    new CallStatus.EngagementOngoingTransferring(
                        formattedTimeValue,
                        callStatus.getVisitorMediaState()
                    )
                )
                .setOnlyTimeChanged(true)
                .createCallState();
        } else {
            return this;
        }
    }

    public CallState connectingTimerValueChanged(String timeValue) {
        if (isCallOngoingAndOperatorIsConnecting()) {
            return new Builder()
                .copyFrom(this)
                .setCallStatus(
                    new CallStatus.EngagementOngoingOperatorIsConnecting(
                        callStatus.getFormattedOperatorName(),
                        timeValue,
                        callStatus.getOperatorProfileImageUrl(),
                        callStatus.getVisitorMediaState()
                    )
                )
                .setOnlyTimeChanged(true)
                .createCallState();
        } else {
            return this;
        }
    }

    public CallState landscapeControlsVisibleChanged(boolean visible) {
        return new Builder()
            .copyFrom(this)
            .setLandscapeLayoutControlsVisible(visible)
            .createCallState();
    }

    public CallState speakerValueChanged(boolean isSpeakerOn) {
        Logger.i(TAG, "Speaker value changed to " + isSpeakerOn);
        return new Builder()
            .copyFrom(this)
            .setIsSpeakerOn(isSpeakerOn)
            .createCallState();
    }

    public CallState setOnHold(boolean isOnHold) {
        return new Builder()
            .copyFrom(this)
            .setIsOnHold(isOnHold)
            .createCallState();
    }

    public CallState setTransferring() {
        Logger.i(TAG, "Transfer the call");
        return new Builder()
            .copyFrom(this)
            .setCallStatus(
                new CallStatus.EngagementOngoingTransferring(
                    callStatus.getTime(),
                    callStatus.getVisitorMediaState()
                )
            )
            .createCallState();
    }

    public CallState startScreenSharing() {
        return new Builder().copyFrom(this).setIsSharingScreen(true).createCallState();
    }

    public CallState endScreenSharing() {
        return new Builder().copyFrom(this).setIsSharingScreen(false).createCallState();
    }

    public ViewState getMuteButtonViewState() {
        if (isCallVisualizer) {
            return ViewState.HIDE;
        } else if ((isAudioCall() || isVideoCall()) && !showOnHold()) {
            return ViewState.SHOW;
        } else {
            return ViewState.DISABLE;
        }
    }

    public boolean isVideoButtonEnabled() {
        return is2WayVideoCall() && !showOnHold();
    }

    public ViewState getSpeakerButtonViewState() {
        if (isCallVisualizer) {
            return ViewState.HIDE;
        } else if (isAudioCall() || isVideoCall()) {
            return ViewState.SHOW;
        } else {
            return ViewState.DISABLE;
        }
    }

    public ViewState getChatButtonViewState() {
        if (isCallVisualizer) {
            return ViewState.HIDE;
        } else {
            return ViewState.SHOW;
        }
    }

    public boolean showCallTimerView() {
        return isCallOngoingAndOperatorConnected() && !showOnHold() || isTransferring();
    }

    public boolean showVisitorVideo() {
        return isVisible && isVisitorVideoPlaying(callStatus.getVisitorMediaState());
    }

    public boolean showOperatorVideo() {
        return isVisible && isVideoCallAndOperatorVideoIsConnected() && !showOnHold();
    }

    public boolean showContinueBrowsingView() {
        return isCallOngoingAndOperatorIsConnecting() || isCallNotOngoing() || showOnHold();
    }

    public boolean showOperatorNameView() {
        return isCallOngoingAndOperatorConnected() || isTransferring();
    }

    public boolean showCompanyNameView() {
        return !isMediaEngagementStarted();
    }

    public boolean showOperatorStatusViewRippleAnimation() {
        return isCallNotOngoing() || isCallOngoingAndOperatorIsConnecting() || isTransferring();
    }

    public boolean showOnHold() {
        return isOnHold && !isTransferring() && !isCallOngoingAndOperatorIsConnecting();
    }

    private boolean isVisitorVideoAvailable() {
        return callStatus.getVisitorMediaState() != null &&
            callStatus.getVisitorMediaState().getVideo() != null;
    }

    private boolean isOperatorVideoAvailable() {
        return callStatus.getOperatorMediaState() != null &&
            callStatus.getOperatorMediaState().getVideo() != null;
    }

    private boolean isVisitorVideoPlaying(MediaState visitorMediaState) {
        return visitorMediaState != null &&
            visitorMediaState.getVideo() != null &&
            visitorMediaState.getVideo().getStatus() == Media.Status.PLAYING;
    }

    private boolean isMuted(MediaState visitorMediaState) {
        return visitorMediaState == null ||
            visitorMediaState.getAudio() == null ||
            visitorMediaState.getAudio().getStatus() != Media.Status.PLAYING;
    }

    public CallState initCall(@Nullable MediaType requestedMediaType) {
        return new Builder()
            .copyFrom(this)
            .setIntegratorCallStarted(true)
            .setVisible(true)
            .setIsOnHold(false)
            .setRequestedMediaType(requestedMediaType)
            .createCallState();
    }

    public enum ViewState {
        SHOW, DISABLE, HIDE
    }

    public static class Builder {
        private boolean integratorCallStarted;
        private boolean isVisible;
        private int messagesNotSeen;
        private CallStatus callStatus;
        private boolean landscapeLayoutControlsVisible;
        private boolean isMuted;
        private boolean hasVideo;
        private MediaType requestedMediaType;
        private boolean isSpeakerOn;
        private boolean isOnHold;
        //Maybe helpful when converting to Kotlin, as an android studio makes fields nullable.
        private boolean isOnlyTimeChanged = false;
        private boolean isCallVisualizer;
        private boolean isSharingScreen;
        private FlipButtonState flipButtonState = FlipButtonState.HIDE;

        public Builder setIntegratorCallStarted(boolean integratorCallStarted) {
            this.integratorCallStarted = integratorCallStarted;
            return this;
        }

        public Builder setVisible(boolean visible) {
            isVisible = visible;
            return this;
        }

        public Builder setMessagesNotSeen(int messagesNotSeen) {
            this.messagesNotSeen = messagesNotSeen;
            return this;
        }

        public Builder setCallStatus(CallStatus callStatus) {
            this.callStatus = callStatus;
            return this;
        }

        public Builder setLandscapeLayoutControlsVisible(boolean visible) {
            this.landscapeLayoutControlsVisible = visible;
            return this;
        }

        public Builder setIsMuted(boolean isMuted) {
            this.isMuted = isMuted;
            return this;
        }

        public Builder setHasVideo(boolean hasVideo) {
            this.hasVideo = hasVideo;
            return this;
        }

        public Builder setRequestedMediaType(MediaType requestedMediaType) {
            this.requestedMediaType = requestedMediaType;
            return this;
        }

        public Builder setIsSpeakerOn(boolean isSpeakerOn) {
            this.isSpeakerOn = isSpeakerOn;
            return this;
        }

        public Builder setIsOnHold(boolean isOnHold) {
            this.isOnHold = isOnHold;
            return this;
        }

        public Builder setOnlyTimeChanged(boolean isOnlyTimeChanged) {
            this.isOnlyTimeChanged = isOnlyTimeChanged;
            return this;
        }

        public Builder setIsCallVisualizer(boolean isCallVisualizer) {
            this.isCallVisualizer = isCallVisualizer;
            return this;
        }

        public Builder setIsSharingScreen(Boolean isSharingScreen) {
            this.isSharingScreen = isSharingScreen;
            return this;
        }

        public Builder setFlipButtonState(FlipButtonState flipButtonState) {
            this.flipButtonState = flipButtonState;
            return this;
        }

        Builder copyFrom(CallState callState) {
            integratorCallStarted = callState.integratorCallStarted;
            isVisible = callState.isVisible;
            messagesNotSeen = callState.messagesNotSeen;
            callStatus = callState.callStatus;
            landscapeLayoutControlsVisible = callState.landscapeLayoutControlsVisible;
            isMuted = callState.isMuted;
            hasVideo = callState.hasVideo;
            requestedMediaType = callState.requestedMediaType;
            isSpeakerOn = callState.isSpeakerOn;
            isOnHold = callState.isOnHold;
            //as we are updating this field only when only time is changed, so needs to make it false every time.
            isOnlyTimeChanged = false;
            isCallVisualizer = callState.isCallVisualizer;
            isSharingScreen = callState.isSharingScreen;
            flipButtonState = callState.flipButtonState;
            return this;
        }

        CallState createCallState() {
            return new CallState(this);
        }
    }
}
