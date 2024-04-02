package com.glia.widgets.messagecenter

import com.glia.androidsdk.GliaException
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.chat.domain.SiteInfoUseCase
import com.glia.widgets.chat.domain.TakePictureUseCase
import com.glia.widgets.chat.domain.UriToFileAttachmentUseCase
import com.glia.widgets.core.configuration.GliaSdkConfiguration
import com.glia.widgets.core.dialog.DialogContract
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.core.permissions.domain.RequestNotificationPermissionIfPushNotificationsSetUpUseCase
import com.glia.widgets.core.secureconversations.domain.AddSecureFileAttachmentsObserverUseCase
import com.glia.widgets.core.secureconversations.domain.AddSecureFileToAttachmentAndUploadUseCase
import com.glia.widgets.core.secureconversations.domain.GetSecureFileAttachmentsUseCase
import com.glia.widgets.core.secureconversations.domain.IsMessageCenterAvailableUseCase
import com.glia.widgets.core.secureconversations.domain.OnNextMessageUseCase
import com.glia.widgets.core.secureconversations.domain.RemoveSecureFileAttachmentUseCase
import com.glia.widgets.core.secureconversations.domain.ResetMessageCenterUseCase
import com.glia.widgets.core.secureconversations.domain.SendMessageButtonStateUseCase
import com.glia.widgets.core.secureconversations.domain.SendSecureMessageUseCase
import com.glia.widgets.core.secureconversations.domain.ShowMessageLimitErrorUseCase
import com.glia.widgets.view.head.controller.ServiceChatHeadController
import io.reactivex.rxjava3.core.Observable
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class MessageCenterControllerTest {
    private lateinit var serviceChatHeadController: ServiceChatHeadController
    private lateinit var messageCenterController: MessageCenterController
    private lateinit var sendSecureMessageUseCase: SendSecureMessageUseCase
    private lateinit var isMessageCenterAvailableUseCase: IsMessageCenterAvailableUseCase
    private lateinit var addFileAttachmentsObserverUseCase: AddSecureFileAttachmentsObserverUseCase
    private lateinit var addFileToAttachmentAndUploadUseCase: AddSecureFileToAttachmentAndUploadUseCase
    private lateinit var getFileAttachmentsUseCase: GetSecureFileAttachmentsUseCase
    private lateinit var removeFileAttachmentUseCase: RemoveSecureFileAttachmentUseCase
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

    @Before
    fun setUp() {
        serviceChatHeadController = mock()
        sendSecureMessageUseCase = mock()
        isMessageCenterAvailableUseCase = mock()
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
        messageCenterController = MessageCenterController(
            serviceChatHeadController = serviceChatHeadController,
            sendSecureMessageUseCase = sendSecureMessageUseCase,
            isMessageCenterAvailableUseCase = isMessageCenterAvailableUseCase,
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
            requestNotificationPermissionIfPushNotificationsSetUpUseCase = requestNotificationPermissionIfPushNotificationsSetUpUseCase
        )
    }

    @Test
    fun setConfiguration_setBuildTimeTheme_onTrigger() {
        val uiTheme = mock<UiTheme>()
        messageCenterController.setConfiguration(uiTheme, mock())

        verify(serviceChatHeadController, times(1)).setBuildTimeTheme(uiTheme)
    }

    @Test
    fun setConfiguration_setSdkConfiguration_onTrigger() {
        val configuration = mock<GliaSdkConfiguration>()
        messageCenterController.setConfiguration(mock(), configuration)

        verify(serviceChatHeadController, times(1)).setSdkConfiguration(configuration)
    }

    @Test
    fun setView_ExecutesAddSecureFileAttachmentsObserverUseCase_onTrigger() {
        whenever(isAuthenticatedUseCase()) doReturn true
        whenever(addFileAttachmentsObserverUseCase.invoke()) doReturn Observable.empty()
        whenever(showMessageLimitErrorUseCase.invoke()) doReturn Observable.empty()
        whenever(sendMessageButtonStateUseCase.invoke()) doReturn Observable.empty()

        messageCenterController.setView(viewContract)

        verify(addFileAttachmentsObserverUseCase, times(1)).invoke()
    }

    @Test
    fun setView_ExecutesShowMessageLimitErrorUseCase_onTrigger() {
        whenever(isAuthenticatedUseCase()) doReturn true
        whenever(addFileAttachmentsObserverUseCase.invoke()) doReturn Observable.empty()
        whenever(showMessageLimitErrorUseCase.invoke()) doReturn Observable.empty()
        whenever(sendMessageButtonStateUseCase.invoke()) doReturn Observable.empty()

        messageCenterController.setView(viewContract)

        verify(showMessageLimitErrorUseCase, times(1)).invoke()
    }

    @Test
    fun setView_ExecutesSendMessageButtonStateUseCase_onTrigger() {
        whenever(isAuthenticatedUseCase()) doReturn true
        whenever(addFileAttachmentsObserverUseCase.invoke()) doReturn Observable.empty()
        whenever(showMessageLimitErrorUseCase.invoke()) doReturn Observable.empty()
        whenever(sendMessageButtonStateUseCase.invoke()) doReturn Observable.empty()

        messageCenterController.setView(viewContract)

        verify(sendMessageButtonStateUseCase, times(1)).invoke()
    }

    @Test
    fun setView_ExecutesGetFileAttachmentsUseCase_onTrigger() {
        whenever(isAuthenticatedUseCase()) doReturn true
        whenever(addFileAttachmentsObserverUseCase.invoke()) doReturn Observable.empty()
        whenever(showMessageLimitErrorUseCase.invoke()) doReturn Observable.empty()
        whenever(sendMessageButtonStateUseCase.invoke()) doReturn Observable.empty()

        messageCenterController.setView(viewContract)

        verify(getFileAttachmentsUseCase, times(1)).invoke()
    }

    @Test
    fun setView_triggersNotAuthenticatedDialog_whenNotAuthenticated() {
        whenever(isAuthenticatedUseCase()) doReturn false

        messageCenterController.setView(viewContract)

        verify(dialogController).showUnauthenticatedDialog()
    }

    @Test
    fun setView_triggersViewInitialization_whenAuthenticated() {
        whenever(isAuthenticatedUseCase()) doReturn true
        whenever(addFileAttachmentsObserverUseCase.invoke()) doReturn Observable.empty()
        whenever(showMessageLimitErrorUseCase.invoke()) doReturn Observable.empty()
        whenever(sendMessageButtonStateUseCase.invoke()) doReturn Observable.empty()

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
    fun onDestroy_ExecutesIsMessageCenterAvailableUseCase_onTrigger() {
        messageCenterController.onDestroy()

        verify(isMessageCenterAvailableUseCase, times(1)).dispose()
    }
}
