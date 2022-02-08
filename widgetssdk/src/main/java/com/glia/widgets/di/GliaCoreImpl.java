package com.glia.widgets.di;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Glia;
import com.glia.androidsdk.GliaConfig;
import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.Operator;
import com.glia.androidsdk.OutgoingEngagementRequest;
import com.glia.androidsdk.RequestCallback;
import com.glia.androidsdk.VisitorContext;
import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.comms.EngagementOptions;
import com.glia.androidsdk.fcm.PushNotifications;
import com.glia.androidsdk.queuing.Queue;
import com.glia.androidsdk.visitor.VisitorInfo;
import com.glia.androidsdk.visitor.VisitorInfoUpdateRequest;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;

class GliaCoreImpl implements GliaCore {

    @Override
    public synchronized void init(@NotNull GliaConfig config) throws GliaException {
        Glia.init(config);
    }

    @Override
    public synchronized void onAppCreate(Application application) throws GliaException {
        Glia.onAppCreate(application);
    }

    @Override
    public void getVisitorInfo(final RequestCallback<VisitorInfo> visitorCallback) {
        Glia.getVisitorInfo(visitorCallback);
    }

    @Override
    public void updateVisitorInfo(
            final VisitorInfoUpdateRequest visitorInfoUpdateRequest,
            final Consumer<GliaException> visitorCallback) {
        Glia.updateVisitorInfo(visitorInfoUpdateRequest, visitorCallback);
    }

    @Override
    public PushNotifications getPushNotifications() {
        return Glia.getPushNotifications();
    }

    @Override
    public <T> void on(@NotNull Glia.OmnicoreEvent<T> event, Consumer<T> listener) {
        Glia.on(event, listener);
    }

    @Override
    public <T> void off(@NotNull Glia.OmnicoreEvent<T> event, Consumer<T> listener) {
        Glia.off(event, listener);
    }

    @Override
    public <T> void off(@NotNull Glia.OmnicoreEvent<T> event) {
        Glia.off(event);
    }

    @Override
    public Optional<Engagement> getCurrentEngagement() {
        return Glia.getCurrentEngagement();
    }

    @Override
    public void fetchFile(AttachmentFile attachmentFile, RequestCallback<InputStream> callback) {
        Glia.fetchFile(attachmentFile, callback);
    }

    @Override
    public void getChatHistory(RequestCallback<ChatMessage[]> callback) {
        Glia.getChatHistory(callback);
    }

    @Override
    public void getQueues(final RequestCallback<Queue[]> requestCallback) {
        Glia.getQueues(requestCallback);
    }

    @Override
    public void getOperators(final RequestCallback<Operator[]> requestCallback) {
        Glia.getOperators(requestCallback);
    }

    @Override
    public void requestEngagement(@NotNull String operatorId,
                                  @NotNull VisitorContext visitorContext,
                                  @NotNull RequestCallback<OutgoingEngagementRequest> requestCallback) {
        Glia.requestEngagement(operatorId, visitorContext, requestCallback);
    }

    @Override
    public void requestEngagement(@NotNull String operatorId,
                                  @NotNull VisitorContext visitorContext,
                                  @NotNull Engagement.MediaType mediaType,
                                  int mediaPermissionRequestCode,
                                  @NotNull RequestCallback<OutgoingEngagementRequest> requestCallback) {
        Glia.requestEngagement(operatorId, visitorContext, mediaType,
                mediaPermissionRequestCode, requestCallback);
    }

    @Override
    public void requestEngagement(@NotNull String operatorId,
                                  @NotNull VisitorContext visitorContext,
                                  @NotNull Engagement.MediaType mediaType,
                                  @Nullable EngagementOptions engagementOptions,
                                  int mediaPermissionRequestCode,
                                  @NotNull RequestCallback<OutgoingEngagementRequest> requestCallback) {
        Glia.requestEngagement(operatorId, visitorContext, mediaType,
                engagementOptions, mediaPermissionRequestCode, requestCallback);
    }

    @Override
    public void cancelEngagementRequest(@NotNull String engagementRequestId,
                                        Consumer<GliaException> callback) {
        Glia.cancelEngagementRequest(engagementRequestId, callback);
    }

    @Override
    public void queueForEngagement(@NotNull String queueId,
                                   @NotNull VisitorContext visitorContext,
                                   Consumer<GliaException> callback) {
        Glia.queueForEngagement(queueId, visitorContext, callback);
    }

    @Override
    public void queueForEngagement(@NotNull String queueId,
                                   Engagement.MediaType mediaType,
                                   @NotNull VisitorContext visitorContext,
                                   Consumer<GliaException> callback) {
        Glia.queueForEngagement(queueId, mediaType, visitorContext, callback);
    }

    @Override
    public void queueForEngagement(@NotNull String queueId,
                                   @NotNull Engagement.MediaType mediaType,
                                   @NotNull VisitorContext visitorContext,
                                   int mediaPermissionRequestCode,
                                   @NotNull Consumer<GliaException> callback) {
        Glia.queueForEngagement(queueId, mediaType,
                visitorContext, mediaPermissionRequestCode, callback);
    }

    @Override
    public void queueForEngagement(@NotNull String queueId,
                                   @NotNull Engagement.MediaType mediaType,
                                   @NotNull VisitorContext visitorContext,
                                   @Nullable EngagementOptions engagementOptions,
                                   int mediaPermissionRequestCode,
                                   @NotNull Consumer<GliaException> callback) {
        Glia.queueForEngagement(queueId, mediaType, visitorContext,
                engagementOptions, mediaPermissionRequestCode, callback);
    }

    @Override
    public void cancelQueueTicket(@NotNull String queueTicketId,
                                  Consumer<GliaException> callback) {
        Glia.cancelQueueTicket(queueTicketId, callback);
    }

    @Override
    public void subscribeToQueueStateUpdates(@NotNull String[] queueIds,
                                             Consumer<GliaException> onError,
                                             Consumer<Queue> callback) {
        Glia.subscribeToQueueStateUpdates(queueIds, onError, callback);
    }

    @Override
    public void subscribeToQueueStateUpdates(@NotNull String queueId,
                                             Consumer<GliaException> onError,
                                             Consumer<Queue> callback) {
        Glia.subscribeToQueueStateUpdates(queueId, onError, callback);
    }

    @Override
    public void unsubscribeFromQueueUpdates(Consumer<GliaException> onError,
                                            Consumer<Queue> callback) {
        Glia.unsubscribeFromQueueUpdates(onError, callback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           @NonNull int[] grantResults) {
        Glia.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean isInitialized() {
        return Glia.isInitialized();
    }

    @Override
    public void clearVisitorSession() {
        Glia.clearVisitorSession();
    }

}
