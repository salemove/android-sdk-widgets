package com.glia.widgets.core.secureconversations.domain;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.queuing.Queue;
import com.glia.androidsdk.queuing.QueueState;
import com.glia.widgets.core.queue.GliaQueueRepository;
import com.glia.widgets.helper.rx.Schedulers;

import org.junit.Before;
import org.junit.Test;

public class IsMessageCenterAvailableUseCaseTest {
    private IsMessageCenterAvailableUseCase isMessageCenterAvailableUseCase;

    // Media types
    private final Engagement.MediaType[] mediaTypesWithMessaging = {
            Engagement.MediaType.TEXT,
            Engagement.MediaType.MESSAGING};
    private final Engagement.MediaType[] mediaTypesWithoutMessaging = {
            Engagement.MediaType.TEXT,
            Engagement.MediaType.AUDIO};

    // Queues
    private final Queue audioQueue = new Queue(
            "audioQueueId",
            "Audio Queue",
            QueueState.Status.OPEN,
            mediaTypesWithoutMessaging,
            false);
    private final Queue videoQueue = new Queue(
            "videoQueueId",
            "Video Queue",
            QueueState.Status.OPEN,
            mediaTypesWithoutMessaging,
            false);

    @Before
    public void setUp() {
        isMessageCenterAvailableUseCase = new IsMessageCenterAvailableUseCase(
                "messagingQueueId",
                mock(GliaQueueRepository.class),
                mock(Schedulers.class));
    }

    @Test
    public void containsMessagingQueue_ReturnsTrue_QueueIdExistMediaTypeMessaging() {
        Queue messagingQueue = new Queue(
                "messagingQueueId",
                "Messaging Queue",
                QueueState.Status.OPEN,
                mediaTypesWithMessaging,
                false);
        Queue[] queues = {audioQueue, messagingQueue, videoQueue};
        boolean isMessageCenterAvailable = isMessageCenterAvailableUseCase.containsMessagingQueue(queues);
        assertTrue(isMessageCenterAvailable);
    }

    @Test
    public void containsMessagingQueue_ReturnsFalse_QueueIdDoesNotExistMediaTypeMessaging() {
        Queue queueMessagingIdNotFromUseCase = new Queue(
                "messagingQueueIdNotFromUseCase",
                "Messaging Queue",
                QueueState.Status.OPEN,
                mediaTypesWithMessaging,
                false);
        Queue[] queues = {audioQueue, queueMessagingIdNotFromUseCase, videoQueue};
        boolean isMessageCenterAvailable = isMessageCenterAvailableUseCase.containsMessagingQueue(queues);
        assertFalse(isMessageCenterAvailable);
    }

    @Test
    public void containsMessagingQueue_ReturnsFalse_QueueIdExistButNoMediaTypeMessaging() {
        Queue queueWithoutMessagingIdFromUseCase = new Queue(
                "messagingQueueId",
                "Messaging Queue",
                QueueState.Status.OPEN,
                mediaTypesWithoutMessaging,
                false);
        Queue[] queues = {audioQueue, queueWithoutMessagingIdFromUseCase, videoQueue};
        boolean isMessageCenterAvailable = isMessageCenterAvailableUseCase.containsMessagingQueue(queues);
        assertFalse(isMessageCenterAvailable);
    }

    @Test
    public void containsMessagingQueue_ReturnsTrue_QueueUnstaffed() {
        Queue queueWithMessagingIdFromUseCaseUnstaffed = new Queue(
                "messagingQueueId",
                "Messaging Queue",
                QueueState.Status.UNSTAFFED,
                mediaTypesWithMessaging,
                false);
        Queue[] queues = {audioQueue, queueWithMessagingIdFromUseCaseUnstaffed, videoQueue};
        boolean isMessageCenterAvailable = isMessageCenterAvailableUseCase.containsMessagingQueue(queues);
        assertTrue(isMessageCenterAvailable);
    }

    @Test
    public void containsMessagingQueue_ReturnsTrue_QueueFull() {
        Queue queueWithMessagingIdFromUseCaseFull = new Queue(
                "messagingQueueId",
                "Messaging Queue",
                QueueState.Status.FULL,
                mediaTypesWithMessaging,
                false);
        Queue[] queues = {audioQueue, queueWithMessagingIdFromUseCaseFull, videoQueue};
        boolean isMessageCenterAvailable = isMessageCenterAvailableUseCase.containsMessagingQueue(queues);
        assertTrue(isMessageCenterAvailable);
    }

    @Test
    public void containsMessagingQueue_ReturnsFalse_QueueClosed() {
        Queue queueWithMessagingIdFromUseCaseClosed = new Queue(
                "messagingQueueId",
                "Messaging Queue",
                QueueState.Status.CLOSED,
                mediaTypesWithMessaging,
                false);
        Queue[] queues = {audioQueue, queueWithMessagingIdFromUseCaseClosed, videoQueue};
        boolean isMessageCenterAvailable = isMessageCenterAvailableUseCase.containsMessagingQueue(queues);
        assertFalse(isMessageCenterAvailable);
    }

    @Test
    public void containsMessagingQueue_ReturnsFalse_QueueStateUnknown() {
        Queue queueWithMessagingIdFromUseCaseUnknown = new Queue(
                "messagingQueueId",
                "Messaging Queue",
                QueueState.Status.UNKNOWN,
                mediaTypesWithMessaging,
                false);
        Queue[] queues = {audioQueue, queueWithMessagingIdFromUseCaseUnknown, videoQueue};
        boolean isMessageCenterAvailable = isMessageCenterAvailableUseCase.containsMessagingQueue(queues);
        assertFalse(isMessageCenterAvailable);
    }

    @Test
    public void containsMessagingQueue_ReturnsFalse_QueuesEmpty() {
        Queue[] queues = {};
        boolean isMessageCenterAvailable = isMessageCenterAvailableUseCase.containsMessagingQueue(queues);
        assertFalse(isMessageCenterAvailable);
    }
}
