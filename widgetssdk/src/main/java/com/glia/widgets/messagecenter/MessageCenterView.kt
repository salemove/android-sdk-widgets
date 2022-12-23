package com.glia.widgets.messagecenter

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.*
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.AttachmentPopup
import com.glia.widgets.databinding.MessageCenterViewBinding
import com.glia.widgets.helper.Utils
import com.glia.widgets.view.Dialogs
import com.glia.widgets.view.header.AppBarView
import com.google.android.material.button.MaterialButton
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import kotlin.properties.Delegates

class MessageCenterView(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int)
    : ConstraintLayout(
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

    var onFinishListener: OnFinishListener? = null
    var onNavigateToMessagingListener: OnNavigateToMessagingListener? = null

    private var controller: MessageCenterContract.Controller? = null

    private val binding: MessageCenterViewBinding by lazy {
        MessageCenterViewBinding.inflate(LayoutInflater.from(this.context), this)
    }

    private val attachmentPopup by lazy { AttachmentPopup(addAttachmentButton) }

    private val appBar: AppBarView get() = binding.appBarView
    private val icon: ImageView get() = binding.icon
    private val title: TextView get() = binding.title
    private val description: TextView get() = binding.description
    private val checkMessagesButton: MaterialButton get() = binding.btnCheckMessages
    private val sendMessageButton: MaterialButton get() = binding.btnSendMessage
    private val messageTitle: TextView get() = binding.messageTitle
    private val messageEditText: EditText get() = binding.messageEditText
    private val addAttachmentButton: ImageButton get() = binding.addAttachmentButton
    private val attachmentRecyclerView: RecyclerView get() = binding.attachmentsRecyclerView
    private var alertDialog: AlertDialog? = null

    @JvmOverloads
    constructor(
            context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.gliaChatStyle
    ) : this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat)

    init {
        initConfigurations()
        readTypedArray(attrs, defStyleAttr, defStyleRes)
        initCallbacks()
    }

    private fun readTypedArray(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        context.withStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes) {
            setDefaultTheme(this)
        }
    }

    private fun setDefaultTheme(typedArray: TypedArray) {
        theme = Utils.getThemeFromTypedArray(typedArray, this.context)
    }

    private fun initConfigurations() {
        setBackgroundColor(ContextCompat.getColor(this.context, R.color.glia_chat_background_color))
        // needed to overlap existing app bar in existing view with this view's app bar.
        ViewCompat.setElevation(this, 100.0f)
    }

    private fun initCallbacks() {
        checkMessagesButton.setOnClickListener {
            controller?.onCheckMessagesClicked()
        }
        sendMessageButton.setOnClickListener {
            controller?.onSendMessageClicked(messageEditText.text.toString())
        }
        appBar.setOnBackClickedListener {
            controller?.onBackArrowClicked()
        }
        appBar.setOnXClickedListener {
            controller?.onCloseButtonClicked()
        }
        addAttachmentButton.setOnClickListener {
            controller?.onAddAttachmentButtonClicked()
        }
    }

    override fun showAttachmentPopup() {
        attachmentPopup.show(
            addAttachmentButton,
            { controller?.onGalleryClicked() },
            { controller?.onTakePhotoClicked() },
            { controller?.onBrowseClicked() }
        )
    }

    private fun showAlertDialog(
        @StringRes title: Int, @StringRes message: Int, buttonClickListener: OnClickListener
    ) {
        dismissAlertDialog()
        alertDialog = Dialogs.showAlertDialog(
            this.context, theme, title, message, buttonClickListener
        )
    }

    private fun dismissAlertDialog() {
        alertDialog?.apply {
            dismiss()
            alertDialog = null
        }
    }

    override fun showUnexpectedErrorDialog() {
        showAlertDialog(
            R.string.glia_dialog_unexpected_error_title,
            R.string.glia_dialog_unexpected_error_message
        ) {
            dismissAlertDialog()
            alertDialog = null
        }
    }

    override fun showMessageCenterUnavailableDialog() {
        showAlertDialog(
            R.string.glia_dialog_message_center_unavailable_title,
            R.string.glia_dialog_message_center_unavailable_message
        ) {
            dismissAlertDialog()
            alertDialog = null
        }
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
}
