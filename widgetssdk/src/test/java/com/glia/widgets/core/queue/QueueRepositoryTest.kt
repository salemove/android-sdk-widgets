package com.glia.widgets.core.queue

import android.assertCurrentValue
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.queuing.Queue
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.Logger
import com.glia.widgets.launcher.ConfigurationManager
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.reactivex.rxjava3.functions.Predicate
import io.reactivex.rxjava3.processors.PublishProcessor
import io.reactivex.rxjava3.subscribers.TestSubscriber
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.function.Consumer

class QueueRepositoryTest {

    private lateinit var gliaCore: GliaCore
    private lateinit var configurationManager: ConfigurationManager
    private lateinit var queueRepository: QueueRepository

    private lateinit var getQueuesCallbackSlot: CapturingSlot<RequestCallback<Array<Queue>?>>
    private lateinit var subscribeToQueueUpdatesCallbackSlot: CapturingSlot<Consumer<Queue>>

    private lateinit var testSubscriber: TestSubscriber<QueuesState>

    private val configurationQueuesProcessor: PublishProcessor<List<String>> = PublishProcessor.create()

    @Before
    fun setUp() {
        Logger.setIsDebug(false)
        gliaCore = mockk(relaxUnitFun = true)
        configurationManager = mockk {
            every { queueIdsObservable } returns configurationQueuesProcessor.hide()
        }
        getQueuesCallbackSlot = slot()
        subscribeToQueueUpdatesCallbackSlot = slot()

        queueRepository = QueueRepositoryImpl(gliaCore, configurationManager)
        verify { configurationManager.queueIdsObservable }

        testSubscriber = queueRepository.queuesState.test()

        //Verify fetchQueues is called on subscription
        testSubscriber.assertValue(QueuesState.Loading)
        assertTrue(queueRepository.relevantQueueIds.isEmpty())
        verify { gliaCore.getQueues(capture(getQueuesCallbackSlot)) }
    }

    private fun pushCustomQueues(queues: List<String> = emptyList()) {
        configurationQueuesProcessor.onNext(queues)
    }

    private fun createQueue(id: String, isDefault: Boolean, name: String = "defaultName"): Queue {
        val queue = mockk<Queue>()
        every { queue.id } returns id
        every { queue.isDefault } returns isDefault
        every { queue.name } returns name
        return queue
    }

    @Test
    fun `fetchQueues will set Error state when fetching failed`() {
        val exception = GliaException("Fetching queues failed", GliaException.Cause.NETWORK_TIMEOUT)
        getQueuesCallbackSlot.captured.onResult(null, exception)

        pushCustomQueues()

        testSubscriber.assertCurrentValue(QueuesState.Error(exception))
        assertTrue(queueRepository.relevantQueueIds.isEmpty())
    }

    @Test
    fun `fetchQueues will set Error state when queues are null`() {
        getQueuesCallbackSlot.captured.onResult(null, null)

        pushCustomQueues()

        testSubscriber.assertCurrentValue(Predicate { it is QueuesState.Error })
        assertTrue(queueRepository.relevantQueueIds.isEmpty())
    }

    @Test
    fun `onQueuesReceived will emit Empty state when site has no Queues`() {
        getQueuesCallbackSlot.captured.onResult(emptyArray(), null)

        pushCustomQueues(listOf("1", "2"))

        testSubscriber.assertCurrentValue(Predicate { it is QueuesState.Empty })
        assertTrue(queueRepository.relevantQueueIds.isEmpty())
    }

    @Test
    fun `onQueuesReceived will fall back to Default Queues when integrator queues are empty`() {
        val defaultQueueId = "defaultQueueId"
        val defaultQueue = createQueue(defaultQueueId, true)
        getQueuesCallbackSlot.captured.onResult(arrayOf(createQueue("3", false), defaultQueue), null)

        pushCustomQueues()

        testSubscriber.assertCurrentValue(QueuesState.Queues(listOf(defaultQueue)))
        assertEquals(1, queueRepository.relevantQueueIds.count())
    }

    @Test
    fun `onQueuesReceived will emit Empty state when integrator queues are empty and no default queues`() {
        getQueuesCallbackSlot.captured.onResult(arrayOf(createQueue("3", false)), null)

        pushCustomQueues()

        testSubscriber.assertCurrentValue(Predicate { it is QueuesState.Empty })
        assertTrue(queueRepository.relevantQueueIds.isEmpty())
    }

    @Test
    fun `onQueuesReceived will fall back to Default Queues when no matched queues`() {
        val defaultQueueId = "defaultQueueId"
        val defaultQueue = createQueue(defaultQueueId, true)
        getQueuesCallbackSlot.captured.onResult(arrayOf(createQueue("3", false), defaultQueue), null)

        pushCustomQueues(listOf("1"))

        testSubscriber.assertCurrentValue(QueuesState.Queues(listOf(defaultQueue)))
        assertEquals(1, queueRepository.relevantQueueIds.count())
    }

    @Test
    fun `onQueuesReceived will emit Empty state when no matched and default queues`() {
        getQueuesCallbackSlot.captured.onResult(arrayOf(createQueue("3", false)), null)

        pushCustomQueues(listOf("1"))

        testSubscriber.assertCurrentValue(Predicate { it is QueuesState.Empty })
        assertTrue(queueRepository.relevantQueueIds.isEmpty())
    }

    @Test
    fun `onQueuesReceived will emit matched Queues`() {
        val defaultQueueId = "defaultQueueId"
        val defaultQueue = createQueue(defaultQueueId, true)
        val matchedQueue = createQueue("3", false)
        getQueuesCallbackSlot.captured.onResult(arrayOf(matchedQueue, defaultQueue), null)

        pushCustomQueues(listOf(matchedQueue.id))

        testSubscriber.assertCurrentValue(QueuesState.Queues(listOf(matchedQueue)))
        assertEquals(1, queueRepository.relevantQueueIds.count())
    }

    @Test
    fun `updateQueues updates proper queue when it receives`() {
        val customQueueId = "matchedQueueId"
        val defaultQueue = createQueue("defaultQueue", true)
        val customQueue = createQueue(customQueueId, false)

        getQueuesCallbackSlot.captured.onResult(arrayOf(customQueue, defaultQueue), null)

        pushCustomQueues(listOf(customQueueId))

        verify { gliaCore.subscribeToQueueStateUpdates(eq(arrayOf(customQueueId)), any(), capture(subscribeToQueueUpdatesCallbackSlot)) }

        testSubscriber.assertCurrentValue(QueuesState.Queues(listOf(customQueue)))
        assertEquals(1, queueRepository.relevantQueueIds.count())

        val updatedQueue = createQueue(customQueueId, false, "updatedName")
        subscribeToQueueUpdatesCallbackSlot.captured.accept(updatedQueue)

        testSubscriber.assertCurrentValue(QueuesState.Queues(listOf(updatedQueue)))
        testSubscriber.assertCurrentValue(Predicate { (it as QueuesState.Queues).queues.first().name == "updatedName" })
        assertEquals(1, queueRepository.relevantQueueIds.count())
    }


}
