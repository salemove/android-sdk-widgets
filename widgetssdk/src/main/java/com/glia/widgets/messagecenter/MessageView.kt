package com.glia.widgets.messagecenter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.Group
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.chat.AttachmentPopup
import com.glia.widgets.databinding.MessageCenterMessageViewBinding
import com.glia.widgets.view.unifiedui.exstensions.layoutInflater
import com.google.android.material.button.MaterialButton
import com.google.android.material.theme.overlay.MaterialThemeOverlay

class MessageView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : ScrollView(
    MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, defStyleRes),
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
    private val sendMessageButton: MaterialButton get() = binding.btnSendMessage
    private val messageTitle: TextView get() = binding.messageTitle
    private val messageEditText: EditText get() = binding.messageEditText
    private val addAttachmentButton: ImageButton get() = binding.addAttachmentButton
    private val attachmentRecyclerView: RecyclerView get() = binding.attachmentsRecyclerView
    private val sendMessageGroup: Group get() = binding.sendMessageGroup

    private val attachmentPopup by lazy { AttachmentPopup(addAttachmentButton) }

    private var checkMessageButtonClickListener: OnClickListener? = null
    private var attachmentButtonClickListener: OnClickListener? = null
    private var sendMessageButtonClickListener: ((String) -> Unit)? = null

    init {
        isFillViewport = true
        handleScrollView()
        initCallbacks()
    }

    @JvmOverloads
    constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.gliaChatStyle
    ) : this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat)

    private fun initCallbacks() {
        checkMessagesButton.setOnClickListener {
            checkMessageButtonClickListener?.onClick(it)
        }
        sendMessageButton.setOnClickListener {
            sendMessageButtonClickListener?.invoke(messageEditText.text.toString())
        }

        addAttachmentButton.setOnClickListener {
            attachmentButtonClickListener?.onClick(it)
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

    fun showSendMessageGroup() {
        sendMessageGroup.isVisible = true
    }

    fun hideSendMessageGroup() {
        sendMessageGroup.isInvisible = true
    }

    fun setOnCheckMessageButtonClickListener(listener: OnClickListener) {
        checkMessageButtonClickListener = listener
    }

    fun setOnAttachmentButtonClickListener(listener: OnClickListener) {
        checkMessageButtonClickListener = listener
    }

    fun setOnSendMessageButtonClickListener(listener: (String) -> Unit) {
        sendMessageButtonClickListener = listener
    }

}