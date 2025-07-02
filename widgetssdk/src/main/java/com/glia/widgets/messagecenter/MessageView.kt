package com.glia.widgets.messagecenter

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Space
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.constraintlayout.widget.Group
import androidx.core.content.withStyledAttributes
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.TextViewCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.AttachmentPopup
import com.glia.widgets.chat.adapter.UploadAttachmentAdapter
import com.glia.widgets.databinding.MessageCenterMessageViewBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Utils
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getColorStateListCompat
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.helper.setCompoundDrawableTintListCompat
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.helper.setLocaleHint
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.helper.wrapWithMaterialThemeOverlay
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.glia.widgets.view.unifiedui.applyColorTheme
import com.glia.widgets.view.unifiedui.applyImageColorTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.securemessaging.SecureMessagingWelcomeScreenTheme
import com.google.android.material.button.MaterialButton
import java.util.concurrent.Executor
import kotlin.properties.Delegates

internal class MessageView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : NestedScrollView(
    context.wrapWithMaterialThemeOverlay(attrs, defStyleAttr, defStyleRes),
    attrs,
    defStyleAttr
) {

    private val binding: MessageCenterMessageViewBinding by lazy {
        MessageCenterMessageViewBinding.inflate(layoutInflater, this)
    }

    private val icon: ImageView get() = binding.icon
    private val title: TextView get() = binding.title
    private val description: TextView get() = binding.description
    private val checkMessagesButton: MaterialButton get() = binding.btnCheckMessages
    private val sendMessageButton: ProgressButton get() = binding.btnSendMessage
    private val messageTitle: TextView get() = binding.messageTitle
    private val messageEditText: StatefulEditText get() = binding.messageEditText
    private val messageErrorTextView: TextView get() = binding.errorTextView
    private val addAttachmentButton: ImageButton get() = binding.addAttachmentButton
    private val attachmentRecyclerView: RecyclerView get() = binding.attachmentsRecyclerView
    private val sendMessageGroup: Group get() = binding.sendMessageGroup
    private val bottomSpace: Space get() = binding.bottomSpace

    private val unifiedTheme: SecureMessagingWelcomeScreenTheme? by lazy {
        Dependencies.gliaThemeManager.theme?.secureMessagingWelcomeScreenTheme
    }

    private val attachmentPopup by lazy {
        AttachmentPopup(context, unifiedTheme?.pickMediaTheme)
    }

    val messageTitleTop: Int get() = messageTitle.top

    private var checkMessageButtonClickListener: OnClickListener? = null
    private var attachmentButtonClickListener: OnClickListener? = null
    private var sendMessageButtonClickListener: OnClickListener? = null
    private var onMessageTextChangedListener: ((String) -> Unit)? = null
    private var onRemoveAttachmentListener: ((LocalAttachment) -> Unit)? = null

    private var uploadAttachmentAdapter by Delegates.notNull<UploadAttachmentAdapter>()

    init {
        isFillViewport = true
        isNestedScrollingEnabled = false
        overScrollMode = OVER_SCROLL_NEVER
        setBackgroundColor(getColorCompat(R.color.glia_chat_background_color))
        setupViewAppearance()
        initCallbacks()
        readTypedArray(attrs, defStyleAttr, defStyleRes)
        setupUnifiedTheme()
    }

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.gliaChatStyle
    ) : this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat)

    private fun readTypedArray(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        messageErrorTextView.setLocaleText(R.string.message_center_welcome_message_length_error)
        context.withStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes) {
            setDefaultTheme(this)
        }
    }

    private fun setDefaultTheme(typedArray: TypedArray) {
        val theme = Utils.getThemeFromTypedArray(typedArray, this.context)
        setupAttachmentIconTheme(theme)
        setupMessageErrorTextTheme(theme)
    }

    private fun setupMessageErrorTextTheme(theme: UiTheme) {
        val systemNegativeColorId = theme.systemNegativeColor ?: return

        TextViewCompat.setCompoundDrawableTintList(
            messageErrorTextView,
            getColorStateListCompat(systemNegativeColorId)
        )

        messageErrorTextView.setTextColor(getColorCompat(systemNegativeColorId))
    }

    private fun setupUnifiedTheme() {
        unifiedTheme?.apply {
            applyColorTheme(backgroundTheme)
            icon.applyImageColorTheme(titleImageTheme)
            title.applyTextTheme(welcomeTitleTheme, withAlignment = false)
            description.applyTextTheme(welcomeSubtitleTheme, withAlignment = false)
            checkMessagesButton.applyTextTheme(checkMessagesButtonTheme, withAlignment = false)
            messageTitle.applyTextTheme(messageTitleTheme, withAlignment = false)
            messageErrorTextView.applyTextTheme(messageWarningTheme, withAlignment = false)
            messageErrorTextView.setCompoundDrawableTintListCompat(messageWarningIconColorTheme?.primaryColorStateList)
            messageEditText.updateStatefulTheme(
                mapOf(
                    StatefulEditText.State.ENABLED to messageInputNormalTheme,
                    StatefulEditText.State.FOCUSED to messageInputActiveTheme,
                    StatefulEditText.State.DISABLED to messageInputDisabledTheme,
                    StatefulEditText.State.ERROR to messageInputErrorTheme
                )
            )
            sendMessageButton.updateProgressTheme(activityIndicatorColorTheme)

            sendMessageButton.updateStatefulTheme(
                mapOf(
                    ProgressButton.State.ENABLED to enabledSendButtonTheme,
                    ProgressButton.State.DISABLED to disabledSendButtonTheme,
                    ProgressButton.State.PROGRESS to loadingSendButtonTheme
                )
            )
            messageEditText.updateHintTheme(messageInputHintTheme)
        }
    }

    private fun setupViewAppearance() {
        title.setLocaleText(R.string.message_center_welcome_title)
        description.setLocaleText(R.string.message_center_welcome_subtitle)
        checkMessagesButton.setLocaleText(R.string.message_center_welcome_check_messages)
        checkMessagesButton.setLocaleContentDescription(R.string.message_center_welcome_check_messages_accessibility_hint)
        messageTitle.setLocaleText(R.string.message_center_welcome_message_title)
        messageEditText.setLocaleHint(R.string.message_center_welcome_message_input_placeholder)
        addAttachmentButton.setLocaleContentDescription(R.string.message_center_welcome_file_picker_accessibility_label)
        uploadAttachmentAdapter = UploadAttachmentAdapter(isMessageCenter = true)
        uploadAttachmentAdapter.setItemCallback { onRemoveAttachmentListener?.invoke(it) }
        uploadAttachmentAdapter.registerAdapterDataObserver(
            object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    attachmentRecyclerView.smoothScrollToPosition(uploadAttachmentAdapter.itemCount)
                }
            }
        )
        attachmentRecyclerView.layoutManager = LinearLayoutManager(this.context)
        attachmentRecyclerView.adapter = uploadAttachmentAdapter
    }

    private fun setupAttachmentIconTheme(theme: UiTheme) {
        val normalColor = unifiedTheme?.filePickerButtonTheme?.primaryColor
            ?: theme.baseNormalColor?.let { getColorCompat(it) }
        val disabledColor = unifiedTheme?.filePickerButtonDisabledTheme?.primaryColor
            ?: theme.baseShadeColor?.let { getColorCompat(it) }

        val colors: MutableList<Int> = mutableListOf()
        val states: MutableList<IntArray> = mutableListOf()

        val disabledState = intArrayOf(-android.R.attr.state_enabled)
        val enabledState = intArrayOf()

        colors.add(disabledColor ?: return)
        states.add(disabledState)

        colors.add(normalColor ?: return)
        states.add(enabledState)

        addAttachmentButton.imageTintList =
            ColorStateList(states.toTypedArray(), colors.toIntArray())
    }

    private fun initCallbacks() {
        checkMessagesButton.setOnClickListener {
            checkMessageButtonClickListener?.onClick(it)
        }
        sendMessageButton.setOnClickListener {
            sendMessageButtonClickListener?.onClick(it)
        }

        addAttachmentButton.setOnClickListener {
            attachmentButtonClickListener?.onClick(it)
        }

        messageEditText.doAfterTextChanged {
            onMessageTextChangedListener?.invoke(it.toString())
        }
    }

    fun showAttachmentPopup(
        onGalleryClicked: () -> Unit,
        onTakePhotoClicked: () -> Unit,
        onBrowseClicked: () -> Unit
    ) {
        attachmentPopup.show(
            addAttachmentButton,
            onGalleryClicked,
            onTakePhotoClicked,
            onBrowseClicked
        )
    }

    private fun showSendMessageGroup(state: MessageCenterState) {
        sendMessageGroup.isVisible = true

        if (state.addAttachmentButtonVisible) {
            addAttachmentButton.visibility = VISIBLE
            addAttachmentButton.isEnabled = state.addAttachmentButtonEnabled
        } else {
            addAttachmentButton.visibility = GONE
        }
    }

    private fun hideSendMessageGroup() {
        sendMessageGroup.isInvisible = true
        addAttachmentButton.visibility = GONE
    }

    fun setOnCheckMessageButtonClickListener(listener: OnClickListener) {
        checkMessageButtonClickListener = listener
    }

    fun setOnAttachmentButtonClickListener(listener: OnClickListener) {
        attachmentButtonClickListener = listener
    }

    fun setOnSendMessageButtonClickListener(listener: OnClickListener) {
        sendMessageButtonClickListener = listener
    }

    fun setOnMessageTextChangedListener(listener: (String) -> Unit) {
        onMessageTextChangedListener = listener
    }

    fun setOnRemoveAttachmentListener(listener: (LocalAttachment) -> Unit) {
        onRemoveAttachmentListener = listener
    }

    fun onStateUpdated(state: MessageCenterState) {
        updateSendButtonState(state.sendMessageButtonState)
        updateSendMessageError(state.showMessageLimitError)
        updateMessageEditText(state.messageEditTextEnabled, state.showMessageLimitError)
        updateSendMessageGroup(state)
    }

    fun emitUploadAttachments(attachments: List<LocalAttachment>) {
        post {
            uploadAttachmentAdapter.submitList(attachments)
            if (attachments.isEmpty()) {
                bottomSpace.visibility = VISIBLE
            } else {
                bottomSpace.visibility = GONE
            }
        }
    }

    private fun updateSendMessageGroup(state: MessageCenterState) {
        if (state.showSendMessageGroup) {
            showSendMessageGroup(state)
        } else {
            hideSendMessageGroup()
        }
    }

    private fun updateSendMessageError(showError: Boolean) {
        if (showError) {
            messageErrorTextView.visibility = VISIBLE
        } else {
            messageErrorTextView.visibility = GONE
        }
    }

    private fun updateMessageEditText(isEnabled: Boolean, showError: Boolean) {
        messageEditText.isEnabled = isEnabled
        messageEditText.setError(showError)
    }

    private fun updateSendButtonState(state: MessageCenterState.ButtonState) {
        when (state) {
            MessageCenterState.ButtonState.PROGRESS -> sendMessageButton.setProgress(true)
            MessageCenterState.ButtonState.NORMAL -> sendMessageButton.isEnabled = true
            MessageCenterState.ButtonState.DISABLE -> sendMessageButton.isEnabled = false
        }
    }

    @VisibleForTesting
    internal var executor: Executor? = null

    override fun post(action: Runnable?): Boolean {
        return executor?.execute(action)?.let { true } ?: super.post(action)
    }
}
