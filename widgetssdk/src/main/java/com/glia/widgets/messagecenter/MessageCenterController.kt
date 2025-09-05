package com.glia.widgets.messagecenter

import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.androidsdk.engagement.EngagementFile
import com.glia.androidsdk.site.SiteInfo
import com.glia.widgets.Constants
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.chat.domain.SiteInfoUseCase
import com.glia.widgets.chat.domain.TakePictureUseCase
import com.glia.widgets.chat.domain.UriToFileAttachmentUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.internal.dialog.DialogContract
import com.glia.widgets.internal.fileupload.domain.AddFileAttachmentsObserverUseCase
import com.glia.widgets.internal.fileupload.domain.AddFileToAttachmentAndUploadUseCase
import com.glia.widgets.internal.fileupload.domain.GetFileAttachmentsUseCase
import com.glia.widgets.internal.fileupload.domain.RemoveFileAttachmentUseCase
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.glia.widgets.internal.permissions.domain.RequestNotificationPermissionIfPushNotificationsSetUpUseCase
import com.glia.widgets.internal.secureconversations.domain.AddSecureFileToAttachmentAndUploadUseCase
import com.glia.widgets.internal.secureconversations.domain.IsMessagingAvailableUseCase
import com.glia.widgets.internal.secureconversations.domain.OnNextMessageUseCase
import com.glia.widgets.internal.secureconversations.domain.ResetMessageCenterUseCase
import com.glia.widgets.internal.secureconversations.domain.SendMessageButtonStateUseCase
import com.glia.widgets.internal.secureconversations.domain.SendSecureMessageUseCase
import com.glia.widgets.internal.secureconversations.domain.ShowMessageLimitErrorUseCase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable

internal class MessageCenterController(
    private val sendSecureMessageUseCase: SendSecureMessageUseCase,
    private val addFileAttachmentsObserverUseCase: AddFileAttachmentsObserverUseCase,
    private val addFileToAttachmentAndUploadUseCase: AddSecureFileToAttachmentAndUploadUseCase,
    private val getFileAttachmentsUseCase: GetFileAttachmentsUseCase,
    private val removeFileAttachmentUseCase: RemoveFileAttachmentUseCase,
    private val isAuthenticatedUseCase: IsAuthenticatedUseCase,
    private val siteInfoUseCase: SiteInfoUseCase,
    private val onNextMessageUseCase: OnNextMessageUseCase,
    private val sendMessageButtonStateUseCase: SendMessageButtonStateUseCase,
    private val showMessageLimitErrorUseCase: ShowMessageLimitErrorUseCase,
    private val resetMessageCenterUseCase: ResetMessageCenterUseCase,
    private val dialogController: DialogContract.Controller,
    private val takePictureUseCase: TakePictureUseCase,
    private val uriToFileAttachmentUseCase: UriToFileAttachmentUseCase,
    private val requestNotificationPermissionIfPushNotificationsSetUpUseCase: RequestNotificationPermissionIfPushNotificationsSetUpUseCase,
    private val isMessagingAvailableUseCase: IsMessagingAvailableUseCase,
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase
) : MessageCenterContract.Controller {
    private var view: MessageCenterContract.View? = null
    private val disposables = CompositeDisposable()

    @Volatile
    private var state = MessageCenterState()

    override fun setView(view: MessageCenterContract.View) {
        this.view = view
    }

    override fun initialize() {
        if (isAuthenticatedUseCase()) {
            initComponents()
        } else {
            dialogController.showUnauthenticatedDialog()
            Logger.i(TAG, "Secure Messaging is unavailable because the visitor is not authenticated.")
        }
    }

    private fun initComponents() {
        setState(state)

        view?.setupViewAppearance()
        view?.emitUploadAttachments(getFileAttachmentsUseCase())

        disposables.add(
            addFileAttachmentsObserverUseCase().subscribe {
                view?.emitUploadAttachments(it)
            }
        )
        disposables.add(
            showMessageLimitErrorUseCase().subscribe {
                setState(state.copy(showMessageLimitError = it))
            }
        )
        disposables.add(
            sendMessageButtonStateUseCase().subscribe {
                setState(state.copy(sendMessageButtonState = it))
            }
        )

        updateAllowFileSendState()
    }

    private fun updateAllowFileSendState() {
        siteInfoUseCase { siteInfo: SiteInfo?, _ -> onSiteInfoReceived(siteInfo) }
    }

    private fun onSiteInfoReceived(siteInfo: SiteInfo?) {
        val attachmentAllowed = siteInfo?.allowedFileSenders?.isVisitorAllowed ?: false
        setState(state.copy(addAttachmentButtonVisible = attachmentAllowed))
    }

    override fun onCheckMessagesClicked() {
        if (isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement) {
            view?.returnToLiveChat()
        } else {
            view?.navigateToMessaging()
        }
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

        requestNotificationPermissionIfPushNotificationsSetUpUseCase {
            sendSecureMessageUseCase { _: VisitorMessage?, gliaException: GliaException? ->
                handleSendMessageResult(gliaException)
            }
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
        view?.selectAttachmentFile(Constants.MIME_TYPE_IMAGES)
    }

    override fun onBrowseClicked() {
        view?.selectAttachmentFile(Constants.MIME_TYPE_ALL)
    }

    override fun onTakePhotoClicked() {
        takePictureUseCase.prepare {
            view?.takePhoto(it)
        }
    }

    override fun ensureMessageCenterAvailability() {
        disposables.add(isMessagingAvailableUseCase().observeOn(AndroidSchedulers.mainThread()).subscribe(::handleMessagingAvailableResult))
    }

    private fun handleMessagingAvailableResult(isAvailable: Boolean) {
        if (isAvailable) {
            dialogController.dismissMessageCenterUnavailableDialog()
        } else {
            dialogController.showMessageCenterUnavailableDialog()
        }

        setState(state.copy(showSendMessageGroup = isAvailable))

    }

    fun onAttachmentReceived(file: LocalAttachment) {
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
                    Logger.e(TAG, "Upload file failed: " + ex.message)
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

    override fun onRemoveAttachment(file: LocalAttachment) {
        removeFileAttachmentUseCase(file)
    }

    override fun onDestroy() {
        view = null
        disposables.dispose()
    }

    @Synchronized
    private fun setState(state: MessageCenterState) {
        this.state = state
        this.view?.onStateUpdated(state)
    }

    private fun reset() {
        resetMessageCenterUseCase()
    }

    override fun addCallback(dialogCallback: DialogContract.Controller.Callback) {
        dialogController.addCallback(dialogCallback)
    }

    override fun removeCallback(dialogCallback: DialogContract.Controller.Callback) {
        dialogController.removeCallback(dialogCallback)
    }

    override fun dismissDialogs() {
        dialogController.dismissDialogs()
    }

    override fun dismissCurrentDialog() {
        dialogController.dismissCurrentDialog()
    }

    override fun onImageCaptured(result: Boolean) {
        takePictureUseCase.onImageCaptured(result, ::onAttachmentReceived)
    }

    override fun onContentChosen(uri: Uri) {
        uriToFileAttachmentUseCase(uri)?.also(::onAttachmentReceived)
    }
}
