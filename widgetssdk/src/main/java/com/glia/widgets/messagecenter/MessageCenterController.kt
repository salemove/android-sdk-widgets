package com.glia.widgets.messagecenter

import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.androidsdk.engagement.EngagementFile
import com.glia.androidsdk.site.SiteInfo
import com.glia.widgets.Constants
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.ChatType
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.chat.domain.SiteInfoUseCase
import com.glia.widgets.chat.domain.TakePictureUseCase
import com.glia.widgets.chat.domain.UriToFileAttachmentUseCase
import com.glia.widgets.core.configuration.EngagementConfiguration
import com.glia.widgets.core.dialog.DialogContract
import com.glia.widgets.core.engagement.domain.SetEngagementConfigUseCase
import com.glia.widgets.core.fileupload.domain.AddFileToAttachmentAndUploadUseCase
import com.glia.widgets.core.fileupload.model.LocalAttachment
import com.glia.widgets.core.permissions.domain.RequestNotificationPermissionIfPushNotificationsSetUpUseCase
import com.glia.widgets.core.secureconversations.domain.AddSecureFileAttachmentsObserverUseCase
import com.glia.widgets.core.secureconversations.domain.AddSecureFileToAttachmentAndUploadUseCase
import com.glia.widgets.core.secureconversations.domain.GetAvailableQueueIdsForSecureMessagingUseCase
import com.glia.widgets.core.secureconversations.domain.GetSecureFileAttachmentsUseCase
import com.glia.widgets.core.secureconversations.domain.OnNextMessageUseCase
import com.glia.widgets.core.secureconversations.domain.RemoveSecureFileAttachmentUseCase
import com.glia.widgets.core.secureconversations.domain.ResetMessageCenterUseCase
import com.glia.widgets.core.secureconversations.domain.SendMessageButtonStateUseCase
import com.glia.widgets.core.secureconversations.domain.SendSecureMessageUseCase
import com.glia.widgets.core.secureconversations.domain.ShowMessageLimitErrorUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.view.head.ChatHeadContract
import io.reactivex.rxjava3.disposables.CompositeDisposable

internal class MessageCenterController(
    private var serviceChatHeadController: ChatHeadContract.Controller,
    private val engagementConfigUseCase: SetEngagementConfigUseCase,
    private val sendSecureMessageUseCase: SendSecureMessageUseCase,
    private val getAvailableQueueIdsForSecureMessagingUseCase: GetAvailableQueueIdsForSecureMessagingUseCase,
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
    private val dialogController: DialogContract.Controller,
    private val takePictureUseCase: TakePictureUseCase,
    private val uriToFileAttachmentUseCase: UriToFileAttachmentUseCase,
    private val requestNotificationPermissionIfPushNotificationsSetUpUseCase: RequestNotificationPermissionIfPushNotificationsSetUpUseCase
) : MessageCenterContract.Controller {
    private var view: MessageCenterContract.View? = null
    private val disposables = CompositeDisposable()

    @Volatile
    private var state = MessageCenterState()

    override fun setConfiguration(uiTheme: UiTheme?, configuration: EngagementConfiguration?) {
        setQueueIds(configuration?.queueIds)

        serviceChatHeadController.setBuildTimeTheme(uiTheme)
        serviceChatHeadController.setEngagementConfiguration(configuration)
    }

    private fun setQueueIds(queueIds: List<String>?) {
        engagementConfigUseCase(ChatType.SECURE_MESSAGING, queueIds ?: emptyList())
    }

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
        getAvailableQueueIdsForSecureMessagingUseCase(
            RequestCallback { queueIds, exception ->
                setQueueIds(queueIds)

                val showSendMessageGroup = when {
                    exception != null -> {
                        dialogController.showUnexpectedErrorDialog()
                        false
                    }

                    queueIds == null -> {
                        dialogController.showMessageCenterUnavailableDialog()
                        false
                    }

                    else -> true
                }
                setState(state.copy(showSendMessageGroup = showSendMessageGroup))
            }
        )
    }

    fun onAttachmentReceived(file: LocalAttachment) {
        addFileToAttachmentAndUploadUseCase.execute(
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
        removeFileAttachmentUseCase.execute(file)
    }

    override fun onDestroy() {
        getAvailableQueueIdsForSecureMessagingUseCase.dispose()
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
