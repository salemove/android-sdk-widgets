package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.queuing.Queue
import com.glia.androidsdk.queuing.QueueState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
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

    @Before
    fun setUp() {
        isMessagingAvailableUseCase = IsMessagingAvailableUseCase()
    }

    @Test
    fun containsMessagingQueue_ReturnsTrue_WhenQueueWithMediaTypeMessagingExists() {
        val messagingQueue = createMessagingQueueWithStatus()
        val queues = listOf(audioQueue, messagingQueue, videoQueue)
        val isMessageCenterAvailable: Boolean = isMessagingAvailableUseCase(queues)
        assertTrue(isMessageCenterAvailable)
    }

    @Test
    fun containsMessagingQueue_ReturnsFalse_WhenQueueWithMediaTypeMessagingDoesNotExist() {
        val queues = listOf(audioQueue, videoQueue)
        val isMessageCenterAvailable: Boolean = isMessagingAvailableUseCase(queues)
        assertFalse(isMessageCenterAvailable)
    }

    @Test
    fun containsMessagingQueue_ReturnsTrue_WhenExistingQueueUnStaffed() {
        val queueWithMessagingIdFromUseCaseUnstaffed =
            createMessagingQueueWithStatus(QueueState.Status.UNSTAFFED)
        val queues = listOf(audioQueue, queueWithMessagingIdFromUseCaseUnstaffed, videoQueue)
        val isMessageCenterAvailable: Boolean = isMessagingAvailableUseCase(queues)
        assertTrue(isMessageCenterAvailable)
    }

    @Test
    fun containsMessagingQueue_ReturnsTrue_WhenExistingQueueFull() {
        val queueWithMessagingIdFromUseCaseFull =
            createMessagingQueueWithStatus(QueueState.Status.FULL)
        val queues = listOf(audioQueue, queueWithMessagingIdFromUseCaseFull, videoQueue)
        val isMessageCenterAvailable: Boolean = isMessagingAvailableUseCase(queues)
        assertTrue(isMessageCenterAvailable)
    }

    @Test
    fun containsMessagingQueue_ReturnsFalse_WhenExistingQueueClosed() {
        val queueWithMessagingIdFromUseCaseClosed =
            createMessagingQueueWithStatus(QueueState.Status.CLOSED)
        val queues = listOf(audioQueue, queueWithMessagingIdFromUseCaseClosed, videoQueue)
        val isMessageCenterAvailable: Boolean = isMessagingAvailableUseCase(queues)
        assertFalse(isMessageCenterAvailable)
    }

    @Test
    fun containsMessagingQueue_ReturnsFalse_WhenExistingQueueStateUnknown() {
        val queueWithMessagingIdFromUseCaseUnknown =
            createMessagingQueueWithStatus(QueueState.Status.UNKNOWN)
        val queues = listOf(audioQueue, queueWithMessagingIdFromUseCaseUnknown, videoQueue)
        val isMessageCenterAvailable: Boolean = isMessagingAvailableUseCase(queues)
        assertFalse(isMessageCenterAvailable)
    }

    @Test
    fun containsMessagingQueue_ReturnsFalse_WhenQueuesEmpty() {
        val queues = listOf<Queue>()
        val isMessageCenterAvailable: Boolean = isMessagingAvailableUseCase(queues)
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
