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
import org.junit.Before
import org.junit.Test
import java.util.function.Consumer

class QueueRepositoryTest {

    private lateinit var gliaCore: GliaCore
    private lateinit var configurationManager: ConfigurationManager
    private lateinit var queueRepository: QueueRepository

    private lateinit var getQueuesCallbackSlot: MutableList<RequestCallback<List<Queue>?>>
    private lateinit var subscribeToQueueUpdatesCallbackSlot: CapturingSlot<Consumer<Queue>>

    private val configurationQueuesProcessor: PublishProcessor<List<String>> =
        PublishProcessor.create()

    @Before
    fun setUp() {
        Logger.setIsDebug(false)
        gliaCore = mockk(relaxUnitFun = true)
        configurationManager = mockk {
            every { queueIdsObservable } returns configurationQueuesProcessor.hide()
        }
        getQueuesCallbackSlot = mutableListOf()
        subscribeToQueueUpdatesCallbackSlot = slot()

        queueRepository = QueueRepositoryImpl(gliaCore, configurationManager)

        verify(exactly = 0) { configurationManager.queueIdsObservable }
        verify(exactly = 0) { gliaCore.getQueues(any()) }
        verify(exactly = 0) { gliaCore.isInitialized }
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

    private fun initialize() {
        every { gliaCore.isInitialized } returns true

        queueRepository.initialize()

        verify { gliaCore.isInitialized }
        verify { gliaCore.getQueues(capture(getQueuesCallbackSlot)) }

        verify(exactly = 0) { configurationManager.queueIdsObservable }
    }

    private fun pushSiteQueues(vararg queues: Queue) {
        getQueuesCallbackSlot.last().onResult(listOf(*queues), null)
    }

    @Test
    fun `getQueues is not called when core not initialized`() {
        every { gliaCore.isInitialized } returns false

        queueRepository.initialize()

        verify(exactly = 0) { configurationManager.queueIdsObservable }
        verify(exactly = 0) { gliaCore.getQueues(any()) }
        verify { gliaCore.isInitialized }
    }

    @Test
    fun `getQueues will be called on subscription when core initialized`() {
        every { gliaCore.isInitialized } returns true

        queueRepository.relevantQueueIds.test().assertNotComplete()
        verify { gliaCore.isInitialized }
        verify { gliaCore.getQueues(any()) }

        verify(exactly = 0) { configurationManager.queueIdsObservable }
    }

    @Test
    fun `getQueues will be called on initialization when core initialized`() {
        initialize()
    }

    @Test
    fun `fetchQueues will set Error state when fetching failed`() {
        initialize()

        val exception = GliaException("Fetching queues failed", GliaException.Cause.NETWORK_TIMEOUT)

        val subscriber = queueRepository.queuesState.test()

        verify { gliaCore.getQueues(capture(getQueuesCallbackSlot)) }
        verify(exactly = 0) { configurationManager.queueIdsObservable }
        subscriber.assertCurrentValue(QueuesState.Loading)

        getQueuesCallbackSlot.last().onResult(null, exception)
        subscriber.assertCurrentValue(QueuesState.Error(exception))
    }

    @Test
    fun `fetchQueues will set Error state when queues are null`() {
        initialize()

        val subscriber = queueRepository.queuesState.test()

        verify { gliaCore.getQueues(capture(getQueuesCallbackSlot)) }
        verify(exactly = 0) { configurationManager.queueIdsObservable }
        subscriber.assertCurrentValue(QueuesState.Loading)

        getQueuesCallbackSlot.last().onResult(null, null)
        subscriber.assertCurrentValue(Predicate { it is QueuesState.Error })
    }

    @Test
    fun `fetchQueues will subscribe to queues when response is not error`() {
        initialize()

        pushSiteQueues(createQueue("1", false))
        verify { configurationManager.queueIdsObservable }
    }

    @Test
    fun `onQueuesReceived will emit Empty state when site has no Queues`() {
        initialize()

        pushSiteQueues()
        pushCustomQueues(listOf("1", "2"))
        verify { configurationManager.queueIdsObservable }
        verify(exactly = 0) { gliaCore.subscribeToQueueStateUpdates(any(), any(), any()) }
        queueRepository.queuesState.test().assertCurrentValue(Predicate { it is QueuesState.Empty })
        queueRepository.relevantQueueIds.test().assertValue { it.isEmpty() }
    }

    @Test
    fun `onQueuesReceived will fall back to Default Queues when integrator queues are empty`() {
        val defaultQueueId = "defaultQueue"
        val defaultQueue = createQueue(defaultQueueId, true)
        val nonDefaultQueue = createQueue("nonDefaultQueue", false)
        initialize()

        pushSiteQueues(defaultQueue, nonDefaultQueue)
        pushCustomQueues()

        verify { configurationManager.queueIdsObservable }

        queueRepository.queuesState.test()
            .assertCurrentValue(QueuesState.Queues(listOf(defaultQueue)))

        queueRepository.relevantQueueIds.test().assertValue(listOf(defaultQueue.id))
        verify { gliaCore.subscribeToQueueStateUpdates(eq(listOf(defaultQueueId)), any(), any()) }
    }

    @Test
    fun `onQueuesReceived will emit Empty state when integrator queues are empty and no default queues`() {
        val nonDefaultQueue = createQueue("nonDefaultQueue", false)
        initialize()

        pushSiteQueues(nonDefaultQueue)
        pushCustomQueues()

        verify { configurationManager.queueIdsObservable }

        queueRepository.queuesState.test().assertCurrentValue(QueuesState.Empty)
        queueRepository.relevantQueueIds.test().assertValue { it.isEmpty() }
        verify(exactly = 0) { gliaCore.subscribeToQueueStateUpdates(any(), any(), any()) }
    }

    @Test
    fun `onQueuesReceived will fall back to Default Queues when no matched queues`() {
        val defaultQueueId = "defaultQueue"
        val defaultQueue = createQueue(defaultQueueId, true)
        val nonDefaultQueue = createQueue("nonDefaultQueue", false)
        initialize()

        pushSiteQueues(defaultQueue, nonDefaultQueue)
        pushCustomQueues(listOf("1"))

        verify { configurationManager.queueIdsObservable }

        queueRepository.queuesState.test()
            .assertCurrentValue(QueuesState.Queues(listOf(defaultQueue)))
        queueRepository.relevantQueueIds.test().assertValue(listOf(defaultQueue.id))
        verify { gliaCore.subscribeToQueueStateUpdates(eq(listOf(defaultQueueId)), any(), any()) }
    }

    @Test
    fun `onQueuesReceived will emit Empty state when no matched and default queues`() {
        val nonDefaultQueue = createQueue("nonDefaultQueue", false)
        initialize()

        pushSiteQueues(nonDefaultQueue)
        pushCustomQueues(listOf("1"))

        verify { configurationManager.queueIdsObservable }

        queueRepository.queuesState.test().assertCurrentValue(QueuesState.Empty)
        queueRepository.relevantQueueIds.test().assertValue { it.isEmpty() }
        verify(exactly = 0) { gliaCore.subscribeToQueueStateUpdates(any(), any(), any()) }
    }

    @Test
    fun `onQueuesReceived will emit matched Queues`() {
        val defaultQueueId = "defaultQueue"
        val defaultQueue = createQueue(defaultQueueId, true)
        val nonDefaultQueueId = "nonDefaultQueue"
        val nonDefaultQueue = createQueue(nonDefaultQueueId, false)
        initialize()

        pushSiteQueues(defaultQueue, nonDefaultQueue)
        pushCustomQueues(listOf(defaultQueue.id, nonDefaultQueue.id))

        verify { configurationManager.queueIdsObservable }

        queueRepository.queuesState.test()
            .assertCurrentValue(QueuesState.Queues(listOf(defaultQueue, nonDefaultQueue)))
        queueRepository.relevantQueueIds.test()
            .assertValue(listOf(defaultQueue.id, nonDefaultQueue.id))
        verify {
            gliaCore.subscribeToQueueStateUpdates(
                eq(
                    listOf(
                        defaultQueueId,
                        nonDefaultQueueId
                    )
                ), any(), any()
            )
        }
    }

    @Test
    fun `updateQueues updates proper queue when it receives`() {
        val defaultQueueId = "defaultQueue"
        val defaultQueue = createQueue(defaultQueueId, true)
        val nonDefaultQueueId = "nonDefaultQueue"
        val nonDefaultQueue = createQueue(nonDefaultQueueId, false)
        initialize()

        pushSiteQueues(defaultQueue, nonDefaultQueue)
        pushCustomQueues(listOf(defaultQueue.id, nonDefaultQueue.id))

        verify { configurationManager.queueIdsObservable }

        queueRepository.queuesState.test()
            .assertCurrentValue(QueuesState.Queues(listOf(defaultQueue, nonDefaultQueue)))
        queueRepository.relevantQueueIds.test()
            .assertValue(listOf(defaultQueue.id, nonDefaultQueue.id))

        verify {
            gliaCore.subscribeToQueueStateUpdates(
                eq(listOf(defaultQueueId, nonDefaultQueueId)),
                any(),
                capture(subscribeToQueueUpdatesCallbackSlot)
            )
        }

        val updatedQueueName = "updatedDefaultQueueName"
        val updatedDefaultQueue = createQueue(defaultQueue.id, true, updatedQueueName)

        subscribeToQueueUpdatesCallbackSlot.captured.accept(updatedDefaultQueue)

        verify(atMost = 1) {
            gliaCore.subscribeToQueueStateUpdates(
                eq(listOf(defaultQueueId, nonDefaultQueueId)), any(), any()
            )
        }

        queueRepository.queuesState.test()
            .assertCurrentValue(QueuesState.Queues(listOf(updatedDefaultQueue, nonDefaultQueue)))
        queueRepository.relevantQueueIds.test()
            .assertValue(listOf(defaultQueue.id, nonDefaultQueue.id))
    }


}