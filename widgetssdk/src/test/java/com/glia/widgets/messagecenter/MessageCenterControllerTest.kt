package com.glia.widgets.messagecenter

import com.glia.androidsdk.GliaException
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.core.secureconversations.domain.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.times
import org.mockito.kotlin.*

internal class MessageCenterControllerTest {
    private lateinit var messageCenterController: MessageCenterController
    private lateinit var sendSecureMessageUseCase: SendSecureMessageUseCase
    private lateinit var isMessageCenterAvailableUseCase: IsMessageCenterAvailableUseCase
    private lateinit var addFileAttachmentsObserverUseCase: AddSecureFileAttachmentsObserverUseCase
    private lateinit var addFileToAttachmentAndUploadUseCase: AddSecureFileToAttachmentAndUploadUseCase
    private lateinit var getFileAttachmentsUseCase: GetSecureFileAttachmentsUseCase
    private lateinit var removeFileAttachmentObserverUseCase: RemoveSecureFileAttachmentObserverUseCase
    private lateinit var removeFileAttachmentUseCase: RemoveSecureFileAttachmentUseCase
    private lateinit var setSecureEngagementUseCase: SetSecureEngagementUseCase
    private lateinit var isAuthenticatedUseCase: IsAuthenticatedUseCase
    private lateinit var viewContract: MessageCenterContract.View

    @Before
    fun setUp() {
        sendSecureMessageUseCase = mock()
        isMessageCenterAvailableUseCase = mock()
        addFileAttachmentsObserverUseCase = mock()
        addFileToAttachmentAndUploadUseCase = mock()
        getFileAttachmentsUseCase = mock()
        removeFileAttachmentObserverUseCase = mock()
        removeFileAttachmentUseCase = mock()
        setSecureEngagementUseCase = mock()
        viewContract = mock()
        isAuthenticatedUseCase = mock()
        messageCenterController =
            MessageCenterController(sendSecureMessageUseCase, isMessageCenterAvailableUseCase,
                addFileAttachmentsObserverUseCase, addFileToAttachmentAndUploadUseCase,
                getFileAttachmentsUseCase, removeFileAttachmentObserverUseCase,
                removeFileAttachmentUseCase, setSecureEngagementUseCase, isAuthenticatedUseCase)
    }

    @Test
    fun setView_ExecutesAddSecureFileAttachmentsObserverUseCase_onTrigger() {
        whenever(isAuthenticatedUseCase.execute()) doReturn true
        messageCenterController.setView(viewContract)

        verify(addFileAttachmentsObserverUseCase, times(1)).execute(any())
    }

    @Test
    fun setView_ExecutesGetFileAttachmentsUseCase_onTrigger() {
        whenever(isAuthenticatedUseCase.execute()) doReturn true
        messageCenterController.setView(viewContract)

        verify(getFileAttachmentsUseCase, times(1)).execute()
    }

    @Test
    fun setView_triggersNotAuthenticatedDialog_whenNotAuthenticated() {
        whenever(isAuthenticatedUseCase.execute()) doReturn false

        messageCenterController.setView(viewContract)

        verify(viewContract).showUnAuthenticatedDialog()
    }

    @Test
    fun setView_triggersViewInitialization_whenAuthenticated() {
        whenever(isAuthenticatedUseCase.execute()) doReturn true

        messageCenterController.setView(viewContract)

        verify(viewContract).setupViewAppearance()
    }

    @Test
    fun handleMessageSendResult_CallsShowNavigationScreen_WhenErrorNull() {
        messageCenterController.setView(viewContract)
        messageCenterController.handleSendMessageResult(null)
        verify(viewContract, times(1)).showConfirmationScreen()
    }

    @Test
    fun handleMessageSendResult_CallsNavigateToMessaging_WhenAuthError() {
        messageCenterController.setView(viewContract)
        val gliaException = GliaException("Message", GliaException.Cause.AUTHENTICATION_ERROR)
        messageCenterController.handleSendMessageResult(gliaException)
        verify(viewContract, times(1)).showMessageCenterUnavailableDialog()
    }

    @Test
    fun handleMessageSendResult_CallsNavigateToMessaging_WhenInternalError() {
        messageCenterController.setView(viewContract)
        val gliaException = GliaException("Message", GliaException.Cause.INTERNAL_ERROR)
        messageCenterController.handleSendMessageResult(gliaException)
        verify(viewContract, times(1)).showUnexpectedErrorDialog()
    }

    @Test
    fun handleMessageSendResult_CallsNavigateToMessaging_WhenOtherError() {
        messageCenterController.setView(viewContract)
        val gliaException = GliaException("Message", GliaException.Cause.INVALID_INPUT)
        messageCenterController.handleSendMessageResult(gliaException)
        verify(viewContract, times(1)).showUnexpectedErrorDialog()
    }

    @Test
    fun handleSendMessageResult_SetSecureEngagement_WhenErrorNull() {
        messageCenterController.handleSendMessageResult(null)
        verify(setSecureEngagementUseCase, times(1)).invoke(true)
    }

    @Test
    fun handleSendMessageResult_NotSetSecureEngagement_WhenError() {
        val gliaException = GliaException("Message", GliaException.Cause.INTERNAL_ERROR)
        messageCenterController.handleSendMessageResult(gliaException)
        verify(setSecureEngagementUseCase, never()).invoke(any())
    }

    @Test
    fun onAttachmentReceived_ExecutesAddSecureFileToAttachmentAndUploadUseCase_onTrigger() {
        val fileAttachment = mock<FileAttachment>()
        messageCenterController.onAttachmentReceived(fileAttachment)
        verify(addFileToAttachmentAndUploadUseCase, times(1)).execute(eq(fileAttachment), any())
    }

    @Test
    fun onRemoveAttachment() {
        val fileAttachment = mock<FileAttachment>()
        messageCenterController.onRemoveAttachment(fileAttachment)
        verify(removeFileAttachmentUseCase, times(1)).execute(eq(fileAttachment))
    }

    @Test
    fun onDestroy_ExecutesRemoveSecureFileAttachmentObserverUseCase_onTrigger() {
        messageCenterController.onDestroy()

        verify(removeFileAttachmentObserverUseCase, times(1)).execute(any())
    }
}
