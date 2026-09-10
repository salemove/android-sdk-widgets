package com.glia.widgets.view.head

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.VisibleForTesting
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.isVisible
import com.glia.widgets.R
import com.glia.widgets.chat.Intention
import com.glia.widgets.databinding.ChatHeadViewBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.addColorFilter
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getColorStateListCompat
import com.glia.widgets.helper.gliaAttrDrawableRes
import com.glia.widgets.helper.gliaAttrResourceId
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.helper.load
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.internal.chathead.BubbleContent
import com.glia.widgets.internal.chathead.BubbleUiModel
import com.glia.widgets.launcher.ActivityLauncher
import com.glia.widgets.view.unifiedui.applyColorTheme
import com.glia.widgets.view.unifiedui.applyImageColorTheme
import com.glia.widgets.view.unifiedui.theme.bubble.BubbleTheme
import com.glia.widgets.view.unifiedui.theme.chat.UserImageTheme
import java.util.concurrent.Executor

internal class ChatHeadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(
    context,
    attrs,
    defStyleAttr
) {
    private val activityLauncher: ActivityLauncher by lazy { Dependencies.activityLauncher }
    private val binding by lazy { ChatHeadViewBinding.inflate(layoutInflater, this) }

    /*
     * The bubble is repainted per state (placeholder / queueing / operator picture), so these are
     * resolved on every read instead of being captured once at inflation.
     */
    @get:ColorRes
    private val brandPrimaryColorRes: Int
        get() = context.gliaAttrResourceId(R.attr.gliaBrandPrimaryColor, R.color.glia_primary_color)

    @get:ColorRes
    private val baseLightColorRes: Int
        get() = context.gliaAttrResourceId(R.attr.gliaBaseLightColor, R.color.glia_light_color)

    @get:DrawableRes
    private val placeholderIconRes: Int
        get() = context.gliaAttrDrawableRes(R.attr.gliaIconPlaceholder, R.drawable.ic_person)

    @get:DrawableRes
    private val onHoldIconRes: Int
        get() = context.gliaAttrDrawableRes(R.attr.gliaIconOnHold, R.drawable.ic_pause_circle)

    private var isOnChatScreen: Boolean = false

    /**
     * The bubble on the chat screen is part of the chat UI, so it is themed by `chatTheme.bubble`.
     * Everywhere else — any other screen and the overlay — it is the standalone bubble, themed by the
     * top-level `bubbleTheme`. The screen, not the host, decides: the model carries the answer.
     */
    private val bubbleTheme: BubbleTheme?
        get() = Dependencies.gliaThemeManager.theme?.run { if (isOnChatScreen) chatTheme?.bubble else bubbleTheme }

    init {
        setAccessibilityLabels()
        post { updateView() }
        updateView()
    }

    /**
     * Draws the model the controller published. The bubble applies no rules of its own —
     * `state.unreadCount` already accounts for engagements that must not show a badge.
     */
    fun render(state: BubbleUiModel) {
        applyThemeForScreen(state.isOnChatScreen)

        when (val content = state.content) {
            is BubbleContent.Engaged -> content.operatorImageUrl?.also(::showOperatorImage) ?: showPlaceholder()
            BubbleContent.Queueing -> showQueueing()
            BubbleContent.Ended -> showPlaceholder()
        }

        if (state.isOnHold) showOnHold() else hideOnHold()
        showUnreadMessageCount(state.unreadCount)
    }

    /**
     * Repaints from scratch when the screen kind changes, because the two themes are applied on top of
     * the base colours and would otherwise blend into each other.
     */
    private fun applyThemeForScreen(isOnChatScreen: Boolean) {
        if (this.isOnChatScreen == isOnChatScreen) return
        this.isOnChatScreen = isOnChatScreen
        updateView()
    }

    fun showUnreadMessageCount(count: Int) {
        post {
            binding.chatBubbleBadge.apply {
                text = count.toString()
                isVisible = isDisplayUnreadMessageBadge(count)
                ChatHeadLogger.logUnreadMessageCountChanged(count.toString())
            }
        }
    }

    fun showOperatorImage(operatorImgUrl: String) {
        post {
            binding.apply {
                queueingLottieAnimation.visibility = GONE
                queueingLottieAnimationPlaceholder.visibility = GONE
                placeholderView.visibility = GONE
                profilePictureView.load(operatorImgUrl)
            }
        }
    }

    fun showPlaceholder() {
        post {
            binding.apply {
                queueingLottieAnimation.visibility = GONE
                queueingLottieAnimationPlaceholder.visibility = GONE
                profilePictureView.setImageDrawable(null)
                profilePictureView.backgroundTintList = getColorStateListCompat(brandPrimaryColorRes)
                placeholderView.setImageResource(placeholderIconRes)
                placeholderView.visibility = VISIBLE
            }
        }
    }

    fun showQueueing() {
        post {
            binding.apply {
                placeholderView.visibility = GONE
                profilePictureView.setImageDrawable(null)
                profilePictureView.backgroundTintList = getColorStateListCompat(baseLightColorRes)
                queueingLottieAnimation.visibility = VISIBLE
                queueingLottieAnimationPlaceholder.visibility = VISIBLE
            }
        }
        ChatHeadLogger.logEnqueueingStarted()
    }

    fun showOnHold() {
        post { binding.onHoldIcon.visibility = VISIBLE }
        ChatHeadLogger.logOnHold()
    }

    fun hideOnHold() {
        post { binding.onHoldIcon.visibility = GONE }
    }

    private fun applyBubbleTheme() {
        bubbleTheme?.badge?.also(binding.chatBubbleBadge::applyBadgeTheme)
        bubbleTheme?.onHoldOverlay?.also {
            binding.onHoldIcon.setLocaleContentDescription(R.string.android_call_on_hold_icon_accessibility)
            it.tintColor.also(binding.onHoldIcon::applyImageColorTheme)
            it.backgroundColor?.primaryColorStateList?.also(binding.onHoldIcon::setBackgroundTintList)
        }
        bubbleTheme?.userImage?.also(::applyUserImageTheme)
    }

    private fun applyUserImageTheme(userImageTheme: UserImageTheme?) {
        userImageTheme?.imageBackgroundColor.also(binding.profilePictureView::applyColorTheme)
        userImageTheme?.placeholderBackgroundColor.also(binding.placeholderView::applyColorTheme)
        userImageTheme?.placeholderBackgroundColor.also(binding.queueingLottieAnimationPlaceholder::applyColorTheme)
        userImageTheme?.placeholderColor.also(binding.placeholderView::applyImageColorTheme)
        userImageTheme?.placeholderColor.also(binding.queueingLottieAnimationPlaceholder::applyImageColorTheme)
    }

    fun navigateToChat() {
        activityLauncher.launchChat(context, Intention.RETURN_TO_CHAT)
    }

    fun navigateToCall() {
        activityLauncher.launchCall(context, null, false)
    }

    private fun setAccessibilityLabels() {
        val view = binding.root
        view.isFocusable = true
        view.setLocaleContentDescription(R.string.android_bubble_accessibility)
        ViewCompat.setAccessibilityDelegate(
            view,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(
                    host: View,
                    info: AccessibilityNodeInfoCompat
                ) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.className = Button::class.java.name
                    info.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK)
                }
            }
        )
    }

    private fun updatePlaceholderImageView() {
        binding.placeholderView.apply {
            setImageResource(placeholderIconRes)
            setBackgroundColor(getColorCompat(brandPrimaryColorRes))
            imageTintList = getColorStateListCompat(baseLightColorRes)
        }
        binding.queueingLottieAnimationPlaceholder.apply {
            setImageResource(placeholderIconRes)
            setBackgroundColor(getColorCompat(brandPrimaryColorRes))
            imageTintList = getColorStateListCompat(baseLightColorRes)
        }
    }

    private fun updateOnHoldImageView() {
        binding.onHoldIcon.apply {
            setImageResource(onHoldIconRes)
            imageTintList = getColorStateListCompat(baseLightColorRes)
        }
    }

    private fun updateBadgeView() {
        binding.chatBubbleBadge.apply {
            backgroundTintList = getColorStateListCompat(brandPrimaryColorRes)
            setTextColor(getColorCompat(baseLightColorRes))
        }
    }

    private fun updateProfilePictureView() {
        binding.profilePictureView.setBackgroundColor(getColorCompat(brandPrimaryColorRes))
    }

    private fun updateQueueingAnimationView() {
        binding.queueingLottieAnimation.addColorFilter(color = getColorCompat(brandPrimaryColorRes))
    }

    private fun updateView() {
        updatePlaceholderImageView()
        updateOnHoldImageView()
        updateBadgeView()
        updateProfilePictureView()
        updateQueueingAnimationView()

        applyBubbleTheme()
        invalidate()
    }

    private fun isDisplayUnreadMessageBadge(unreadMessageCount: Int): Boolean = unreadMessageCount > 0

    @VisibleForTesting
    internal var executor: Executor? = null

    override fun post(action: Runnable?): Boolean {
        return executor?.execute(action)?.let { true } ?: super.post(action)
    }

    companion object {
        @JvmStatic
        fun getInstance(context: Context): ChatHeadView = ChatHeadView(context)
    }
}
