package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.queuing.Queue
import com.glia.androidsdk.queuing.QueueState
import com.glia.widgets.core.queue.GliaQueueRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import kotlin.properties.Delegates

class IsMessagingAvailableUseCaseTest {
    private val messagingQueueId = "messagingQueueId"
    private val mediaTypesWithMessaging =
        arrayOf(Engagement.MediaType.TEXT, Engagement.MediaType.MESSAGING)
    private val mediaTypesWithoutMessaging =
        arrayOf(Engagement.MediaType.TEXT, Engagement.MediaType.AUDIO)

    // Queues
    private val audioQueue = Queue(
        "audioQueueId",
        "Audio Queue",
        QueueState.Status.OPEN,
        mediaTypesWithoutMessaging,
        false
    )
    private val videoQueue = Queue(
        "videoQueueId",
        "Video Queue",
        QueueState.Status.OPEN,
        mediaTypesWithoutMessaging,
        false
    )

    private var isMessagingAvailableUseCase: IsMessagingAvailableUseCase by Delegates.notNull()
    private var queueRepository: GliaQueueRepository by Delegates.notNull()

    @Before
    fun setUp() {
        queueRepository = mock()
        isMessagingAvailableUseCase = IsMessagingAvailableUseCase(queueRepository, mock())
    }

    @Test
    fun containsMessagingQueue_ReturnsTrue_WhenQueueIdWithMediaTypeMessagingExists() {
        val messagingQueue = createMessagingQueueWithStatus()
        val queues = arrayOf(audioQueue, messagingQueue, videoQueue)
        val isMessageCenterAvailable: Boolean =
            isMessagingAvailableUseCase.containsMessagingQueue(arrayOf(messagingQueueId), queues)
        assertTrue(isMessageCenterAvailable)
    }

    @Test
    fun containsMessagingQueue_ReturnsFalse_WhenQueueIdWithMediaTypeMessagingDoesNotExist() {
        val queueMessagingIdNotFromUseCase = Queue(
            "messagingQueueIdNotFromUseCase",
            "Messaging Queue",
            QueueState.Status.OPEN,
            mediaTypesWithMessaging,
            false
        )
        val queues = arrayOf(audioQueue, queueMessagingIdNotFromUseCase, videoQueue)
        val isMessageCenterAvailable: Boolean =
            isMessagingAvailableUseCase.containsMessagingQueue(arrayOf(messagingQueueId), queues)
        assertFalse(isMessageCenterAvailable)
    }

    @Test
    fun containsMessagingQueue_ReturnsFalse_WhenQueueIdWithAnotherMediaTypeExists() {
        val queueWithoutMessagingIdFromUseCase = Queue(
            "messagingQueueId",
            "Messaging Queue",
            QueueState.Status.OPEN,
            mediaTypesWithoutMessaging,
            false
        )
        val queues = arrayOf(audioQueue, queueWithoutMessagingIdFromUseCase, videoQueue)
        val isMessageCenterAvailable: Boolean = isMessagingAvailableUseCase.containsMessagingQueue(
            arrayOf(messagingQueueId), queues
        )
        assertFalse(isMessageCenterAvailable)
    }

    @Test
    fun containsMessagingQueue_ReturnsTrue_WhenExistingQueueUnStaffed() {
        val queueWithMessagingIdFromUseCaseUnstaffed =
            createMessagingQueueWithStatus(QueueState.Status.UNSTAFFED)
        val queues = arrayOf(audioQueue, queueWithMessagingIdFromUseCaseUnstaffed, videoQueue)
        val isMessageCenterAvailable: Boolean =
            isMessagingAvailableUseCase.containsMessagingQueue(arrayOf(messagingQueueId), queues)
        assertTrue(isMessageCenterAvailable)
    }

    @Test
    fun containsMessagingQueue_ReturnsTrue_WhenExistingQueueFull() {
        val queueWithMessagingIdFromUseCaseFull =
            createMessagingQueueWithStatus(QueueState.Status.FULL)
        val queues = arrayOf(audioQueue, queueWithMessagingIdFromUseCaseFull, videoQueue)
        val isMessageCenterAvailable: Boolean =
            isMessagingAvailableUseCase.containsMessagingQueue(arrayOf(messagingQueueId), queues)
        assertTrue(isMessageCenterAvailable)
    }

    @Test
    fun containsMessagingQueue_ReturnsFalse_WhenExistingQueueClosed() {
        val queueWithMessagingIdFromUseCaseClosed =
            createMessagingQueueWithStatus(QueueState.Status.CLOSED)
        val queues = arrayOf(audioQueue, queueWithMessagingIdFromUseCaseClosed, videoQueue)
        val isMessageCenterAvailable: Boolean =
            isMessagingAvailableUseCase.containsMessagingQueue(arrayOf(messagingQueueId), queues)
        assertFalse(isMessageCenterAvailable)
    }

    @Test
    fun containsMessagingQueue_ReturnsFalse_WhenExistingQueueStateUnknown() {
        val queueWithMessagingIdFromUseCaseUnknown =
            createMessagingQueueWithStatus(QueueState.Status.UNKNOWN)
        val queues = arrayOf(audioQueue, queueWithMessagingIdFromUseCaseUnknown, videoQueue)
        val isMessageCenterAvailable: Boolean =
            isMessagingAvailableUseCase.containsMessagingQueue(arrayOf(messagingQueueId), queues)
        assertFalse(isMessageCenterAvailable)
    }

    @Test
    fun containsMessagingQueue_ReturnsFalse_WhenQueuesEmpty() {
        val queues = arrayOf<Queue>()
        val isMessageCenterAvailable: Boolean = isMessagingAvailableUseCase.containsMessagingQueue(arrayOf(messagingQueueId), queues)
        assertFalse(isMessageCenterAvailable)
    }

    private fun createMessagingQueueWithStatus(status: QueueState.Status = QueueState.Status.OPEN) =
        Queue(
            messagingQueueId,
            "Messaging Queue",
            status,
            mediaTypesWithMessaging,
            false
        )

}