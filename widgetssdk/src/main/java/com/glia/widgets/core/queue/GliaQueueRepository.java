package com.glia.widgets.core.queue;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Glia;
import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.RequestCallback;
import com.glia.androidsdk.queuing.Queue;
import com.glia.androidsdk.queuing.QueueTicket;
import com.glia.widgets.core.queue.model.GliaQueueingState;
import com.glia.widgets.di.GliaCore;
import com.glia.widgets.helper.Logger;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.Single;

public class GliaQueueRepository {
    private static final String TAG = GliaQueueRepository.class.getSimpleName();
    private static final int MEDIA_PERMISSION_REQUEST_CODE = 1001;

    private final GliaCore gliaCore;
    private GliaQueueingState queueingState = new GliaQueueingState.None();

    public GliaQueueRepository(GliaCore gliaCore) {
        this.gliaCore = gliaCore;
    }

    @VisibleForTesting
    public GliaQueueRepository(GliaCore gliaCore, GliaQueueingState queueingState) {
        this.gliaCore = gliaCore;
        this.queueingState = queueingState;
    }

    public Completable startQueueingForEngagement(
            String queueId,
            String  visitorContextAssetId
    ) {
        Logger.i(TAG, "Start queueing for chat engagement");
        return queueForEngagement(queueId, visitorContextAssetId)
                .mergeWith(
                        awaitTicket()
                                .flatMapCompletable(queueTicket -> setQueueingChat(queueId, queueTicket.getId()))
                );
    }

    public Completable startQueueingForMediaEngagement(String queueId,
                                                       String  visitorContextAssetId,
                                                       Engagement.MediaType mediaType
    ) {
        Logger.i(TAG, "Start queueing for media engagement");
        return queueForMediaEngagement(queueId, visitorContextAssetId, mediaType)
                .mergeWith(
                        awaitTicket()
                                .flatMapCompletable(queueTicket -> setQueueingMedia(queueId, queueTicket.getId()))
                );
    }

    private Single<QueueTicket> awaitTicket() {
        return Single.create(emitter -> gliaCore.on(Glia.Events.QUEUE_TICKET, emitter::onSuccess));
    }

    private Completable queueForMediaEngagement(String queueId,
                                                String  visitorContextAssetId,
                                                Engagement.MediaType mediaType) {
        return Completable.create(emitter ->
                gliaCore.queueForEngagement(
                        queueId,
                        mediaType,
                        visitorContextAssetId,
                        null,
                        MEDIA_PERMISSION_REQUEST_CODE,
                        exception -> {
                            if (exception == null ||
                                    exception.cause == GliaException.Cause.ALREADY_QUEUED) {
                                emitter.onComplete();
                            } else {
                                emitter.onError(exception);
                            }
                        }
                )
        );
    }

    private Completable queueForEngagement(
            String queueId,
            String  visitorContextAssetId
    ) {
        return Completable.create(emitter ->
                gliaCore.queueForEngagement(
                        queueId,
                        visitorContextAssetId,
                        exception -> {
                            if (exception == null ||
                                    exception.cause == GliaException.Cause.ALREADY_QUEUED) {
                                emitter.onComplete();
                            } else {
                                emitter.onError(exception);
                            }
                        }
                )
        );
    }

    public Completable cancelTicket(String ticketId) {
        Logger.i(TAG, "Cancel queue ticket");
        return Completable.create(emitter -> gliaCore.cancelQueueTicket(ticketId, exception -> {
            if (exception != null) {
                Logger.e(TAG, "cancelQueueTicketError: " + exception);
                emitter.onError(exception);
            } else {
                Logger.d(TAG, "cancelQueueTicketSuccess");
                emitter.onComplete();
            }
        })).andThen(setQueueingNone());
    }

    public void onEngagementStarted() {
        setQueueingNone().blockingAwait();
    }

    public Completable setQueueingNone() {
        queueingState = new GliaQueueingState.None();
        return Completable.complete();
    }

    public Completable setQueueingMedia(String queueId, String queueTicket) {
        queueingState = new GliaQueueingState.Media(queueId, queueTicket);
        return Completable.complete();
    }

    public Completable setQueueingChat(String queueId, String queueTicket) {
        queueingState = new GliaQueueingState.Chat(queueId, queueTicket);
        return Completable.complete();
    }

    public GliaQueueingState getQueueingState() {
        return queueingState;
    }

    /**
     * Completes when ongoing queue ticket state changes to {@link QueueTicket.State.UNSTAFFED}
     */
    public Completable observeQueueTicketStateChangeToUnstaffed() {
        return Completable.create(emitter ->
                gliaCore.subscribeToQueueTicketUpdates(
                        queueingState.getTicketId(),
                        emitOnQueueTicketChangeToUnstaffed(emitter)));
    }

    @VisibleForTesting
    @NonNull
    public RequestCallback<QueueTicket> emitOnQueueTicketChangeToUnstaffed(CompletableEmitter emitter) {
        return (queueTicket, e) -> {
            if (queueTicket.getState() == QueueTicket.State.UNSTAFFED) {
                emitter.onComplete();
            }
        };
    }

    /**
     * Emits a list of all Queues
     */
    public Single<Queue[]> getQueues() {
        return Single.create(emitter -> {
            RequestCallback<Queue[]> requestCallback = (queues, e) -> {
                if (e == null) {
                    emitter.onSuccess(queues);
                } else {
                    emitter.onError(e);
                }
            };
            gliaCore.getQueues(requestCallback);
        });
    }
}