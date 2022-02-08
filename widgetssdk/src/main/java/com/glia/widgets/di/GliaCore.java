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

public interface GliaCore {

    void init(@NotNull GliaConfig config) throws GliaException;

    void onAppCreate(Application application) throws GliaException;

    void getVisitorInfo(final RequestCallback<VisitorInfo> visitorCallback);

    void updateVisitorInfo(
            final VisitorInfoUpdateRequest visitorInfoUpdateRequest,
            final Consumer<GliaException> visitorCallback);

    PushNotifications getPushNotifications();

    <T> void on(@NotNull Glia.OmnicoreEvent<T> event, Consumer<T> listener);

    <T> void off(@NotNull Glia.OmnicoreEvent<T> event, Consumer<T> listener);

    <T> void off(@NotNull Glia.OmnicoreEvent<T> event);

    Optional<Engagement> getCurrentEngagement();

    void fetchFile(AttachmentFile attachmentFile, RequestCallback<InputStream> callback);

    void getChatHistory(RequestCallback<ChatMessage[]> callback);

    void getQueues(final RequestCallback<Queue[]> requestCallback);

    void getOperators(final RequestCallback<Operator[]> requestCallback);

    void requestEngagement(@NotNull String operatorId,
                           @NotNull VisitorContext visitorContext,
                           @NotNull RequestCallback<OutgoingEngagementRequest> requestCallback);

    void requestEngagement(@NotNull String operatorId,
                           @NotNull VisitorContext visitorContext,
                           @NotNull Engagement.MediaType mediaType,
                           int mediaPermissionRequestCode,
                           @NotNull RequestCallback<OutgoingEngagementRequest> requestCallback);

    void requestEngagement(@NotNull String operatorId,
                           @NotNull VisitorContext visitorContext,
                           @NotNull Engagement.MediaType mediaType,
                           @Nullable EngagementOptions engagementOptions,
                           int mediaPermissionRequestCode,
                           @NotNull RequestCallback<OutgoingEngagementRequest> requestCallback);

    void cancelEngagementRequest(@NotNull String engagementRequestId, Consumer<GliaException> callback);

    void queueForEngagement(@NotNull String queueId, @NotNull VisitorContext visitorContext, Consumer<GliaException> callback);

    void queueForEngagement(@NotNull String queueId, Engagement.MediaType mediaType, @NotNull VisitorContext visitorContext, Consumer<GliaException> callback);

    void queueForEngagement(@NotNull String queueId,
                            @NotNull Engagement.MediaType mediaType,
                            @NotNull VisitorContext visitorContext,
                            int mediaPermissionRequestCode,
                            @NotNull Consumer<GliaException> callback);

    void queueForEngagement(@NotNull String queueId,
                            @NotNull Engagement.MediaType mediaType,
                            @NotNull VisitorContext visitorContext,
                            @Nullable EngagementOptions engagementOptions,
                            int mediaPermissionRequestCode,
                            @NotNull Consumer<GliaException> callback);

    void cancelQueueTicket(@NotNull String queueTicketId, Consumer<GliaException> callback);

    void subscribeToQueueStateUpdates(@NotNull String[] queueIds, Consumer<GliaException> onError, Consumer<Queue> callback);

    void subscribeToQueueStateUpdates(@NotNull String queueId, Consumer<GliaException> onError, Consumer<Queue> callback);

    void unsubscribeFromQueueUpdates(Consumer<GliaException> onError, Consumer<Queue> callback);

    void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults);

    boolean isInitialized();

    void clearVisitorSession();
}
