package com.glia.widgets.messagecenter

import com.glia.androidsdk.GliaException
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.chat.domain.SiteInfoUseCase
import com.glia.widgets.chat.domain.TakePictureUseCase
import com.glia.widgets.chat.domain.UriToFileAttachmentUseCase
import com.glia.widgets.core.dialog.DialogContract
import com.glia.widgets.core.fileupload.domain.AddFileAttachmentsObserverUseCase
import com.glia.widgets.core.fileupload.domain.GetFileAttachmentsUseCase
import com.glia.widgets.core.fileupload.domain.RemoveFileAttachmentUseCase
import com.glia.widgets.core.fileupload.model.LocalAttachment
import com.glia.widgets.core.permissions.domain.RequestNotificationPermissionIfPushNotificationsSetUpUseCase
import com.glia.widgets.core.secureconversations.domain.AddSecureFileToAttachmentAndUploadUseCase
import com.glia.widgets.core.secureconversations.domain.IsMessagingAvailableUseCase
import com.glia.widgets.core.secureconversations.domain.OnNextMessageUseCase
import com.glia.widgets.core.secureconversations.domain.ResetMessageCenterUseCase
import com.glia.widgets.core.secureconversations.domain.SendMessageButtonStateUseCase
import com.glia.widgets.core.secureconversations.domain.SendSecureMessageUseCase
import com.glia.widgets.core.secureconversations.domain.ShowMessageLimitErrorUseCase
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.whenever

internal class MessageCenterControllerTest {
    private lateinit var messageCenterController: MessageCenterController
    private lateinit var sendSecureMessageUseCase: SendSecureMessageUseCase
    private lateinit var addFileAttachmentsObserverUseCase: AddFileAttachmentsObserverUseCase
    private lateinit var addFileToAttachmentAndUploadUseCase: AddSecureFileToAttachmentAndUploadUseCase
    private lateinit var getFileAttachmentsUseCase: GetFileAttachmentsUseCase
    private lateinit var removeFileAttachmentUseCase: RemoveFileAttachmentUseCase
    private lateinit var siteInfoUseCase: SiteInfoUseCase
    private lateinit var isAuthenticatedUseCase: IsAuthenticatedUseCase
    private lateinit var viewContract: MessageCenterContract.View
    private lateinit var onNextMessageUseCase: OnNextMessageUseCase
    private lateinit var sendMessageButtonStateUseCase: SendMessageButtonStateUseCase
    private lateinit var showMessageLimitErrorUseCase: ShowMessageLimitErrorUseCase
    private lateinit var resetMessageCenterUseCase: ResetMessageCenterUseCase
    private lateinit var dialogController: DialogContract.Controller
    private lateinit var takePictureUseCase: TakePictureUseCase
    private lateinit var uriToFileAttachmentUseCase: UriToFileAttachmentUseCase
    private lateinit var requestNotificationPermissionIfPushNotificationsSetUpUseCase: RequestNotificationPermissionIfPushNotificationsSetUpUseCase
    private lateinit var isMessagingAvailableUseCase: IsMessagingAvailableUseCase

    @Before
    fun setUp() {
        sendSecureMessageUseCase = mock()
        addFileAttachmentsObserverUseCase = mock()
        addFileToAttachmentAndUploadUseCase = mock()
        getFileAttachmentsUseCase = mock()
        removeFileAttachmentUseCase = mock()
        siteInfoUseCase = mock()
        viewContract = mock()
        isAuthenticatedUseCase = mock()
        onNextMessageUseCase = mock()
        sendMessageButtonStateUseCase = mock()
        showMessageLimitErrorUseCase = mock()
        resetMessageCenterUseCase = mock()
        dialogController = mock()
        takePictureUseCase = mock()
        uriToFileAttachmentUseCase = mock()
        requestNotificationPermissionIfPushNotificationsSetUpUseCase = mock()
        isMessagingAvailableUseCase = mock()
        messageCenterController = MessageCenterController(
            sendSecureMessageUseCase = sendSecureMessageUseCase,
            addFileAttachmentsObserverUseCase = addFileAttachmentsObserverUseCase,
            addFileToAttachmentAndUploadUseCase = addFileToAttachmentAndUploadUseCase,
            getFileAttachmentsUseCase = getFileAttachmentsUseCase,
            removeFileAttachmentUseCase = removeFileAttachmentUseCase,
            isAuthenticatedUseCase = isAuthenticatedUseCase,
            siteInfoUseCase = siteInfoUseCase,
            onNextMessageUseCase = onNextMessageUseCase,
            sendMessageButtonStateUseCase = sendMessageButtonStateUseCase,
            showMessageLimitErrorUseCase = showMessageLimitErrorUseCase,
            resetMessageCenterUseCase = resetMessageCenterUseCase,
            dialogController = dialogController,
            takePictureUseCase = takePictureUseCase,
            uriToFileAttachmentUseCase = uriToFileAttachmentUseCase,
            requestNotificationPermissionIfPushNotificationsSetUpUseCase = requestNotificationPermissionIfPushNotificationsSetUpUseCase,
            isMessagingAvailableUseCase = isMessagingAvailableUseCase
        )
    }

    @Test
    fun initialize_ExecutesAddSecureFileAttachmentsObserverUseCase_onTrigger() {
        whenever(isAuthenticatedUseCase()) doReturn true
        whenever(addFileAttachmentsObserverUseCase.invoke()) doReturn Observable.empty()
        whenever(showMessageLimitErrorUseCase.invoke()) doReturn Observable.empty()
        whenever(sendMessageButtonStateUseCase.invoke()) doReturn Observable.empty()
        messageCenterController.setView(viewContract)

        messageCenterController.initialize()

        verify(addFileAttachmentsObserverUseCase, times(1)).invoke()
    }

    @Test
    fun initialize_ExecutesShowMessageLimitErrorUseCase_onTrigger() {
        whenever(isAuthenticatedUseCase()) doReturn true
        whenever(addFileAttachmentsObserverUseCase.invoke()) doReturn Observable.empty()
        whenever(showMessageLimitErrorUseCase.invoke()) doReturn Observable.empty()
        whenever(sendMessageButtonStateUseCase.invoke()) doReturn Observable.empty()
        messageCenterController.setView(viewContract)

        messageCenterController.initialize()

        verify(showMessageLimitErrorUseCase, times(1)).invoke()
    }

    @Test
    fun initialize_ExecutesSendMessageButtonStateUseCase_onTrigger() {
        whenever(isAuthenticatedUseCase()) doReturn true
        whenever(addFileAttachmentsObserverUseCase.invoke()) doReturn Observable.empty()
        whenever(showMessageLimitErrorUseCase.invoke()) doReturn Observable.empty()
        whenever(sendMessageButtonStateUseCase.invoke()) doReturn Observable.empty()
        messageCenterController.setView(viewContract)

        messageCenterController.initialize()

        verify(sendMessageButtonStateUseCase, times(1)).invoke()
    }

    @Test
    fun initialize_ExecutesGetFileAttachmentsUseCase_onTrigger() {
        whenever(isAuthenticatedUseCase()) doReturn true
        whenever(addFileAttachmentsObserverUseCase.invoke()) doReturn Observable.empty()
        whenever(showMessageLimitErrorUseCase.invoke()) doReturn Observable.empty()
        whenever(sendMessageButtonStateUseCase.invoke()) doReturn Observable.empty()
        messageCenterController.setView(viewContract)

        messageCenterController.initialize()

        verify(getFileAttachmentsUseCase, times(1)).invoke()
    }

    @Test
    fun initialize_triggersNotAuthenticatedDialog_whenNotAuthenticated() {
        whenever(isAuthenticatedUseCase()) doReturn false
        messageCenterController.setView(viewContract)

        messageCenterController.initialize()

        verify(dialogController).showUnauthenticatedDialog()
    }

    @Test
    fun initialize_triggersViewInitialization_whenAuthenticated() {
        whenever(isAuthenticatedUseCase()) doReturn true
        whenever(addFileAttachmentsObserverUseCase.invoke()) doReturn Observable.empty()
        whenever(showMessageLimitErrorUseCase.invoke()) doReturn Observable.empty()
        whenever(sendMessageButtonStateUseCase.invoke()) doReturn Observable.empty()
        messageCenterController.setView(viewContract)

        messageCenterController.initialize()

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
        verify(dialogController).showMessageCenterUnavailableDialog()
    }

    @Test
    fun handleMessageSendResult_CallsNavigateToMessaging_WhenInternalError() {
        messageCenterController.setView(viewContract)
        val gliaException = GliaException("Message", GliaException.Cause.INTERNAL_ERROR)
        messageCenterController.handleSendMessageResult(gliaException)
        verify(dialogController).showUnexpectedErrorDialog()
    }

    @Test
    fun handleMessageSendResult_CallsNavigateToMessaging_WhenOtherError() {
        messageCenterController.setView(viewContract)
        val gliaException = GliaException("Message", GliaException.Cause.INVALID_INPUT)
        messageCenterController.handleSendMessageResult(gliaException)
        verify(dialogController).showUnexpectedErrorDialog()
    }

    @Test
    fun onAttachmentReceived_ExecutesAddSecureFileToAttachmentAndUploadUseCase_onTrigger() {
        val localAttachment = mock<LocalAttachment>()
        messageCenterController.onAttachmentReceived(localAttachment)
        verify(addFileToAttachmentAndUploadUseCase, times(1)).invoke(eq(localAttachment), any())
    }

    @Test
    fun onRemoveAttachment() {
        val localAttachment = mock<LocalAttachment>()
        messageCenterController.onRemoveAttachment(localAttachment)
        verify(removeFileAttachmentUseCase, times(1)).invoke(eq(localAttachment))
    }

    @Test
    fun onCheckMessagesClicked_ExecutesResetMessageCenterUseCase_onTrigger() {
        messageCenterController.onCheckMessagesClicked()

        verify(resetMessageCenterUseCase, times(1)).invoke()
    }

    @Test
    fun onCloseButtonClicked_ExecutesResetMessageCenterUseCase_onTrigger() {
        messageCenterController.onCloseButtonClicked()

        verify(resetMessageCenterUseCase, times(1)).invoke()
    }

    @Test
    fun onSystemBack_ExecutesResetMessageCenterUseCase_onTrigger() {
        messageCenterController.onSystemBack()

        verify(resetMessageCenterUseCase, times(1)).invoke()
    }

    @Test
    fun ensureMessageCenterAvailability_showsUnexpectedErrorDialog_onException() {
        messageCenterController.setView(viewContract)
        whenever(isMessagingAvailableUseCase.invoke()) doReturn Flowable.just(
            Result.failure(
                GliaException(
                    "Error",
                    GliaException.Cause.INTERNAL_ERROR
                )
            )
        )
        messageCenterController.ensureMessageCenterAvailability()

        val messageCenterStateArgumentCaptor = argumentCaptor<MessageCenterState>()
        verify(viewContract, times(1)).onStateUpdated(messageCenterStateArgumentCaptor.capture())

        messageCenterStateArgumentCaptor.firstValue.apply {
            assert(!showSendMessageGroup)
        }

        verify(dialogController, never()).showMessageCenterUnavailableDialog()
        verify(dialogController, times(1)).showUnexpectedErrorDialog()
    }

    @Test
    fun ensureMessageCenterAvailability_showsMessageCenterUnavailableDialog_whenMessagingIsUnavailable() {
        messageCenterController.setView(viewContract)
        whenever(isMessagingAvailableUseCase.invoke()) doReturn Flowable.just(Result.success(false))
        messageCenterController.ensureMessageCenterAvailability()

        val messageCenterStateArgumentCaptor = argumentCaptor<MessageCenterState>()
        verify(viewContract, times(1)).onStateUpdated(messageCenterStateArgumentCaptor.capture())

        messageCenterStateArgumentCaptor.firstValue.apply {
            assert(!showSendMessageGroup)
        }

        verify(dialogController, times(1)).showMessageCenterUnavailableDialog()
        verify(dialogController, never()).showUnexpectedErrorDialog()
    }

    @Test
    fun ensureMessageCenterAvailability_showSendMessageGroup_whenMessageCenterAvailable() {
        messageCenterController.setView(viewContract)
        whenever(isMessagingAvailableUseCase.invoke()) doReturn Flowable.just(Result.success(true))

        messageCenterController.ensureMessageCenterAvailability()

        val messageCenterStateArgumentCaptor = argumentCaptor<MessageCenterState>()
        verify(viewContract, times(1)).onStateUpdated(messageCenterStateArgumentCaptor.capture())

        messageCenterStateArgumentCaptor.firstValue.apply {
            assert(showSendMessageGroup)
        }

        verify(dialogController, never()).showMessageCenterUnavailableDialog()
        verify(dialogController, never()).showUnexpectedErrorDialog()
    }
}
