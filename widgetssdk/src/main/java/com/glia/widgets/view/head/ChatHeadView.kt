package com.glia.widgets.view.head

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.isVisible
import com.glia.widgets.GliaWidgets
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.call.CallActivityIntentHelper
import com.glia.widgets.call.CallConfiguration
import com.glia.widgets.callvisualizer.EndScreenSharingActivity
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.core.callvisualizer.domain.IsCallVisualizerScreenSharingUseCase
import com.glia.widgets.core.configuration.EngagementConfiguration
import com.glia.widgets.databinding.ChatHeadViewBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.helper.Utils
import com.glia.widgets.helper.addColorFilter
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getColorStateListCompat
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.helper.load
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.view.configuration.ChatHeadConfiguration
import com.glia.widgets.view.unifiedui.applyColorTheme
import com.glia.widgets.view.unifiedui.applyImageColorTheme
import com.glia.widgets.view.unifiedui.theme.bubble.BubbleTheme
import com.glia.widgets.view.unifiedui.theme.chat.UserImageTheme
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import java.util.concurrent.Executor
import kotlin.properties.Delegates

internal class ChatHeadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.gliaChatStyle,
    defStyleRes: Int = R.style.Application_Glia_Chat
) : ConstraintLayout(
    MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, defStyleRes),
    attrs,
    defStyleAttr,
    defStyleRes
), ChatHeadContract.View {
    private val binding by lazy { ChatHeadViewBinding.inflate(layoutInflater, this) }
    private var engagementConfiguration: EngagementConfiguration? = null
    private var configuration: ChatHeadConfiguration by Delegates.notNull()

    private val isService by lazy { context is Service }

    private val bubbleTheme: BubbleTheme?
        get() = Dependencies.gliaThemeManager.theme?.run {
            if (isService) bubbleTheme else chatTheme?.bubble
        }

    @Suppress("JoinDeclarationAndAssignment")
    private var serviceChatHeadController: ChatHeadContract.Controller
    private var isCallVisualizerScreenSharingUseCase: IsCallVisualizerScreenSharingUseCase
    private var isCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase
    private var theme: UiTheme? = null

    init {
        serviceChatHeadController = Dependencies.controllerFactory.chatHeadController
        isCallVisualizerScreenSharingUseCase = Dependencies.useCaseFactory.createIsCallVisualizerScreenSharingUseCase()
        isCallVisualizerUseCase = Dependencies.useCaseFactory.isCurrentEngagementCallVisualizer
        setAccessibilityLabels()
        readTypedArray()
    }

    private fun readTypedArray() {
        context.withStyledAttributes(
            set = null,
            attrs = R.styleable.GliaView,
            defStyleAttr = 0
        ) {
            setDefaultTheme(this)
        }
    }
    private fun setDefaultTheme(typedArray: TypedArray) {
        theme = Utils.getThemeFromTypedArray(typedArray, this.context)
        serviceChatHeadController.setBuildTimeTheme(theme)
    }

    override fun showUnreadMessageCount(unreadMessageCount: Int) {
        post {
            if (isCallVisualizerUseCase.invoke()) {
                binding.chatBubbleBadge.isVisible = false
            } else {
                binding.chatBubbleBadge.apply {
                    text = unreadMessageCount.toString()
                    isVisible = isDisplayUnreadMessageBadge(unreadMessageCount)
                }
            }
        }
    }

    override fun setController(controller: ChatHeadContract.Controller) {
        // Unused
    }

    override fun showOperatorImage(operatorProfileImgUrl: String) {
        post {
            binding.apply {
                queueingLottieAnimation.visibility = GONE
                placeholderView.visibility = GONE
                profilePictureView.load(operatorProfileImgUrl)
            }
        }
    }

    override fun showPlaceholder() {
        post {
            binding.apply {
                queueingLottieAnimation.visibility = GONE
                profilePictureView.setImageDrawable(null)
                profilePictureView.backgroundTintList = getColorStateListCompat(configuration.backgroundColorRes)
                placeholderView.setImageResource(configuration.operatorPlaceholderIcon)
                placeholderView.visibility = VISIBLE
            }
        }
    }

    override fun showQueueing() {
        post {
            binding.apply {
                placeholderView.visibility = GONE
                profilePictureView.setImageDrawable(null)
                profilePictureView.backgroundTintList = getColorStateListCompat(configuration.badgeTextColor)
                queueingLottieAnimation.visibility = VISIBLE
            }
        }
    }

    override fun showScreenSharing() {
        post {
            binding.apply {
                placeholderView.visibility = GONE
                profilePictureView.setImageDrawable(null)
                profilePictureView.backgroundTintList = getColorStateListCompat(configuration.backgroundColorRes)
                placeholderView.setImageResource(configuration.iconScreenSharingDialog)
                placeholderView.visibility = VISIBLE
            }
        }
    }

    override fun showOnHold() {
        post { binding.onHoldIcon.visibility = VISIBLE }
    }

    override fun hideOnHold() {
        post { binding.onHoldIcon.visibility = GONE }
    }

    override fun updateConfiguration(
        buildTimeTheme: UiTheme,
        engagementConfiguration: EngagementConfiguration?
    ) {
        this.engagementConfiguration = engagementConfiguration
        serviceChatHeadController.setBuildTimeTheme(buildTimeTheme)
        createHybridConfiguration(buildTimeTheme, engagementConfiguration)
        post { updateView() }
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
        userImageTheme?.placeholderColor.also(binding.placeholderView::applyImageColorTheme)
    }

    override fun navigateToChat() {
        engagementConfiguration?.let {
            context.startActivity(getNavigationIntent(context, ChatActivity::class.java, it))
        }
    }

    override fun navigateToCall() {
        val activityConfig =
            CallConfiguration.Builder().setEngagementConfiguration(engagementConfiguration).build()

        val intent = CallActivityIntentHelper.createIntent(context, activityConfig)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    override fun navigateToEndScreenSharing() {
        val intent = Intent(context, EndScreenSharingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    private fun createBuildTimeConfiguration(buildTimeTheme: UiTheme): ChatHeadConfiguration {
        return ChatHeadConfiguration.builder()
            .operatorPlaceholderBackgroundColor(buildTimeTheme.brandPrimaryColor)
            .operatorPlaceholderIcon(buildTimeTheme.iconPlaceholder)
            .operatorPlaceholderIconTintList(buildTimeTheme.baseLightColor)
            .badgeTextColor(buildTimeTheme.baseLightColor)
            .badgeBackgroundTintList(buildTimeTheme.brandPrimaryColor)
            .backgroundColorRes(buildTimeTheme.brandPrimaryColor)
            .iconOnHold(buildTimeTheme.iconOnHold)
            .iconOnHoldTintList(buildTimeTheme.baseLightColor)
            .iconScreenSharingDialog(buildTimeTheme.iconScreenSharingDialog)
            .build()
    }

    private fun createHybridConfiguration(
        buildTimeTheme: UiTheme,
        engagementConfiguration: com.glia.widgets.core.configuration.EngagementConfiguration?
    ) {
        configuration = createBuildTimeConfiguration(buildTimeTheme)
        val runTimeTheme = engagementConfiguration?.runTimeTheme ?: return

        val builder = ChatHeadConfiguration.builder(configuration)

        runTimeTheme.chatHeadConfiguration?.apply {
            operatorPlaceholderBackgroundColor?.also(builder::operatorPlaceholderBackgroundColor)
            operatorPlaceholderIcon?.also(builder::operatorPlaceholderIcon)
            operatorPlaceholderIconTintList?.also(builder::operatorPlaceholderIconTintList)
            badgeBackgroundTintList?.also(builder::badgeBackgroundTintList)
            badgeTextColor?.also(builder::badgeTextColor)
            backgroundColorRes?.also(builder::backgroundColorRes)
            iconOnHold?.also(builder::iconOnHold)
            iconOnHoldTintList?.also(builder::iconOnHoldTintList)
            iconScreenSharingDialog?.also(builder::iconScreenSharingDialog)
        }
        configuration = builder.build()
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
                    info.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK)
                }
            }
        )
    }

    private fun updatePlaceholderImageView() {
        val placeholderIcon = if (isCallVisualizerScreenSharingUseCase()) {
            configuration.iconScreenSharingDialog
        } else {
            configuration.operatorPlaceholderIcon
        }
        binding.placeholderView.apply {
            setImageResource(placeholderIcon)
            setBackgroundColor(getColorCompat(configuration.operatorPlaceholderBackgroundColor))
            imageTintList = getColorStateListCompat(configuration.operatorPlaceholderIconTintList)
        }
    }

    private fun updateOnHoldImageView() {
        binding.onHoldIcon.apply {
            setImageResource(configuration.iconOnHold)
            imageTintList = getColorStateListCompat(configuration.iconOnHoldTintList)
        }
    }

    private fun updateBadgeView() {
        binding.chatBubbleBadge.apply {
            backgroundTintList = getColorStateListCompat(configuration.badgeBackgroundTintList)
            setTextColor(getColorCompat(configuration.badgeTextColor))
        }
    }

    private fun updateProfilePictureView() {
        binding.profilePictureView.setBackgroundColor(getColorCompat(configuration.backgroundColorRes))
    }

    private fun updateQueueingAnimationView() {
        binding.queueingLottieAnimation.addColorFilter(
            color = getColorCompat(configuration.backgroundColorRes),
            mode = PorterDuff.Mode.SRC_OVER
        )
    }

    private fun updateView() {
        updatePlaceholderImageView()
        updateOnHoldImageView()
        updateBadgeView()
        updateProfilePictureView()
        updateQueueingAnimationView()

        applyBubbleTheme()
    }

    private fun isDisplayUnreadMessageBadge(unreadMessageCount: Int): Boolean =
        unreadMessageCount > 0

    @VisibleForTesting
    internal var executor: Executor? = null

    override fun post(action: Runnable?): Boolean {
        return executor?.execute(action)?.let { true } ?: super.post(action)
    }

    companion object {
        @JvmStatic
        fun getInstance(context: Context): ChatHeadView = ChatHeadView(context)

        @JvmStatic
        private fun getNavigationIntent(
            context: Context,
            cls: Class<*>,
            engagementConfiguration: EngagementConfiguration
        ): Intent = Intent(context, cls)
            .putExtra(GliaWidgets.QUEUE_IDS, engagementConfiguration.queueIds?.let { ArrayList(it) })
            .putExtra(GliaWidgets.CONTEXT_ASSET_ID, engagementConfiguration.contextAssetId)
            .putExtra(GliaWidgets.UI_THEME, engagementConfiguration.runTimeTheme)
            .putExtra(GliaWidgets.SCREEN_SHARING_MODE, engagementConfiguration.screenSharingMode)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
