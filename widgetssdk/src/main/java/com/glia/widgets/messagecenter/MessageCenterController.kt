package com.glia.widgets.messagecenter

import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.androidsdk.engagement.EngagementFile
import com.glia.androidsdk.site.SiteInfo
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.chat.domain.SiteInfoUseCase
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.fileupload.domain.AddFileToAttachmentAndUploadUseCase
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.core.secureconversations.domain.*
import com.glia.widgets.helper.Logger
import io.reactivex.disposables.CompositeDisposable

internal class MessageCenterController(
    private val sendSecureMessageUseCase: SendSecureMessageUseCase,
    private val isMessageCenterAvailableUseCase: IsMessageCenterAvailableUseCase,
    private val addFileAttachmentsObserverUseCase: AddSecureFileAttachmentsObserverUseCase,
    private val addFileToAttachmentAndUploadUseCase: AddSecureFileToAttachmentAndUploadUseCase,
    private val getFileAttachmentsUseCase: GetSecureFileAttachmentsUseCase,
    private val removeFileAttachmentUseCase: RemoveSecureFileAttachmentUseCase,
    private val isAuthenticatedUseCase: IsAuthenticatedUseCase,
    private val siteInfoUseCase: SiteInfoUseCase,
    private val onNextMessageUseCase: OnNextMessageUseCase,
    private val sendMessageButtonStateUseCase: SendMessageButtonStateUseCase,
    private val showMessageLimitErrorUseCase: ShowMessageLimitErrorUseCase,
    private val resetMessageCenterUseCase: ResetMessageCenterUseCase,
    private val dialogController: DialogController
) : MessageCenterContract.Controller {
    private var view: MessageCenterContract.View? = null
    private val disposables = CompositeDisposable()

    @Volatile
    private var state = State()

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
        setState(state)

        view?.emitUploadAttachments(getFileAttachmentsUseCase())
        view?.setupViewAppearance()

        disposables.add(addFileAttachmentsObserverUseCase().subscribe {
            view?.emitUploadAttachments(it)
        })
        disposables.add(showMessageLimitErrorUseCase().subscribe {
            setState(state.copy(showMessageLimitError = it))
        })
        disposables.add(sendMessageButtonStateUseCase().subscribe {
            setState(state.copy(sendMessageButtonState = it))
        })

        updateAllowFileSendState()
    }

    private fun updateAllowFileSendState() {
        siteInfoUseCase.execute { siteInfo: SiteInfo?, _ ->
            onSiteInfoReceived(siteInfo)
        }
    }

    private fun onSiteInfoReceived(siteInfo: SiteInfo?) {
        val attachmentAllowed = siteInfo?.allowedFileSenders?.isVisitorAllowed ?: false
        setState(state.copy(addAttachmentButtonVisible = attachmentAllowed))
    }

    override fun onCheckMessagesClicked() {
        view?.navigateToMessaging()
        reset()
    }

    override fun onMessageChanged(message: String) {
        onNextMessageUseCase(message)
    }

    override fun onSendMessageClicked() {
        setState(
            state.copy(
                messageEditTextEnabled = false,
                addAttachmentButtonEnabled = false
            )
        )
        view?.hideSoftKeyboard()
        sendSecureMessageUseCase { _: VisitorMessage?, gliaException: GliaException? ->
            handleSendMessageResult(gliaException)
        }
    }

    @VisibleForTesting
    fun handleSendMessageResult(gliaException: GliaException?) {
        if (gliaException == null) {
            view?.showConfirmationScreen()
        } else {
            when (gliaException.cause) {
                GliaException.Cause.AUTHENTICATION_ERROR -> {
                    dialogController.showMessageCenterUnavailableDialog()
                    setState(state.copy(showSendMessageGroup = false))
                }
                GliaException.Cause.INTERNAL_ERROR -> {
                    dialogController.showUnexpectedErrorDialog()
                    setState(state.copy(showSendMessageGroup = false))
                }
                else -> {
                    dialogController.showUnexpectedErrorDialog()
                    setState(
                        state.copy(
                            messageEditTextEnabled = true,
                            addAttachmentButtonEnabled = true
                        )
                    )
                }
            }
        }
    }

    override fun onCloseButtonClicked() {
        view?.finish()
        reset()
    }

    override fun onSystemBack() {
        reset()
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
                exception != null -> {
                    dialogController.showUnexpectedErrorDialog()
                    setState(state.copy(showSendMessageGroup = false))
                }
                !isAvailable -> {
                    dialogController.showMessageCenterUnavailableDialog()
                    setState(state.copy(showSendMessageGroup = false))
                }
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
        disposables.dispose()
    }

    @Synchronized
    private fun setState(state: State) {
        this.state = state
        this.view?.onStateUpdated(state)
    }

    private fun reset() {
        resetMessageCenterUseCase()
    }

    override fun addCallback(dialogCallback: DialogController.Callback) {
        dialogController.addCallback(dialogCallback)
    }

    override fun removeCallback(dialogCallback: DialogController.Callback) {
        dialogController.removeCallback(dialogCallback)
    }

    override fun dismissDialogs() {
        dialogController.dismissDialogs()
    }

    override fun dismissCurrentDialog() {
        dialogController.dismissCurrentDialog()
    }

    companion object {
        private const val TAG = "MessageCenterController"

        private const val FILE_TYPE_IMAGES = "image/*"
        private const val FILE_TYPE_ALL = "*/*"
    }
}
