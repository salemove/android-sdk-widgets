package com.glia.widgets.messagecenter

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.Group
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.chat.AttachmentPopup
import com.glia.widgets.chat.adapter.UploadAttachmentAdapter
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.databinding.MessageCenterMessageViewBinding
import com.glia.widgets.view.unifiedui.extensions.getColorCompat
import com.glia.widgets.view.unifiedui.extensions.layoutInflater
import com.glia.widgets.view.unifiedui.extensions.wrapWithMaterialThemeOverlay
import com.google.android.material.button.MaterialButton
import kotlin.properties.Delegates

class MessageView(
    context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
) : ScrollView(
    context.wrapWithMaterialThemeOverlay(attrs, defStyleAttr, defStyleRes),
    attrs,
    defStyleAttr,
    defStyleRes
) {

    private val binding: MessageCenterMessageViewBinding by lazy {
        MessageCenterMessageViewBinding.inflate(layoutInflater, this)
    }

    private val scrollContainer: View get() = binding.scrollContainer
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

    private val attachmentPopup by lazy { AttachmentPopup(addAttachmentButton) }

    private var checkMessageButtonClickListener: OnClickListener? = null
    private var attachmentButtonClickListener: OnClickListener? = null
    private var sendMessageButtonClickListener: OnClickListener? = null
    private var onMessageTextChangedListener: ((String) -> Unit)? = null
    private var onRemoveAttachmentListener: ((FileAttachment) -> Unit)? = null

    private var uploadAttachmentAdapter by Delegates.notNull<UploadAttachmentAdapter>()

    init {
        isFillViewport = true
        setupViewAppearance()
        handleScrollView()
        initCallbacks()
        setupTheme()
    }

    @JvmOverloads
    constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.gliaChatStyle
    ) : this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat)

    private fun setupViewAppearance() {
        uploadAttachmentAdapter = UploadAttachmentAdapter(isMessageCenter = true)
        uploadAttachmentAdapter.setItemCallback {
            onRemoveAttachmentListener?.invoke(it)
        }
        uploadAttachmentAdapter.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                attachmentRecyclerView.smoothScrollToPosition(uploadAttachmentAdapter.itemCount)
            }
        })
        attachmentRecyclerView.layoutManager = LinearLayoutManager(this.context)
        attachmentRecyclerView.adapter = uploadAttachmentAdapter
    }

    private fun setupTheme() {
        val normalColor = getColorCompat(R.color.glia_base_normal_color)
        val disabledColor = getColorCompat(R.color.glia_base_shade_color)

        val colors: MutableList<Int> = mutableListOf()
        val states: MutableList<IntArray> = mutableListOf()

        val disabledState = intArrayOf(-android.R.attr.state_enabled)
        val enabledState = intArrayOf()

        colors.add(disabledColor)
        states.add(disabledState)

        colors.add(normalColor)
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

    private fun handleScrollView() {
        var scrollContainerHeight = 0
        scrollContainer.viewTreeObserver.addOnGlobalLayoutListener {
            val scrollViewWidth = width
            val scrollViewHeight = height

            // Set to the container the size of the scroll view.
            // 1. Ignore if the scroll view height is less than the value that was previous set.
            // 2. Skip if the content height is more than the scroll view height (small screen/landscape mode)
            if (
                scrollContainerHeight < scrollViewHeight && // 1
                scrollContainer.height <= scrollViewHeight // 2
            ) {
                // Set new layout params.
                scrollContainer.layoutParams = LayoutParams(scrollViewWidth, scrollViewHeight)

                // Remember the maximum size to prevent cyclically layout params updates.
                scrollContainerHeight = scrollViewHeight
            }

            // Scroll to the top of the send message group when keyboard appears.
            // 1. Ignore if the content is more than scroll view (landscape mode). In this case, Android
            //    will automatically scroll to the edit text view. Or will use a fullscreen keyboard.
            // 2. Check possible keyboard appearing.
            if (
                scrollContainerHeight > 0 && // 1
                scrollContainer.height > scrollViewHeight // 2
            ) {
                post { smoothScrollTo(0, addAttachmentButton.top) }
            }
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

    private fun showSendMessageGroup() {
        sendMessageGroup.isVisible = true
    }

    private fun hideSendMessageGroup() {
        sendMessageGroup.isInvisible = true
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

    fun setOnRemoveAttachmentListener(listener: (FileAttachment) -> Unit) {
        onRemoveAttachmentListener = listener
    }

    fun onStateUpdated(state: State) {
        updateSendButtonState(state.sendMessageButtonState)
        updateSendMessageError(state.showMessageLimitError)
        updateMessageEditText(state.messageEditTextEnabled, state.showMessageLimitError)
        if (state.showSendMessageGroup) {
            showSendMessageGroup()
            if (state.addAttachmentButtonVisible) {
                addAttachmentButton.visibility = VISIBLE
                addAttachmentButton.isEnabled = state.addAttachmentButtonEnabled
            } else {
                addAttachmentButton.visibility = GONE
            }
        } else {
            hideSendMessageGroup()
        }
    }

    fun emitUploadAttachments(attachments: List<FileAttachment>) {
        post {
            uploadAttachmentAdapter.submitList(attachments)
            if (attachments.isEmpty()) {
                bottomSpace.visibility = VISIBLE
            } else {
                bottomSpace.visibility = GONE
            }
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

    private fun updateSendButtonState(state: State.ButtonState) {
        when (state) {
            State.ButtonState.PROGRESS -> sendMessageButton.setProgress(true)
            State.ButtonState.NORMAL -> sendMessageButton.isEnabled = true
            State.ButtonState.DISABLE -> sendMessageButton.isEnabled = false
        }
    }
}
