package com.glia.widgets.internal.secureconversations.domain

import com.glia.widgets.internal.secureconversations.SendMessageRepository
import com.glia.widgets.helper.rx.Schedulers
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.reactivex.rxjava3.subjects.BehaviorSubject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit

class ShowMessageLimitErrorUseCaseTest {
    private lateinit var repository: SendMessageRepository
    private lateinit var testScheduler: TestScheduler

    private lateinit var useCase: ShowMessageLimitErrorUseCase

    @Before
    fun setUp() {
        repository = mock()
        testScheduler = TestScheduler()
        val schedulers = mock<Schedulers>()
        whenever(schedulers.mainScheduler) doReturn testScheduler
        whenever(schedulers.computationScheduler) doReturn testScheduler
        useCase = ShowMessageLimitErrorUseCase(repository, schedulers)
        RxJavaPlugins.reset()
    }

    @After
    fun tearDown() {
        RxJavaPlugins.reset()
    }

    @Test
    fun `invoke returns false if the message is less than the max length`() {
        val subject = BehaviorSubject.createDefault("test")
        whenever(repository.observable) doReturn subject

        val testObservable = useCase().test()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        assertEquals(1, testObservable.values().size)
        assertFalse(testObservable.values()[0])
    }

    @Test
    fun `invoke returns false if the message equals the max length`() {
        val subject = BehaviorSubject.createDefault("0".repeat(10000))
        whenever(repository.observable) doReturn subject

        val testObservable = useCase().test()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        assertEquals(1, testObservable.values().size)
        assertFalse(testObservable.values()[0])
    }

    @Test
    fun `invoke returns true if the message is more than the max length`() {
        val subject = BehaviorSubject.createDefault("0".repeat(10001))
        whenever(repository.observable) doReturn subject

        val testObservable = useCase().test()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        assertEquals(1, testObservable.values().size)
        assertTrue(testObservable.values()[0])
    }
}
