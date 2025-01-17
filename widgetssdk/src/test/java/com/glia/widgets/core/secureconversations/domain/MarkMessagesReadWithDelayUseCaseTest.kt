package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.RequestCallback
import com.glia.widgets.chat.data.ChatScreenRepository
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.processors.BehaviorProcessor
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

@RunWith(RobolectricTestRunner::class)
class MarkMessagesReadWithDelayUseCaseTest {
    private var secureConversationsRepository: SecureConversationsRepository by Delegates.notNull()
    private var chatScreenRepository: ChatScreenRepository by Delegates.notNull()
    private var useCase: MarkMessagesReadWithDelayUseCase by Delegates.notNull()

    @Before
    fun setUp() {
        secureConversationsRepository = mock()
        chatScreenRepository = mock()
        useCase = MarkMessagesReadWithDelayUseCase(
            secureConversationsRepository,
            chatScreenRepository
        )
        RxJavaPlugins.reset()
    }

    @After
    fun tearDown() = RxJavaPlugins.reset()

    @Test
    fun `invoke calls mark messages read after expected delay`() {
        val testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        doAnswer {
            val callback: RequestCallback<Void> = it.getArgument(0)
            callback.onResult(null, null)
        }.whenever(secureConversationsRepository).markMessagesRead(any())
        whenever(secureConversationsRepository.isLeaveSecureConversationDialogVisibleObservable) doReturn Flowable.just(false)
        whenever(chatScreenRepository.isChatScreenOpenObservable) doReturn Flowable.just(true)

        val testObservable = useCase().test()
        verify(secureConversationsRepository, never()).markMessagesRead(any())
        testScheduler.advanceTimeBy(DELAY_SEC, TimeUnit.SECONDS)
        verify(secureConversationsRepository).markMessagesRead(any())

        testObservable.assertComplete()
    }

    @Test
    fun `invoke calls mark messages read after leave SC dialog closed with expected delay`() {
        val testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        doAnswer {
            val callback: RequestCallback<Void> = it.getArgument(0)
            callback.onResult(null, null)
        }.whenever(secureConversationsRepository).markMessagesRead(any())
        val isLeaveSecureConversationDialogVisibleBehaviorProcessor = BehaviorProcessor.createDefault(true)
        whenever(secureConversationsRepository.isLeaveSecureConversationDialogVisibleObservable) doReturn isLeaveSecureConversationDialogVisibleBehaviorProcessor
        whenever(chatScreenRepository.isChatScreenOpenObservable) doReturn Flowable.just(true)

        val testObservable = useCase().test()
        testScheduler.advanceTimeBy(DELAY_SEC, TimeUnit.SECONDS)
        verify(secureConversationsRepository, never()).markMessagesRead(any())
        isLeaveSecureConversationDialogVisibleBehaviorProcessor.onNext(false)
        verify(secureConversationsRepository, never()).markMessagesRead(any())
        testScheduler.advanceTimeBy(DELAY_SEC, TimeUnit.SECONDS)
        verify(secureConversationsRepository).markMessagesRead(any())

        testObservable.assertComplete()
    }

    @Test
    fun `invoke calls mark messages read after resume to chat screen`() {
        val testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        doAnswer {
            val callback: RequestCallback<Void> = it.getArgument(0)
            callback.onResult(null, null)
        }.whenever(secureConversationsRepository).markMessagesRead(any())
        val isLeaveSecureConversationDialogVisibleBehaviorProcessor = BehaviorProcessor.createDefault(false)
        whenever(secureConversationsRepository.isLeaveSecureConversationDialogVisibleObservable) doReturn isLeaveSecureConversationDialogVisibleBehaviorProcessor
        val isChatScreenOpenBehaviorProcessor = BehaviorProcessor.createDefault(false)
        whenever(chatScreenRepository.isChatScreenOpenObservable) doReturn isChatScreenOpenBehaviorProcessor

        val testObservable = useCase().test()
        testScheduler.advanceTimeBy(DELAY_SEC, TimeUnit.SECONDS)
        verify(secureConversationsRepository, never()).markMessagesRead(any())
        isChatScreenOpenBehaviorProcessor.onNext(true)
        verify(secureConversationsRepository, never()).markMessagesRead(any())
        testScheduler.advanceTimeBy(DELAY_SEC, TimeUnit.SECONDS)
        verify(secureConversationsRepository).markMessagesRead(any())

        testObservable.assertComplete()
    }
}
