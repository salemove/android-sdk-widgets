package com.glia.widgets.entrywidget

import android.app.Activity
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.queuing.Queue
import com.glia.androidsdk.queuing.QueueState
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.core.queue.QueueRepository
import com.glia.widgets.core.queue.QueuesState
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.Logger
import io.reactivex.rxjava3.core.Flowable
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito.atLeast
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.never

class EntryWidgetControllerTest {

    private lateinit var queueRepository: QueueRepository
    private lateinit var isAuthenticatedUseCase: IsAuthenticatedUseCase
    private lateinit var core: GliaCore
    private lateinit var controller: EntryWidgetController
    private lateinit var view: EntryWidgetContract.View
    private lateinit var activity: Activity

    @Before
    fun setUp() {
        Logger.setIsDebug(false)
        queueRepository = mock(QueueRepository::class.java)
        isAuthenticatedUseCase = mock(IsAuthenticatedUseCase::class.java)
        core = mock(GliaCore::class.java)
        activity = mock(Activity::class.java)
        view = mock(EntryWidgetContract.View::class.java)

        controller = EntryWidgetController(queueRepository, isAuthenticatedUseCase, core)
        controller.setView(view)
    }

    @Test
    fun `showItems is called if sdk is initialized`() {
        `when`(core.isInitialized).thenReturn(true)
        `when`(queueRepository.queuesState).thenReturn(Flowable.just(QueuesState.Loading))

        controller.setView(view)

        verify(view, atLeast(1)).showItems(anyList())
    }

    @Test
    fun `showItems is called with ERROR_STATE if SDK is not initialized`() {
        `when`(core.isInitialized).thenReturn(false)

        controller.setView(view)

        verify(view, atLeast(1)).showItems(listOf(EntryWidgetContract.ItemType.SDK_NOT_INITIALIZED_STATE))
    }

    @Test
    fun `mapState returns CHAT and PROVIDED_BY if TEXT media type is available`() {
        val queue = mock(Queue::class.java)
        val queueState = mock(QueueState::class.java)
        `when`(queue.state).thenReturn(queueState)
        `when`(queueState.status).thenReturn(QueueState.Status.OPEN)
        `when`(queueState.medias).thenReturn(arrayOf(Engagement.MediaType.TEXT))

        val result = controller.mapState(QueuesState.Queues(listOf(queue)))
        assert(
            result == listOf(
                EntryWidgetContract.ItemType.CHAT,
                EntryWidgetContract.ItemType.PROVIDED_BY
            )
        )
    }

    @Test
    fun `mapState returns SECURE_MESSAGE and PROVIDED_BY if SECURE_MESSAGE media type is available and visitor is authenticates`() {
        val queue = mock(Queue::class.java)
        val queueState = mock(QueueState::class.java)
        `when`(queue.state).thenReturn(queueState)
        `when`(queueState.status).thenReturn(QueueState.Status.OPEN)
        `when`(isAuthenticatedUseCase.invoke()).thenReturn(true)
        `when`(queueState.medias).thenReturn(arrayOf(Engagement.MediaType.MESSAGING))

        val result = controller.mapState(QueuesState.Queues(listOf(queue)))
        assert(
            result == listOf(
                EntryWidgetContract.ItemType.SECURE_MESSAGE,
                EntryWidgetContract.ItemType.PROVIDED_BY
            )
        )
    }

    @Test
    fun `mapState returns EMPTY_STATE if SECURE_MESSAGE media type is available but visitor is not authenticates`() {
        val queue = mock(Queue::class.java)
        val queueState = mock(QueueState::class.java)
        `when`(queue.state).thenReturn(queueState)
        `when`(queueState.status).thenReturn(QueueState.Status.OPEN)
        `when`(isAuthenticatedUseCase.invoke()).thenReturn(false)
        `when`(queueState.medias).thenReturn(arrayOf(Engagement.MediaType.MESSAGING))

        val result = controller.mapState(QueuesState.Queues(listOf(queue)))
        assert(
            result == listOf(
                EntryWidgetContract.ItemType.EMPTY_STATE
            )
        )
    }

    @Test
    fun `mapState returns a list of LOADING_STATE if called with a Loading state`() {
        val result = controller.mapState(QueuesState.Loading)
        assert(
            result == listOf(
                EntryWidgetContract.ItemType.LOADING_STATE,
                EntryWidgetContract.ItemType.LOADING_STATE,
                EntryWidgetContract.ItemType.LOADING_STATE,
                EntryWidgetContract.ItemType.LOADING_STATE,
                EntryWidgetContract.ItemType.PROVIDED_BY
            )
        )
    }

    @Test
    fun `mapState returns EMPTY_STATE if called with Empty state`() {
        val result = controller.mapState(QueuesState.Empty)
        assert(result == listOf(EntryWidgetContract.ItemType.EMPTY_STATE))
    }

    @Test
    fun `mapState returns ERROR_STATE if called with Error state`() {
        val result = controller.mapState(QueuesState.Error(Exception("Error")))
        assert(result == listOf(EntryWidgetContract.ItemType.ERROR_STATE))
    }

    @Test
    fun `onItemClicked calls dismiss when CHAT item clicked`() {
        controller.onItemClicked(EntryWidgetContract.ItemType.CHAT, activity)
        verify(view).dismiss()
    }

    @Test
    fun `onItemClicked calls dismiss when AUDIO_CALL item clicked`() {
        controller.onItemClicked(EntryWidgetContract.ItemType.AUDIO_CALL, activity)
        verify(view).dismiss()
    }

    @Test
    fun `onItemClicked calls dismiss when VIDEO_CALL item clicked`() {
        controller.onItemClicked(EntryWidgetContract.ItemType.VIDEO_CALL, activity)
        verify(view).dismiss()
    }

    @Test
    fun `onItemClicked calls dismiss when SECURE_MESSAGE item clicked`() {
        controller.onItemClicked(EntryWidgetContract.ItemType.SECURE_MESSAGE, activity)
        verify(view).dismiss()
    }

    @Test
    fun `onItemClicked does not call dismiss when ERROR_STATE item clicked`() {
        val mockQueuesState : Flowable<QueuesState> = Flowable.just(QueuesState.Loading)
        `when`(queueRepository.queuesState).thenReturn(mockQueuesState)
        controller.onItemClicked(EntryWidgetContract.ItemType.ERROR_STATE, activity)
        verify(view, never()).dismiss()
    }
}
