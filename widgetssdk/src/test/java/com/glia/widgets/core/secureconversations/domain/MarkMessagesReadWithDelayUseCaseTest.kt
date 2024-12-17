package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.RequestCallback
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
    private var repository: SecureConversationsRepository by Delegates.notNull()
    private var useCase: MarkMessagesReadWithDelayUseCase by Delegates.notNull()

    @Before
    fun setUp() {
        repository = mock()
        useCase = MarkMessagesReadWithDelayUseCase(repository)
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
        }.whenever(repository).markMessagesRead(any())
        whenever(repository.isLeaveSecureConversationDialogVisibleObservable) doReturn Flowable.just(false)

        val testObservable = useCase().test()
        verify(repository, never()).markMessagesRead(any())
        testScheduler.advanceTimeBy(DELAY_SEC, TimeUnit.SECONDS)
        verify(repository).markMessagesRead(any())

        testObservable.assertComplete()
    }

    @Test
    fun `invoke calls mark messages read after leave SC dialog closed with expected delay`() {
        val testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        doAnswer {
            val callback: RequestCallback<Void> = it.getArgument(0)
            callback.onResult(null, null)
        }.whenever(repository).markMessagesRead(any())
        val isLeaveSecureConversationDialogVisibleBehaviorProcessor = BehaviorProcessor.createDefault(true)
        whenever(repository.isLeaveSecureConversationDialogVisibleObservable) doReturn isLeaveSecureConversationDialogVisibleBehaviorProcessor

        val testObservable = useCase().test()
        testScheduler.advanceTimeBy(DELAY_SEC, TimeUnit.SECONDS)
        verify(repository, never()).markMessagesRead(any())
        isLeaveSecureConversationDialogVisibleBehaviorProcessor.onNext(false)
        verify(repository, never()).markMessagesRead(any())
        testScheduler.advanceTimeBy(DELAY_SEC, TimeUnit.SECONDS)
        verify(repository).markMessagesRead(any())

        testObservable.assertComplete()
    }
}
