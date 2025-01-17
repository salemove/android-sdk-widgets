package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.core.fileupload.FileAttachmentRepository
import com.glia.widgets.core.fileupload.model.LocalAttachment
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.core.secureconversations.SendMessageRepository
import com.glia.widgets.helper.rx.Schedulers
import com.glia.widgets.messagecenter.MessageCenterState
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.reactivex.rxjava3.subjects.BehaviorSubject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit

class SendMessageButtonDialogStateUseCaseTest {
    private lateinit var sendMessageRepository: SendMessageRepository
    private lateinit var fileAttachmentRepository: FileAttachmentRepository
    private lateinit var secureConversationsRepository: SecureConversationsRepository
    private lateinit var showMessageLimitErrorUseCase: ShowMessageLimitErrorUseCase
    private lateinit var testScheduler: TestScheduler

    private lateinit var useCase: SendMessageButtonStateUseCase

    @Before
    fun setUp() {
        sendMessageRepository = mock()
        fileAttachmentRepository = mock()
        secureConversationsRepository = mock()
        showMessageLimitErrorUseCase = mock()

        testScheduler = TestScheduler()
        val schedulers = mock<Schedulers>()
        whenever(schedulers.mainScheduler) doReturn testScheduler
        whenever(schedulers.computationScheduler) doReturn testScheduler

        useCase = SendMessageButtonStateUseCase(
            sendMessageRepository,
            fileAttachmentRepository,
            secureConversationsRepository,
            showMessageLimitErrorUseCase,
            schedulers
        )

        RxJavaPlugins.reset()
    }

    @After
    fun tearDown() {
        RxJavaPlugins.reset()
    }

    @Test
    fun `invoke returns disable if the message is blank and files are empty`() {
        val message = ""
        val localAttachments = emptyList<LocalAttachment>()
        val isSending = false
        val isLimitError = false
        setInitialValues(message, localAttachments, isSending, isLimitError)

        val testObservable = useCase().test()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        assertEquals(1, testObservable.values().size)
        assertEquals(MessageCenterState.ButtonState.DISABLE, testObservable.values()[0])
    }

    @Test
    fun `invoke returns disable if the message is blank and file is not ready to send`() {
        val message = ""
        val localAttachments = listOf(
            mock<LocalAttachment>().also {
                whenever(it.isReadyToSend) doReturn false
            }
        )
        val isSending = false
        val isLimitError = false
        setInitialValues(message, localAttachments, isSending, isLimitError)

        val testObservable = useCase().test()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        assertEquals(1, testObservable.values().size)
        assertEquals(MessageCenterState.ButtonState.DISABLE, testObservable.values()[0])
    }

    @Test
    fun `invoke returns disable if the message is not blank and file is not ready to send`() {
        val message = "test"
        val localAttachments = listOf(
            mock<LocalAttachment>().also {
                whenever(it.isReadyToSend) doReturn false
            }
        )
        val isSending = false
        val isLimitError = false
        setInitialValues(message, localAttachments, isSending, isLimitError)

        val testObservable = useCase().test()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        assertEquals(1, testObservable.values().size)
        assertEquals(MessageCenterState.ButtonState.DISABLE, testObservable.values()[0])
    }

    @Test
    fun `invoke returns disable if some file is not ready to send`() {
        val message = "test"
        val localAttachments = listOf(
            mock<LocalAttachment>().also {
                whenever(it.isReadyToSend) doReturn true
            },
            mock<LocalAttachment>().also {
                whenever(it.isReadyToSend) doReturn false
            },
            mock<LocalAttachment>().also {
                whenever(it.isReadyToSend) doReturn true
            }
        )
        val isSending = false
        val isLimitError = false
        setInitialValues(message, localAttachments, isSending, isLimitError)

        val testObservable = useCase().test()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        assertEquals(1, testObservable.values().size)
        assertEquals(MessageCenterState.ButtonState.DISABLE, testObservable.values()[0])
    }

    @Test
    fun `invoke returns disable if the limit error`() {
        val message = "test"
        val localAttachments = listOf(
            mock<LocalAttachment>().also {
                whenever(it.isReadyToSend) doReturn true
            }
        )
        val isSending = false
        val isLimitError = true
        setInitialValues(message, localAttachments, isSending, isLimitError)

        val testObservable = useCase().test()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        assertEquals(1, testObservable.values().size)
        assertEquals(MessageCenterState.ButtonState.DISABLE, testObservable.values()[0])
    }

    @Test
    fun `invoke returns normal if the message is blank and file is ready to send`() {
        val message = ""
        val localAttachments = listOf(
            mock<LocalAttachment>().also {
                whenever(it.isReadyToSend) doReturn true
            }
        )
        val isSending = false
        val isLimitError = false
        setInitialValues(message, localAttachments, isSending, isLimitError)

        val testObservable = useCase().test()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        assertEquals(1, testObservable.values().size)
        assertEquals(MessageCenterState.ButtonState.NORMAL, testObservable.values()[0])
    }

    @Test
    fun `invoke returns normal if the message is blank and all files are ready to send`() {
        val message = ""
        val localAttachments = listOf(
            mock<LocalAttachment>().also {
                whenever(it.isReadyToSend) doReturn true
            },
            mock<LocalAttachment>().also {
                whenever(it.isReadyToSend) doReturn true
            },
            mock<LocalAttachment>().also {
                whenever(it.isReadyToSend) doReturn true
            }
        )
        val isSending = false
        val isLimitError = false
        setInitialValues(message, localAttachments, isSending, isLimitError)

        val testObservable = useCase().test()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        assertEquals(1, testObservable.values().size)
        assertEquals(MessageCenterState.ButtonState.NORMAL, testObservable.values()[0])
    }

    @Test
    fun `invoke returns progress if it is sending`() {
        val message = "test"
        val localAttachments = listOf(
            mock<LocalAttachment>().also {
                whenever(it.isReadyToSend) doReturn true
            }
        )
        val isSending = true
        val isLimitError = false
        setInitialValues(message, localAttachments, isSending, isLimitError)

        val testObservable = useCase().test()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        assertEquals(1, testObservable.values().size)
        assertEquals(MessageCenterState.ButtonState.PROGRESS, testObservable.values()[0])
    }

    private fun setInitialValues(
        message: String,
        localAttachments: List<LocalAttachment>,
        isSending: Boolean,
        isLimitError: Boolean
    ) {
        whenever(sendMessageRepository.observable) doReturn BehaviorSubject
            .createDefault(message)
        whenever(fileAttachmentRepository.observable) doReturn BehaviorSubject
            .createDefault(localAttachments)
        whenever(secureConversationsRepository.messageSendingObservable) doReturn BehaviorSubject
            .createDefault(isSending)
        whenever(showMessageLimitErrorUseCase.invoke()) doReturn BehaviorSubject
            .createDefault(isLimitError)
    }
}
