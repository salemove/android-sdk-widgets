package com.glia.widgets.entrywidget

import android.app.Activity
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.queuing.Queue
import com.glia.androidsdk.queuing.QueueState
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.core.queue.QueueRepository
import com.glia.widgets.core.queue.QueuesState
import com.glia.widgets.core.secureconversations.domain.ObserveUnreadMessagesCountUseCase
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.Logger
import com.glia.widgets.launcher.EngagementLauncher
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.After
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
    private lateinit var observeUnreadMessagesCountUseCase: ObserveUnreadMessagesCountUseCase
    private lateinit var core: GliaCore
    private lateinit var controller: EntryWidgetController
    private lateinit var view: EntryWidgetContract.View
    private lateinit var activity: Activity
    private lateinit var engagementLauncher: EngagementLauncher

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        Logger.setIsDebug(false)
        queueRepository = mock()
        isAuthenticatedUseCase = mock()
        observeUnreadMessagesCountUseCase = mock()
        core = mock()
        activity = mock()
        view = mock()
        engagementLauncher = mock()

        controller = EntryWidgetController(
            queueRepository,
            isAuthenticatedUseCase,
            observeUnreadMessagesCountUseCase,
            core,
            engagementLauncher
        )
        val unreadMessageCount = 0
        `when`(observeUnreadMessagesCountUseCase.invoke()).thenReturn(Observable.just(unreadMessageCount))
        `when`(queueRepository.queuesState).thenReturn(Flowable.just(QueuesState.Loading))
        controller.setView(view, EntryWidgetContract.ViewType.BOTTOM_SHEET)
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()
    }

    @Test
    fun `showItems is called if sdk is initialized`() {
        `when`(core.isInitialized).thenReturn(true)

        controller.setView(view, EntryWidgetContract.ViewType.BOTTOM_SHEET)

        verify(view, atLeast(1)).showItems(anyList())
    }

    @Test
    fun `showItems is called with ERROR_STATE if SDK is not initialized`() {
        `when`(core.isInitialized).thenReturn(false)

        controller.setView(view, EntryWidgetContract.ViewType.BOTTOM_SHEET)

        verify(view, atLeast(1)).showItems(listOf(EntryWidgetContract.ItemType.SdkNotInitializedState))
    }

    @Test
    fun `mapState returns CHAT and PROVIDED_BY if TEXT media type is available`() {
        val queue = mock(Queue::class.java)
        val queueState = mock(QueueState::class.java)
        `when`(queue.state).thenReturn(queueState)
        `when`(queueState.status).thenReturn(QueueState.Status.OPEN)
        `when`(queueState.medias).thenReturn(arrayOf(Engagement.MediaType.TEXT))

        val unreadMessageCount = 0
        val result = controller.mapState(QueuesState.Queues(listOf(queue)), unreadMessageCount)
        assert(
            result == listOf(
                EntryWidgetContract.ItemType.Chat,
                EntryWidgetContract.ItemType.ProvidedBy
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

        val unreadMessageCount = 0
        val result = controller.mapState(QueuesState.Queues(listOf(queue)), unreadMessageCount)
        assert(
            result == listOf(
                EntryWidgetContract.ItemType.Messaging(unreadMessageCount),
                EntryWidgetContract.ItemType.ProvidedBy
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

        val unreadMessageCount = 0
        val result = controller.mapState(QueuesState.Queues(listOf(queue)), unreadMessageCount)
        assert(
            result == listOf(
                EntryWidgetContract.ItemType.EmptyState
            )
        )
    }

    @Test
    fun `mapState returns a list of LOADING_STATE if called with a Loading state`() {
        val unreadMessageCount = 0
        val result = controller.mapState(QueuesState.Loading, unreadMessageCount)
        assert(
            result == listOf(
                EntryWidgetContract.ItemType.LoadingState,
                EntryWidgetContract.ItemType.LoadingState,
                EntryWidgetContract.ItemType.LoadingState,
                EntryWidgetContract.ItemType.LoadingState,
                EntryWidgetContract.ItemType.ProvidedBy
            )
        )
    }

    @Test
    fun `mapState returns EMPTY_STATE if called with Empty state`() {
        val unreadMessageCount = 0
        val result = controller.mapState(QueuesState.Empty, unreadMessageCount)
        assert(result == listOf(EntryWidgetContract.ItemType.EmptyState))
    }

    @Test
    fun `mapState returns ERROR_STATE if called with Error state`() {
        val unreadMessageCount = 0
        val result = controller.mapState(QueuesState.Error(Exception("Error")), unreadMessageCount)
        assert(result == listOf(EntryWidgetContract.ItemType.ErrorState))
    }

    @Test
    fun `onItemClicked calls dismiss when CHAT item clicked`() {
        controller.onItemClicked(EntryWidgetContract.ItemType.Chat, activity)
        verify(engagementLauncher).startChat(activity)
        verify(view).dismiss()
    }

    @Test
    fun `onItemClicked calls dismiss when AUDIO_CALL item clicked`() {
        controller.onItemClicked(EntryWidgetContract.ItemType.AudioCall, activity)
        verify(engagementLauncher).startAudioCall(activity)
        verify(view).dismiss()
    }

    @Test
    fun `onItemClicked calls dismiss when VIDEO_CALL item clicked`() {
        controller.onItemClicked(EntryWidgetContract.ItemType.VideoCall, activity)
        verify(engagementLauncher).startVideoCall(activity)
        verify(view).dismiss()
    }

    @Test
    fun `onItemClicked calls dismiss when SECURE_MESSAGE item clicked`() {
        val unreadMessageCount = 0
        controller.onItemClicked(EntryWidgetContract.ItemType.Messaging(unreadMessageCount), activity)
        verify(engagementLauncher).startSecureMessaging(activity)
        verify(view).dismiss()
    }

    @Test
    fun `onItemClicked does not call dismiss when ERROR_STATE item clicked`() {
        controller.onItemClicked(EntryWidgetContract.ItemType.ErrorState, activity)
        verify(view, never()).dismiss()
        verify(queueRepository).fetchQueues()
    }
}
