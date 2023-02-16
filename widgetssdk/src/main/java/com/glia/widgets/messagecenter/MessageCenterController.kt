package com.glia.widgets.messagecenter

import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.androidsdk.engagement.EngagementFile
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.fileupload.domain.AddFileToAttachmentAndUploadUseCase
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.core.secureconversations.domain.*
import com.glia.widgets.helper.Logger
import java.util.*

internal class MessageCenterController(
    private val sendSecureMessageUseCase: SendSecureMessageUseCase,
    private val isMessageCenterAvailableUseCase: IsMessageCenterAvailableUseCase,
    private val addFileAttachmentsObserverUseCase: AddSecureFileAttachmentsObserverUseCase,
    private val addFileToAttachmentAndUploadUseCase: AddSecureFileToAttachmentAndUploadUseCase,
    private val getFileAttachmentsUseCase: GetSecureFileAttachmentsUseCase,
    private val removeFileAttachmentObserverUseCase: RemoveSecureFileAttachmentObserverUseCase,
    private val removeFileAttachmentUseCase: RemoveSecureFileAttachmentUseCase,
    private val isAuthenticatedUseCase: IsAuthenticatedUseCase,
    private val dialogController: DialogController
) : MessageCenterContract.Controller {
    private var view: MessageCenterContract.View? = null
    private var state = State()

    private val fileAttachmentObserver = Observer { _, _ ->
        view?.emitUploadAttachments(getFileAttachmentsUseCase.execute())
    }

    override var photoCaptureFileUri: Uri? = null

    override fun setView(view: MessageCenterContract.View) {
        this.view = view

        if (isAuthenticatedUseCase()) {
            initComponents()
        } else {
            dialogController.showUnauthenticatedDialog()
        }
    }

    private fun initComponents() {
        addFileAttachmentsObserverUseCase.execute(fileAttachmentObserver)
        setState(state)

        view?.emitUploadAttachments(getFileAttachmentsUseCase.execute())
        view?.setupViewAppearance()
    }

    override fun onCheckMessagesClicked() {
        view?.navigateToMessaging()
    }

    override fun onMessageChanged(message: String) {
        val showLimitError = message.count() > MAX_MESSAGE_LENGTH
        if (state.showMessageLimitError != showLimitError) {
            setState(state.copy(showMessageLimitError = showLimitError))
        }

        val sendButtonState = if (message.isEmpty() || showLimitError) {
            State.ButtonState.DISABLE
        } else {
            State.ButtonState.NORMAL
        }
        if (state.sendMessageButtonState != sendButtonState) {
            setState(state.copy(sendMessageButtonState = sendButtonState))
        }
    }

    override fun onSendMessageClicked(message: String) {
        setState(
            state.copy(
                sendMessageButtonState = State.ButtonState.PROGRESS,
                messageEditTextEnabled = false,
                addAttachmentButtonEnabled = false
            )
        )
        view?.hideSoftKeyboard()
        val callback =
            RequestCallback { _: VisitorMessage?, gliaException: GliaException? ->
                handleSendMessageResult(gliaException)
            }
        sendSecureMessageUseCase.execute(message, callback)
    }

    @VisibleForTesting
    fun handleSendMessageResult(gliaException: GliaException?) {
        if (gliaException == null) {
            view?.showConfirmationScreen()
        } else {
            when (gliaException.cause) {
                GliaException.Cause.AUTHENTICATION_ERROR -> dialogController.showMessageCenterUnavailableDialog()
                GliaException.Cause.INTERNAL_ERROR -> dialogController.showUnexpectedErrorDialog()
                else -> {
                    dialogController.showUnexpectedErrorDialog()
                    setState(
                        state.copy(
                            sendMessageButtonState = State.ButtonState.NORMAL,
                            messageEditTextEnabled = true,
                            addAttachmentButtonEnabled = true
                        )
                    )
                }
            }
        }
    }

    override fun onBackArrowClicked() {
        view?.finish()
    }

    override fun onCloseButtonClicked() {
        view?.finish()
    }

    override fun onAddAttachmentButtonClicked() {
        view?.showAttachmentPopup()
    }

    override fun onGalleryClicked() {
        view?.selectAttachmentFile(FILE_TYPE_IMAGES)
    }

    override fun onBrowseClicked() {
        view?.selectAttachmentFile(FILE_TYPE_ALL)
    }

    override fun onTakePhotoClicked() {
        view?.takePhoto()
    }

    override fun ensureMessageCenterAvailability() {
        isMessageCenterAvailableUseCase(RequestCallback { isAvailable, exception ->
            when {
                exception != null -> dialogController.showUnexpectedErrorDialog()
                !isAvailable -> dialogController.showMessageCenterUnavailableDialog()
                else -> Logger.d(TAG, "Message center is available")
            }
        })
    }

    override fun onAttachmentReceived(file: FileAttachment) {
        addFileToAttachmentAndUploadUseCase.execute(
            file,
            object : AddFileToAttachmentAndUploadUseCase.Listener {
                override fun onFinished() {
                    Logger.d(TAG, "fileUploadFinished")
                }

                override fun onStarted() {
                    Logger.d(TAG, "fileUploadStarted")
                }

                override fun onError(ex: Exception) {
                    Logger.e(TAG, "Upload file failed: " + ex.message)
                }

                override fun onSecurityCheckStarted() {
                    Logger.d(TAG, "fileUploadSecurityCheckStarted")
                }

                override fun onSecurityCheckFinished(scanResult: EngagementFile.ScanResult?) {
                    Logger.d(TAG, "fileUploadSecurityCheckFinished result=$scanResult")
                }
            })
    }

    override fun onRemoveAttachment(file: FileAttachment) {
        removeFileAttachmentUseCase.execute(file)
    }

    override fun onDestroy() {
        isMessageCenterAvailableUseCase.dispose()
        removeFileAttachmentObserverUseCase.execute(fileAttachmentObserver)
    }

    private fun setState(state: State) {
        this.state = state
        this.view?.onStateUpdated(state)
    }

    companion object {
        private const val TAG = "MessageCenterController"

        private const val MAX_MESSAGE_LENGTH = 10000

        private const val FILE_TYPE_IMAGES = "image/*"
        private const val FILE_TYPE_ALL = "*/*"
    }
}
