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
import com.glia.androidsdk.site.SiteInfo;
import com.glia.androidsdk.visitor.VisitorInfo;
import com.glia.androidsdk.visitor.VisitorInfoUpdateRequest;

import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;

class GliaCoreImpl implements GliaCore {

    @Override
    public synchronized void init(@NonNull GliaConfig config) throws GliaException {
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
    public <T> void on(@NonNull Glia.OmnicoreEvent<T> event, Consumer<T> listener) {
        Glia.on(event, listener);
    }

    @Override
    public <T> void off(@NonNull Glia.OmnicoreEvent<T> event, Consumer<T> listener) {
        Glia.off(event, listener);
    }

    @Override
    public <T> void off(@NonNull Glia.OmnicoreEvent<T> event) {
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
    public void requestEngagement(@NonNull String operatorId,
                                  @NonNull VisitorContext visitorContext,
                                  @NonNull RequestCallback<OutgoingEngagementRequest> requestCallback) {
        Glia.requestEngagement(operatorId, visitorContext, requestCallback);
    }

    @Override
    public void requestEngagement(@NonNull String operatorId,
                                  @NonNull VisitorContext visitorContext,
                                  @NonNull Engagement.MediaType mediaType,
                                  int mediaPermissionRequestCode,
                                  @NonNull RequestCallback<OutgoingEngagementRequest> requestCallback) {
        Glia.requestEngagement(operatorId, visitorContext, mediaType,
                mediaPermissionRequestCode, requestCallback);
    }

    @Override
    public void requestEngagement(@NonNull String operatorId,
                                  @NonNull VisitorContext visitorContext,
                                  @NonNull Engagement.MediaType mediaType,
                                  @Nullable EngagementOptions engagementOptions,
                                  int mediaPermissionRequestCode,
                                  @NonNull RequestCallback<OutgoingEngagementRequest> requestCallback) {
        Glia.requestEngagement(operatorId, visitorContext, mediaType,
                engagementOptions, mediaPermissionRequestCode, requestCallback);
    }

    @Override
    public void cancelEngagementRequest(@NonNull String engagementRequestId,
                                        Consumer<GliaException> callback) {
        Glia.cancelEngagementRequest(engagementRequestId, callback);
    }

    @Override
    public void queueForEngagement(@NonNull String queueId,
                                   @NonNull VisitorContext visitorContext,
                                   Consumer<GliaException> callback) {
        Glia.queueForEngagement(queueId, visitorContext, callback);
    }

    @Override
    public void queueForEngagement(@NonNull String queueId,
                                   Engagement.MediaType mediaType,
                                   @NonNull VisitorContext visitorContext,
                                   Consumer<GliaException> callback) {
        Glia.queueForEngagement(queueId, mediaType, visitorContext, callback);
    }

    @Override
    public void queueForEngagement(@NonNull String queueId,
                                   @NonNull Engagement.MediaType mediaType,
                                   @NonNull VisitorContext visitorContext,
                                   int mediaPermissionRequestCode,
                                   @NonNull Consumer<GliaException> callback) {
        Glia.queueForEngagement(queueId, mediaType,
                visitorContext, mediaPermissionRequestCode, callback);
    }

    @Override
    public void queueForEngagement(@NonNull String queueId,
                                   @NonNull Engagement.MediaType mediaType,
                                   @NonNull VisitorContext visitorContext,
                                   @Nullable EngagementOptions engagementOptions,
                                   int mediaPermissionRequestCode,
                                   @NonNull Consumer<GliaException> callback) {
        Glia.queueForEngagement(queueId, mediaType, visitorContext,
                engagementOptions, mediaPermissionRequestCode, callback);
    }

    @Override
    public void cancelQueueTicket(@NonNull String queueTicketId,
                                  Consumer<GliaException> callback) {
        Glia.cancelQueueTicket(queueTicketId, callback);
    }

    @Override
    public void subscribeToQueueStateUpdates(@NonNull String[] queueIds,
                                             Consumer<GliaException> onError,
                                             Consumer<Queue> callback) {
        Glia.subscribeToQueueStateUpdates(queueIds, onError, callback);
    }

    @Override
    public void subscribeToQueueStateUpdates(@NonNull String queueId,
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
    public void getSiteInfo(@NonNull RequestCallback<SiteInfo> callback) {
        Glia.getSiteInfo(callback);
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
