package com.glia.widgets.di;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Glia;
import com.glia.androidsdk.GliaConfig;
import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.Operator;
import com.glia.androidsdk.RequestCallback;
import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.comms.EngagementOptions;
import com.glia.androidsdk.engagement.Survey;
import com.glia.androidsdk.fcm.PushNotifications;
import com.glia.androidsdk.queuing.Queue;
import com.glia.androidsdk.queuing.QueueTicket;
import com.glia.androidsdk.secureconversations.SecureConversations;
import com.glia.androidsdk.site.SiteInfo;
import com.glia.androidsdk.visitor.Authentication;
import com.glia.androidsdk.visitor.VisitorInfo;
import com.glia.androidsdk.visitor.VisitorInfoUpdateRequest;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface GliaCore {

    void init(@NonNull GliaConfig config) throws GliaException;

    void onAppCreate(Application application) throws GliaException;

    void getVisitorInfo(final RequestCallback<VisitorInfo> visitorCallback);

    void updateVisitorInfo(
            final VisitorInfoUpdateRequest visitorInfoUpdateRequest,
            final Consumer<GliaException> visitorCallback);

    PushNotifications getPushNotifications();

    <T> void on(@NonNull Glia.OmnicoreEvent<T> event, Consumer<T> listener);

    <T> void off(@NonNull Glia.OmnicoreEvent<T> event, Consumer<T> listener);

    <T> void off(@NonNull Glia.OmnicoreEvent<T> event);

    Optional<Engagement> getCurrentEngagement();

    void fetchFile(AttachmentFile attachmentFile, RequestCallback<InputStream> callback);

    void getChatHistory(RequestCallback<ChatMessage[]> callback);

    void getQueues(final RequestCallback<Queue[]> requestCallback);

    void cancelEngagementRequest(@NonNull String engagementRequestId, Consumer<GliaException> callback);

    void queueForEngagement(@NonNull String queueId, @Nullable String visitorContextAssetId, Consumer<GliaException> callback);

    void queueForEngagement(@NonNull String queueId,
                            @NonNull Engagement.MediaType mediaType,
                            @Nullable String visitorContextAssetId,
                            @Nullable EngagementOptions engagementOptions,
                            int mediaPermissionRequestCode,
                            @NonNull Consumer<GliaException> callback);

    void cancelQueueTicket(@NonNull String queueTicketId, Consumer<GliaException> callback);

    void subscribeToQueueStateUpdates(@NonNull String[] queueIds, Consumer<GliaException> onError, Consumer<Queue> callback);

    void subscribeToQueueStateUpdates(@NonNull String queueId, Consumer<GliaException> onError, Consumer<Queue> callback);

    void unsubscribeFromQueueUpdates(Consumer<GliaException> onError, Consumer<Queue> callback);

    void subscribeToQueueTicketUpdates(@NonNull String ticketId, RequestCallback<QueueTicket> callback);

    void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults);

    void submitSurveyAnswers(@NonNull List<Survey.Answer> answers,
                             @NonNull String surveyId,
                             @NonNull String engagementId,
                             @NonNull Consumer<GliaException> callback);

    boolean isInitialized();

    void clearVisitorSession();

    void getSiteInfo(@NonNull RequestCallback<SiteInfo> callback);

    void getOperator(@NonNull String operatorId, @NonNull RequestCallback<Operator> callback);

    Authentication getAuthentication(@NonNull Authentication.Behavior behavior);

    SecureConversations getSecureConversations();
}
