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
import com.glia.androidsdk.RequestCallback
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.databinding.MessageCenterViewBinding
import com.glia.widgets.helper.Utils
import com.glia.widgets.view.Dialogs
import com.glia.widgets.view.header.AppBarView
import com.glia.widgets.view.unifiedui.exstensions.getColorCompat
import com.glia.widgets.view.unifiedui.exstensions.layoutInflater
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

    @JvmOverloads
    constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.gliaChatStyle
    ) : this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat)

    init {
        orientation = VERTICAL
        readTypedArray(attrs, defStyleAttr, defStyleRes)
    }

    override fun setupViewAppearance() {
        initCallbacks()
        initConfigurations()
        val callback: RequestCallback<Boolean> = RequestCallback { isAvailable, exception ->
            if (exception != null) {
                showUnexpectedErrorDialog()
                messageView.hideSendMessageGroup()
                return@RequestCallback
            }
            if (!isAvailable) {
                showMessageCenterUnavailableDialog()
                messageView.hideSendMessageGroup()
            }
        }

        controller?.isMessageCenterAvailable(callback)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        defaultStatusBarColor = window?.statusBarColor
        window?.statusBarColor = statusBarColor
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        window?.statusBarColor = defaultStatusBarColor ?: return
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
    }

    private fun initCallbacks() {
        messageView.setOnCheckMessageButtonClickListener {
            controller?.onCheckMessagesClicked()
        }
        messageView.setOnSendMessageButtonClickListener {
            controller?.onSendMessageClicked(it)
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
        appBar.setOnBackClickedListener {
            controller?.onBackArrowClicked()
        }
        appBar.setOnXClickedListener {
            controller?.onCloseButtonClicked()
        }

        confirmationView.setOnCheckMessagesButtonClickListener {
            controller?.onCheckMessagesClicked()
        }
    }

    override fun showUnAuthenticatedDialog() {
        dismissAlertDialog()
        alertDialog = Dialogs.showAlertDialog(
            context,
            theme,
            R.string.glia_dialog_message_center_unavailable_title,
            R.string.glia_dialog_message_center_unauthorized_message
        ) {
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

    override fun showUnexpectedErrorDialog() {
        dismissAlertDialog()
        alertDialog = Dialogs.showAlertDialog(
            this.context,
            theme,
            R.string.glia_dialog_unexpected_error_title,
            R.string.glia_dialog_unexpected_error_message
        ) {
            dismissAlertDialog()
            alertDialog = null
        }
    }

    override fun showMessageCenterUnavailableDialog() {
        dismissAlertDialog()
        alertDialog = Dialogs.showMessageCenterUnavailableDialog(
            this.context,
            theme
        )
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
        messageView.onStateUpdated(state)
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
}
