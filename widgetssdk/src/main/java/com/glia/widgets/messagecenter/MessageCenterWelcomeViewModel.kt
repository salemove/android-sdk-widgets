package com.glia.widgets.messagecenter

import android.net.Uri
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.androidsdk.engagement.EngagementFile
import com.glia.telemetry_lib.ButtonNames
import com.glia.telemetry_lib.GliaLogger
import com.glia.widgets.Constants
import com.glia.widgets.base.BaseViewModel
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.chat.domain.TakePictureUseCase
import com.glia.widgets.chat.domain.UriToFileAttachmentUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.logScWelcomeScreenButtonClicked
import com.glia.widgets.internal.fileupload.domain.AddFileAttachmentsObserverUseCase
import com.glia.widgets.internal.fileupload.domain.AddFileToAttachmentAndUploadUseCase
import com.glia.widgets.internal.fileupload.domain.GetFileAttachmentsUseCase
import com.glia.widgets.internal.fileupload.domain.RemoveFileAttachmentUseCase
import com.glia.widgets.internal.fileupload.domain.SupportedUploadFileTypesUseCase
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.glia.widgets.internal.permissions.domain.RequestNotificationPermissionIfPushNotificationsSetUpUseCase
import com.glia.widgets.internal.secureconversations.domain.AddSecureFileToAttachmentAndUploadUseCase
import com.glia.widgets.internal.secureconversations.domain.IsMessagingAvailableUseCase
import com.glia.widgets.internal.secureconversations.domain.OnNextMessageUseCase
import com.glia.widgets.internal.secureconversations.domain.ResetMessageCenterUseCase
import com.glia.widgets.internal.secureconversations.domain.SendMessageButtonStateUseCase
import com.glia.widgets.internal.secureconversations.domain.SendSecureMessageUseCase
import com.glia.widgets.internal.secureconversations.domain.ShowMessageLimitErrorUseCase
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.rx3.asFlow

internal class MessageCenterWelcomeViewModel(
    private val sendSecureMessageUseCase: SendSecureMessageUseCase,
    private val addFileAttachmentsObserverUseCase: AddFileAttachmentsObserverUseCase,
    private val addFileToAttachmentAndUploadUseCase: AddSecureFileToAttachmentAndUploadUseCase,
    private val getFileAttachmentsUseCase: GetFileAttachmentsUseCase,
    private val removeFileAttachmentUseCase: RemoveFileAttachmentUseCase,
    private val isAuthenticatedUseCase: IsAuthenticatedUseCase,
    private val supportedUploadFileTypesUseCase: SupportedUploadFileTypesUseCase,
    private val onNextMessageUseCase: OnNextMessageUseCase,
    private val sendMessageButtonStateUseCase: SendMessageButtonStateUseCase,
    private val showMessageLimitErrorUseCase: ShowMessageLimitErrorUseCase,
    private val resetMessageCenterUseCase: ResetMessageCenterUseCase,
    private val takePictureUseCase: TakePictureUseCase,
    private val uriToFileAttachmentUseCase: UriToFileAttachmentUseCase,
    private val requestNotificationPermissionIfPushNotificationsSetUpUseCase: RequestNotificationPermissionIfPushNotificationsSetUpUseCase,
    private val isMessagingAvailableUseCase: IsMessagingAvailableUseCase,
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase
) : BaseViewModel<MessageCenterWelcomeUiState, MessageCenterWelcomeIntent, MessageCenterWelcomeEffect>(
    MessageCenterWelcomeUiState()
) {
    companion object {
        private const val TAG: String = "MessageCenterWelcomeVM"
    }

    private var allowedFileTypes: List<String> = listOf(Constants.MIME_TYPE_ALL)
    private var allowedMediaTypes: List<String> = listOf(Constants.MIME_TYPE_IMAGES)

    // Convert RxJava streams to StateFlow
    private val attachmentsFlow: StateFlow<List<LocalAttachment>> = addFileAttachmentsObserverUseCase()
        .asFlow()
        .catch { e ->
            Logger.e(TAG, "Error in attachmentsFlow", e)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val showMessageLimitErrorFlow: StateFlow<Boolean> = showMessageLimitErrorUseCase()
        .asFlow()
        .catch { e ->
            Logger.e(TAG, "Error in showMessageLimitErrorFlow", e)
            emit(false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    private val sendMessageButtonStateFlow: StateFlow<MessageCenterState.ButtonState> =
        sendMessageButtonStateUseCase()
            .asFlow()
            .catch { e ->
                Logger.e(TAG, "Error in sendMessageButtonStateFlow", e)
                emit(MessageCenterState.ButtonState.DISABLE)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = MessageCenterState.ButtonState.DISABLE
            )

    private val isMessagingAvailableFlow: StateFlow<Boolean> = isMessagingAvailableUseCase()
        .toObservable()
        .asFlow()
        .catch { e ->
            Logger.e(TAG, "Error in isMessagingAvailableFlow", e)
            emit(false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    private val allowedFileTypesState: MutableStateFlow<AllowedFileTypesState> =
        MutableStateFlow(AllowedFileTypesState())

    init {
        // Combine flows to update state
        viewModelScope.launch {
            combine(
                attachmentsFlow,
                showMessageLimitErrorFlow,
                sendMessageButtonStateFlow,
                isMessagingAvailableFlow,
                allowedFileTypesState
            ) { attachments, showMessageLimitError, sendButtonState, isMessagingAvailable, allowedTypes ->
                currentState.copy(
                    attachments = attachments,
                    showMessageLimitError = showMessageLimitError,
                    sendMessageButtonState = mapButtonState(sendButtonState),
                    showSendMessageGroup = isMessagingAvailable,
                    addAttachmentButtonVisible = allowedTypes.isSendFilesAllowed,
                    isLibraryAttachmentVisible = allowedTypes.isLibraryAttachmentAllowed,
                    isTakePhotoAttachmentVisible = allowedTypes.isTakePhotoAttachmentAllowed,
                    isBrowseAttachmentVisible = allowedTypes.isBrowseAttachmentAllowed
                )
            }.collect { newState ->
                updateState { newState }
            }
        }
    }

    override suspend fun handleIntent(intent: MessageCenterWelcomeIntent) {
        when (intent) {
            MessageCenterWelcomeIntent.Initialize -> handleInitialize()
            is MessageCenterWelcomeIntent.MessageTextChanged -> handleMessageTextChanged(intent.message)
            MessageCenterWelcomeIntent.SendMessageClicked -> handleSendMessage()
            MessageCenterWelcomeIntent.CheckMessagesClicked -> handleCheckMessages()
            MessageCenterWelcomeIntent.CloseClicked -> handleClose()
            MessageCenterWelcomeIntent.AddAttachmentClicked -> handleAddAttachment()
            MessageCenterWelcomeIntent.GalleryClicked -> handleGallery()
            MessageCenterWelcomeIntent.BrowseClicked -> handleBrowse()
            MessageCenterWelcomeIntent.TakePhotoClicked -> handleTakePhoto()
            is MessageCenterWelcomeIntent.ImageCaptured -> handleImageCaptured(intent.captured)
            is MessageCenterWelcomeIntent.ContentChosen -> handleContentChosen(intent.uri)
            is MessageCenterWelcomeIntent.RemoveAttachment -> handleRemoveAttachment(intent.attachment)
            MessageCenterWelcomeIntent.SystemBack -> handleSystemBack()
        }
    }

    private fun handleInitialize() {
        if (!isAuthenticatedUseCase()) {
            sendEffect(MessageCenterWelcomeEffect.ShowUnauthenticatedDialog)
            Logger.i(TAG, "Secure Messaging is unavailable because the visitor is not authenticated.")
            return
        }

        // Update allowed file types
        supportedUploadFileTypesUseCase { result ->
            allowedFileTypes = result.allowedFileTypes
            allowedMediaTypes = result.allowedMediaTypes
            allowedFileTypesState.value = AllowedFileTypesState(
                isSendFilesAllowed = result.isSendFilesAllowed,
                isLibraryAttachmentAllowed = result.isLibraryAttachmentAllowed,
                isTakePhotoAttachmentAllowed = result.isTakePhotoAttachmentAllowed,
                isBrowseAttachmentAllowed = result.isBrowseAttachmentAllowed
            )
        }
    }

    private fun handleMessageTextChanged(message: String) {
        onNextMessageUseCase(message)
    }

    private suspend fun handleSendMessage() {
        updateState {
            copy(
                messageEditTextEnabled = false,
                addAttachmentButtonEnabled = false
            )
        }
        sendEffect(MessageCenterWelcomeEffect.HideSoftKeyboard)

        requestNotificationPermissionIfPushNotificationsSetUpUseCase {
            sendSecureMessageUseCase { _: VisitorMessage?, gliaException: GliaException? ->
                handleSendMessageResult(gliaException)
            }
        }
    }

    private fun handleSendMessageResult(gliaException: GliaException?) {
        if (gliaException == null) {
            sendEffect(MessageCenterWelcomeEffect.NavigateToConfirmation)
        } else {
            when (gliaException.cause) {
                GliaException.Cause.AUTHENTICATION_ERROR -> {
                    sendEffect(MessageCenterWelcomeEffect.ShowMessageCenterUnavailableDialog)
                    updateState { copy(showSendMessageGroup = false) }
                }

                GliaException.Cause.INTERNAL_ERROR -> {
                    sendEffect(MessageCenterWelcomeEffect.ShowUnexpectedErrorDialog)
                    updateState { copy(showSendMessageGroup = false) }
                }

                else -> {
                    sendEffect(MessageCenterWelcomeEffect.ShowUnexpectedErrorDialog)
                    updateState {
                        copy(
                            messageEditTextEnabled = true,
                            addAttachmentButtonEnabled = true
                        )
                    }
                }
            }
        }
    }

    private fun handleCheckMessages() {
        if (isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement) {
            sendEffect(MessageCenterWelcomeEffect.ReturnToLiveChat)
        } else {
            sendEffect(MessageCenterWelcomeEffect.NavigateToMessaging)
        }
        reset()
    }

    private fun handleClose() {
        sendEffect(MessageCenterWelcomeEffect.Finish)
        reset()
    }

    private fun handleSystemBack() {
        sendEffect(MessageCenterWelcomeEffect.DismissDialogs)
        reset()
    }

    private fun handleAddAttachment() {
        sendEffect(MessageCenterWelcomeEffect.ShowAttachmentPopup)
    }

    private fun handleGallery() {
        sendEffect(MessageCenterWelcomeEffect.SelectMediaAttachmentFile(allowedMediaTypes))
        GliaLogger.logScWelcomeScreenButtonClicked(ButtonNames.ADD_ATTACHMENT_PHOTO_LIBRARY_OPTION)
    }

    private fun handleBrowse() {
        sendEffect(MessageCenterWelcomeEffect.SelectAttachmentFile(allowedFileTypes))
        GliaLogger.logScWelcomeScreenButtonClicked(ButtonNames.ADD_ATTACHMENT_FILES_OPTION)
    }

    private fun handleTakePhoto() {
        takePictureUseCase.prepare { uri ->
            sendEffect(MessageCenterWelcomeEffect.TakePhoto(uri))
        }
    }

    private fun handleImageCaptured(captured: Boolean) {
        takePictureUseCase.onImageCaptured(captured, ::onAttachmentReceived)
    }

    private fun handleContentChosen(uri: Uri) {
        uriToFileAttachmentUseCase(uri)?.also(::onAttachmentReceived)
    }

    private fun onAttachmentReceived(file: LocalAttachment) {
        addFileToAttachmentAndUploadUseCase(
            file,
            object : AddFileToAttachmentAndUploadUseCase.Listener {
                override fun onFinished() {
                    Logger.d(TAG, "fileUploadFinished")
                    takePictureUseCase.deleteCurrent()
                }

                override fun onStarted() {
                    Logger.d(TAG, "fileUploadStarted")
                }

                override fun onError(ex: Exception) {
                    Logger.e(TAG, "Upload file failed: ${ex.message}")
                    takePictureUseCase.deleteCurrent()
                }

                override fun onSecurityCheckStarted() {
                    Logger.d(TAG, "fileUploadSecurityCheckStarted")
                }

                override fun onSecurityCheckFinished(scanResult: EngagementFile.ScanResult?) {
                    Logger.d(TAG, "fileUploadSecurityCheckFinished result=$scanResult")
                }
            }
        )
    }

    private fun handleRemoveAttachment(attachment: LocalAttachment) {
        removeFileAttachmentUseCase(attachment)
    }

    private fun reset() {
        resetMessageCenterUseCase()
    }

    private fun mapButtonState(state: MessageCenterState.ButtonState): MessageCenterWelcomeUiState.ButtonState {
        return when (state) {
            MessageCenterState.ButtonState.NORMAL -> MessageCenterWelcomeUiState.ButtonState.NORMAL
            MessageCenterState.ButtonState.DISABLE -> MessageCenterWelcomeUiState.ButtonState.DISABLE
            MessageCenterState.ButtonState.PROGRESS -> MessageCenterWelcomeUiState.ButtonState.PROGRESS
        }
    }

    private data class AllowedFileTypesState(
        val isSendFilesAllowed: Boolean = false,
        val isLibraryAttachmentAllowed: Boolean = true,
        val isTakePhotoAttachmentAllowed: Boolean = true,
        val isBrowseAttachmentAllowed: Boolean = true
    )
}