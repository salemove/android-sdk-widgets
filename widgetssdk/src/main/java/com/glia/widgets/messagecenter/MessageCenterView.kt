package com.glia.widgets.messagecenter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.TypedArray
import android.net.Uri
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.glia.telemetry_lib.ButtonNames
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.widgets.Constants
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.databinding.MessageCenterViewBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.SimpleWindowInsetsAndAnimationHandler
import com.glia.widgets.helper.Utils
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.hideKeyboard
import com.glia.widgets.helper.insetsController
import com.glia.widgets.helper.isKeyboardVisible
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.helper.logScConfirmationScreenButtonClicked
import com.glia.widgets.helper.logScWelcomeScreenButtonClicked
import com.glia.widgets.helper.rootWindowInsetsCompat
import com.glia.widgets.internal.dialog.DialogContract
import com.glia.widgets.internal.dialog.model.DialogState
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.view.Dialogs
import com.glia.widgets.view.dialog.base.DialogDelegate
import com.glia.widgets.view.dialog.base.DialogDelegateImpl
import com.glia.widgets.view.header.AppBarView
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.HeaderTheme
import com.glia.widgets.view.unifiedui.theme.defaulttheme.DefaultHeader
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import kotlinx.parcelize.Parcelize
import java.util.concurrent.Executor
import kotlin.properties.Delegates

internal class MessageCenterView(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : LinearLayout(
    MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, defStyleRes),
    attrs,
    defStyleAttr,
    defStyleRes
), MessageCenterContract.View, DialogDelegate by DialogDelegateImpl() {

    private var theme: UiTheme by Delegates.notNull()
    private val unifiedTheme: UnifiedTheme? by lazy { Dependencies.gliaThemeManager.theme }

    var onFinishListener: OnFinishListener? = null
    var onNavigateToMessagingListener: OnNavigateToMessagingListener? = null
    var onAttachFileListener: OnAttachFileListener? = null

    private var controller: MessageCenterContract.Controller? = null

    private var binding: MessageCenterViewBinding? = null

    private val appBar: AppBarView? get() = binding?.appBarView
    private val messageView: MessageView? get() = binding?.messageView
    private val confirmationView: ConfirmationScreenView? get() = binding?.confirmationView

    private val dialogCallback: DialogContract.Controller.Callback = DialogContract.Controller.Callback {
        onDialogState(it)
    }

    init {
        isSaveEnabled = true
        orientation = VERTICAL
        // Is needed to overlap existing app bar in existing view with this view's app bar.
        ViewCompat.setElevation(this, Constants.WIDGETS_SDK_LAYER_ELEVATION)
        readTypedArray(attrs, defStyleAttr, defStyleRes)
    }

    private fun onKeyboardAnimation(insets: Insets) {
        if (rootWindowInsetsCompat?.isKeyboardVisible == false) return

        messageView?.apply {
            smoothScrollTo(0, insets.bottom.coerceAtMost(messageTitleTop))
        }
    }

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.gliaChatStyle
    ) : this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat)

    override fun setupViewAppearance() {
        // This is done to avoid view appearance when a visitor is not authenticated.
        binding = MessageCenterViewBinding.inflate(layoutInflater, this)
        SimpleWindowInsetsAndAnimationHandler(this, appBar) { onKeyboardAnimation(it) }

        controller?.ensureMessageCenterAvailability()
        setupAppBarUnifiedTheme(unifiedTheme?.secureMessagingWelcomeScreenTheme?.headerTheme)
        appBar?.hideBackButton()
        appBar?.setTitle(LocaleString(R.string.engagement_secure_messaging_title))
        initCallbacks()
    }

    private fun setupAppBarUnifiedTheme(headerTheme: HeaderTheme?) {
        appBar?.applyHeaderTheme(headerTheme)
    }

    private fun readTypedArray(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        context.withStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes) {
            setDefaultTheme(this)
        }
    }

    private fun setDefaultTheme(typedArray: TypedArray) {
        theme = Utils.getThemeFromTypedArray(typedArray, this.context)
    }

    private fun initCallbacks() {
        messageView?.apply {
            setOnCheckMessageButtonClickListener {
                clearAndDismissDialogs()
                controller?.onCheckMessagesClicked()
            }
            setOnSendMessageButtonClickListener {
                controller?.onSendMessageClicked()
            }
            setOnAttachmentButtonClickListener {
                controller?.onAddAttachmentButtonClicked()
            }
            setOnMessageTextChangedListener {
                controller?.onMessageChanged(it)
            }
            setOnRemoveAttachmentListener {
                controller?.onRemoveAttachment(it)
            }
        }
        appBar?.setOnXClickedListener {
            clearAndDismissDialogs()
            controller?.onCloseButtonClicked()

            if (messageView?.isVisible == true) {
                GliaLogger.logScWelcomeScreenButtonClicked(ButtonNames.CLOSE)
            } else {
                GliaLogger.logScConfirmationScreenButtonClicked(ButtonNames.CLOSE)
            }
        }

        confirmationView?.setOnCheckMessagesButtonClickListener {
            controller?.onCheckMessagesClicked()
        }
    }

    private fun showUnAuthenticatedDialog() {
        showDialog {
            Dialogs.showUnAuthenticatedDialog(context, theme) {
                controller?.dismissCurrentDialog()
                controller?.onCloseButtonClicked()
            }
        }
    }

    fun initialize() {
        controller?.initialize()
    }

    override fun showAttachmentPopup() {
        messageView?.showAttachmentPopup(
            { controller?.onGalleryClicked() },
            { onTakePhotoClicked() },
            { controller?.onBrowseClicked() }
        )
    }

    private fun clearAndDismissDialogs() {
        controller?.dismissDialogs()
        resetDialogStateAndDismiss()
    }

    private fun showUnexpectedErrorDialog() = showDialog {
        Dialogs.showUnexpectedErrorDialog(context, theme) {
            controller?.dismissCurrentDialog()
        }
    }

    private fun showMessageCenterUnavailableDialog() = showDialog {
        Dialogs.showMessageCenterUnavailableDialog(this.context, theme)
    }

    override fun showConfirmationScreen() {
        confirmationView?.fadeThrough(messageView!!)

        showConfirmationAppBar()
        GliaLogger.i(LogEvents.SC_WELCOME_SCREEN_CLOSED)
        GliaLogger.i(LogEvents.SC_CONFIRMATION_SCREEN_SHOWN)
    }

    private fun showConfirmationAppBar() {
        val primaryColorId = theme.brandPrimaryColor ?: R.color.glia_primary_color
        val baseLightColorId = theme.baseLightColor ?: R.color.glia_light_color

        val appBarTheme = DefaultHeader(
            ColorTheme(getColorCompat(primaryColorId)),
            ColorTheme(getColorCompat(baseLightColorId)),
            null
        ) merge unifiedTheme?.secureMessagingConfirmationScreenTheme?.headerTheme

        appBar?.resetTheme()
        setupAppBarUnifiedTheme(appBarTheme)
    }

    override fun hideSoftKeyboard() {
        insetsController?.hideKeyboard()
    }

    override fun setController(controller: MessageCenterContract.Controller) {
        this.controller = controller
        controller.setView(this)
    }

    override fun finish() {
        this.onFinishListener?.finish()
    }

    override fun navigateToMessaging() {
        this.onNavigateToMessagingListener?.navigateToMessaging()
    }

    override fun returnToLiveChat() {
        this.onNavigateToMessagingListener?.returnToLiveChat()
    }

    override fun onStateUpdated(state: MessageCenterState) {
        post { messageView?.onStateUpdated(state) }
    }

    override fun emitUploadAttachments(attachments: List<LocalAttachment>) {
        messageView?.emitUploadAttachments(attachments)
    }

    override fun selectAttachmentFile(type: String) {
        insetsController?.hideKeyboard()
        onAttachFileListener?.selectAttachmentFile(type)
    }

    override fun takePhoto(uri: Uri) {
        onAttachFileListener?.takePhoto(uri)
    }

    private fun onTakePhotoClicked() {
        insetsController?.hideKeyboard()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            onAttachFileListener?.requestCameraPermission()
        } else {
            controller?.onTakePhotoClicked()
        }

        GliaLogger.logScWelcomeScreenButtonClicked(ButtonNames.ADD_ATTACHMENT_CAMERA_OPTION)
    }

    private fun onDialogState(state: DialogState) {
        if (updateDialogState(state)) {
            when (state) {
                DialogState.None -> resetDialogStateAndDismiss()
                DialogState.UnexpectedError -> showUnexpectedErrorDialog()
                DialogState.MessageCenterUnavailable -> showMessageCenterUnavailableDialog()
                DialogState.Unauthenticated -> showUnAuthenticatedDialog()
                else -> throw UnsupportedOperationException("Unexpected dialog type for Message Center screen")
            }
        }
    }

    fun onResume() {
        attachDialogController()

        if (messageView?.isVisible == true) {
            GliaLogger.i(LogEvents.SC_WELCOME_SCREEN_SHOWN)
        } else {
            GliaLogger.i(LogEvents.SC_CONFIRMATION_SCREEN_SHOWN)
        }
    }

    fun onPause() {
        detachDialogController()

        if (messageView?.isVisible == true) {
            GliaLogger.i(LogEvents.SC_WELCOME_SCREEN_CLOSED)
        } else {
            GliaLogger.i(LogEvents.SC_CONFIRMATION_SCREEN_CLOSED)
        }
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

    interface OnFinishListener {
        fun finish()
    }

    interface OnNavigateToMessagingListener {
        fun navigateToMessaging()
        fun returnToLiveChat()
    }

    interface OnAttachFileListener {
        fun selectAttachmentFile(type: String)
        fun takePhoto(uri: Uri)
        fun requestCameraPermission()
    }

    @Parcelize
    internal class SavedState(val state: Parcelable?, val isConfirmationScreen: Boolean) :
        BaseSavedState(state)

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return if (binding == null) {
            superState
        } else {
            SavedState(superState, confirmationView!!.isVisible)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        if (state.isConfirmationScreen) showConfirmationScreen()
    }

    @VisibleForTesting
    internal var executor: Executor? = null

    override fun post(action: Runnable?): Boolean {
        return executor?.execute(action)?.let { true } ?: super.post(action)
    }
}
