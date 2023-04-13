package com.glia.widgets.messagecenter

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.view.Window
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.ViewCompat
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.core.configuration.GliaSdkConfiguration
import com.glia.widgets.core.dialog.Dialog
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.databinding.MessageCenterViewBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Utils
import com.glia.widgets.view.Dialogs
import com.glia.widgets.view.header.AppBarView
import com.glia.widgets.view.unifiedui.extensions.applyColorTheme
import com.glia.widgets.view.unifiedui.extensions.getColorCompat
import com.glia.widgets.view.unifiedui.extensions.layoutInflater
import com.glia.widgets.view.unifiedui.theme.secureconversations.SecureConversationsWelcomeScreenTheme
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import kotlin.properties.Delegates

class MessageCenterView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : LinearLayout(
    MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, defStyleRes),
    attrs,
    defStyleAttr,
    defStyleRes
), MessageCenterContract.View {

    private var theme: UiTheme by Delegates.notNull()
    private val unifiedTheme: SecureConversationsWelcomeScreenTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.secureConversationsWelcomeScreenTheme
    }

    interface OnFinishListener {
        fun finish()
    }

    interface OnNavigateToMessagingListener {
        fun navigateToMessaging()
    }

    interface OnAttachFileListener {
        fun selectAttachmentFile(type: String)
        fun takePhoto()
    }

    var onFinishListener: OnFinishListener? = null
    var onNavigateToMessagingListener: OnNavigateToMessagingListener? = null
    var onAttachFileListener: OnAttachFileListener? = null

    private var controller: MessageCenterContract.Controller? = null

    private val binding: MessageCenterViewBinding by lazy {
        MessageCenterViewBinding.inflate(layoutInflater, this)
    }

    private val appBar: AppBarView get() = binding.appBarView
    private val messageView: MessageView get() = binding.messageView
    private val confirmationView: ConfirmationScreenView get() = binding.confirmationView

    private var alertDialog: AlertDialog? = null

    // Is needed for setting status bar color back when view is gone
    private var defaultStatusBarColor: Int? = null
    private var statusBarColor: Int by Delegates.notNull()

    private val window: Window? by lazy { Utils.getActivity(this.context)?.window }

    private val dialogCallback: DialogController.Callback = DialogController.Callback {
        onDialogState(it)
    }

    @JvmOverloads
    constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.gliaChatStyle
    ) : this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat)

    init {
        orientation = VERTICAL
        initConfigurations()
        readTypedArray(attrs, defStyleAttr, defStyleRes)
        setupUnifiedTheme()
    }

    private fun setupUnifiedTheme() {
        unifiedTheme?.headerTheme?.background?.fill?.primaryColor?.also {
            statusBarColor = it
        }
        appBar.applyHeaderTheme(unifiedTheme?.headerTheme)
        applyColorTheme(unifiedTheme?.backgroundTheme)
    }

    override fun setupViewAppearance() {
        initCallbacks()
        controller?.ensureMessageCenterAvailability()
    }

    private fun readTypedArray(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        context.withStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes) {
            setDefaultTheme(this)
        }
    }

    private fun setDefaultTheme(typedArray: TypedArray) {
        theme = Utils.getThemeFromTypedArray(typedArray, this.context)
        statusBarColor = theme.brandPrimaryColor?.let(::getColorCompat) ?: Color.TRANSPARENT
    }

    private fun initConfigurations() {
        setBackgroundColor(ContextCompat.getColor(this.context, R.color.glia_chat_background_color))
        // Is needed to overlap existing app bar in existing view with this view's app bar.
        ViewCompat.setElevation(this, 100.0f)

        appBar.hideBackButton()
    }

    private fun initCallbacks() {
        messageView.setOnCheckMessageButtonClickListener {
            clearAndDismissDialogs()
            controller?.onCheckMessagesClicked()
        }
        messageView.setOnSendMessageButtonClickListener {
            controller?.onSendMessageClicked()
        }
        messageView.setOnAttachmentButtonClickListener {
            controller?.onAddAttachmentButtonClicked()
        }
        messageView.setOnMessageTextChangedListener {
            controller?.onMessageChanged(it)
        }
        messageView.setOnRemoveAttachmentListener {
            controller?.onRemoveAttachment(it)
        }
        appBar.setOnXClickedListener {
            clearAndDismissDialogs()
            controller?.onCloseButtonClicked()
        }

        confirmationView.setOnCheckMessagesButtonClickListener {
            controller?.onCheckMessagesClicked()
        }
    }

    fun setConfiguration(configuration: GliaSdkConfiguration?) {
        controller?.setConfiguration(theme, configuration)
    }

    private fun showUnAuthenticatedDialog() {
        alertDialog = Dialogs.showAlertDialog(
            context,
            theme,
            R.string.glia_dialog_message_center_unavailable_title,
            R.string.glia_dialog_message_center_unauthorized_message
        ) {
            controller?.dismissCurrentDialog()
            controller?.onCloseButtonClicked()
        }
    }

    override fun showAttachmentPopup() {
        messageView.showAttachmentPopup(
            { controller?.onGalleryClicked() },
            { controller?.onTakePhotoClicked() },
            { controller?.onBrowseClicked() }
        )
    }

    private fun dismissAlertDialog() {
        alertDialog?.apply {
            dismiss()
            alertDialog = null
        }
    }

    private fun clearAndDismissDialogs() {
        controller?.dismissDialogs()
        dismissAlertDialog()
    }

    private fun showUnexpectedErrorDialog() {
        alertDialog = Dialogs.showAlertDialog(
            this.context,
            theme,
            R.string.glia_dialog_unexpected_error_title,
            R.string.glia_dialog_unexpected_error_message
        ) {
            controller?.dismissCurrentDialog()
        }
    }

    private fun showMessageCenterUnavailableDialog() {
        alertDialog = Dialogs.showMessageCenterUnavailableDialog(this.context, theme)
    }

    override fun showConfirmationScreen() {
        confirmationView.fadeThrough(messageView)
    }

    override fun hideSoftKeyboard() {
        Utils.hideSoftKeyboard(context, windowToken)
    }

    override fun setController(controller: MessageCenterContract.Controller?) {
        this.controller = controller
        controller?.setView(this)
    }

    override fun finish() {
        this.onFinishListener?.finish()
    }

    override fun navigateToMessaging() {
        this.onNavigateToMessagingListener?.navigateToMessaging()
    }

    override fun onStateUpdated(state: State) {
        binding.root.post {
            messageView.onStateUpdated(state)
        }
    }

    override fun emitUploadAttachments(attachments: List<FileAttachment>) {
        messageView.emitUploadAttachments(attachments)
    }

    override fun selectAttachmentFile(type: String) {
        onAttachFileListener?.selectAttachmentFile(type)
    }

    override fun takePhoto() {
        onAttachFileListener?.takePhoto()
    }

    private fun onDialogState(state: DialogState) {
        when (state.mode) {
            Dialog.MODE_NONE -> dismissAlertDialog()
            Dialog.MODE_UNEXPECTED_ERROR -> showUnexpectedErrorDialog()
            Dialog.MODE_MESSAGE_CENTER_UNAVAILABLE -> showMessageCenterUnavailableDialog()
            Dialog.MODE_UNAUTHENTICATED -> showUnAuthenticatedDialog()
            else -> throw UnsupportedOperationException("Unexpected dialog type for Message Center screen")
        }
    }

    fun onResume() {
        attachDialogController()
    }

    fun onPause() {
        detachDialogController()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        window?.statusBarColor = statusBarColor
        defaultStatusBarColor = window?.statusBarColor
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        window?.statusBarColor = defaultStatusBarColor ?: return
    }

    private fun attachDialogController() {
        controller?.addCallback(dialogCallback)
    }

    private fun detachDialogController() {
        controller?.removeCallback(dialogCallback)
    }

    fun onSystemBack() {
        controller?.onSystemBack()
        clearAndDismissDialogs()
    }
}
