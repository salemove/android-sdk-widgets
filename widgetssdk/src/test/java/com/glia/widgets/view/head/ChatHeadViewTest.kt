package com.glia.widgets.view.head

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import androidx.annotation.ColorRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.isVisible
import com.glia.widgets.R
import com.glia.widgets.databinding.ChatHeadViewBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.helper.applyGliaThemeOverlays
import com.glia.widgets.helper.wrapWithGliaTheme
import android.graphics.Color
import com.glia.widgets.internal.chathead.BubbleContent
import com.glia.widgets.internal.chathead.BubbleRenderTarget
import com.glia.widgets.internal.chathead.BubbleUiModel
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.glia.widgets.view.unifiedui.theme.base.BadgeTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.bubble.BubbleTheme
import com.glia.widgets.view.unifiedui.theme.chat.ChatTheme
import io.reactivex.rxjava3.core.Observable
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.Executor

@RunWith(RobolectricTestRunner::class)
internal class ChatHeadViewTest {

    private lateinit var chatHeadView: ChatHeadView

    @Before
    fun setUp() {
        val application = RuntimeEnvironment.getApplication()

        Dependencies.resourceProvider = ResourceProvider(application)

        val localeProvider = mock<com.glia.widgets.locale.LocaleProvider>()
        whenever(localeProvider.getLocaleObservable()) doReturn Observable.never()
        whenever(localeProvider.getString(
            org.mockito.kotlin.any<Int>(),
            org.mockito.kotlin.any<List<com.glia.widgets.locale.StringKeyPair>>()
        )) doReturn "Back to the Engagement. Floating Button."
        Dependencies.localeProvider = localeProvider

        // In production ChatHeadView is always hosted by an already-composed context -
        // ChatHeadService's base context or ChatHeadLayout - so it does not wrap on its own.
        chatHeadView = ChatHeadView(application.wrapWithGliaTheme()).also {
            it.executor = Executor(Runnable::run)
        }
    }

    @After
    fun tearDown() {
        // UnifiedThemeManager is process-global state on the Dependencies object
        Dependencies.gliaThemeManager.theme = null
    }

    private val binding: ChatHeadViewBinding get() = ChatHeadViewBinding.bind(chatHeadView)

    private fun model(
        content: BubbleContent = BubbleContent.Ended,
        unreadCount: Int = 0,
        isOnHold: Boolean = false,
        isOnChatScreen: Boolean = false
    ): BubbleUiModel = BubbleUiModel(BubbleRenderTarget.APPLICATION, content, unreadCount, isOnHold, isOnChatScreen)

    @Test
    fun `render draws the placeholder for an engagement without an operator image`() {
        chatHeadView.render(model(content = BubbleContent.Engaged(null)))

        assertEquals(View.VISIBLE, binding.placeholderView.visibility)
        assertEquals(View.GONE, binding.queueingLottieAnimation.visibility)
    }

    @Test
    fun `render draws the placeholder once the engagement ended`() {
        chatHeadView.render(model(content = BubbleContent.Ended))

        assertEquals(View.VISIBLE, binding.placeholderView.visibility)
    }

    @Test
    fun `render draws the queueing animation while queueing`() {
        chatHeadView.render(model(content = BubbleContent.Queueing))

        assertEquals(View.VISIBLE, binding.queueingLottieAnimation.visibility)
        assertEquals(View.GONE, binding.placeholderView.visibility)
    }

    @Test
    fun `render shows the unread badge and the on hold overlay`() {
        chatHeadView.render(model(content = BubbleContent.Engaged(null), unreadCount = 3, isOnHold = true))

        assertEquals(View.VISIBLE, binding.onHoldIcon.visibility)
        assertTrue(binding.chatBubbleBadge.isVisible)
        assertEquals("3", binding.chatBubbleBadge.text.toString())
    }

    @Test
    fun `render hides the unread badge when there is nothing unread`() {
        chatHeadView.render(model(content = BubbleContent.Engaged(null)))

        assertFalse(binding.chatBubbleBadge.isVisible)
        assertEquals(View.GONE, binding.onHoldIcon.visibility)
    }

    /**
     * The bubble on the chat screen is part of the chat UI, so it takes `chatTheme.bubble`; everywhere
     * else — other screens and the overlay — it takes the top-level `bubbleTheme`. The screen decides,
     * not the host: before the unification this was keyed off `context is Service`.
     */
    @Test
    fun `the chat screen bubble is themed by the chat theme, every other bubble by the bubble theme`() {
        val standaloneBadgeColor = Color.RED
        val chatScreenBadgeColor = Color.GREEN
        Dependencies.gliaThemeManager.theme = UnifiedTheme(
            bubbleTheme = BubbleTheme(badge = BadgeTheme(textColor = ColorTheme(standaloneBadgeColor))),
            chatTheme = ChatTheme(bubble = BubbleTheme(badge = BadgeTheme(textColor = ColorTheme(chatScreenBadgeColor))))
        )
        // In production the unified theme is applied at SDK init, before any bubble view exists
        chatHeadView = ChatHeadView(RuntimeEnvironment.getApplication().wrapWithGliaTheme()).also {
            it.executor = Executor(Runnable::run)
        }

        chatHeadView.render(model(isOnChatScreen = false))
        assertEquals(standaloneBadgeColor, binding.chatBubbleBadge.textColors.defaultColor)

        chatHeadView.render(model(isOnChatScreen = true))
        assertEquals(chatScreenBadgeColor, binding.chatBubbleBadge.textColors.defaultColor)

        // and back, proving the switch repaints in both directions
        chatHeadView.render(model(isOnChatScreen = false))
        assertEquals(standaloneBadgeColor, binding.chatBubbleBadge.textColors.defaultColor)
    }

    @Test
    fun `accessibility node info has Button className`() {
        val delegate = ViewCompat.getAccessibilityDelegate(chatHeadView)
            ?: error("Accessibility delegate must be set on ChatHeadView")
        val nodeInfo = obtainNodeInfo()
        delegate.onInitializeAccessibilityNodeInfo(chatHeadView, nodeInfo)

        assertEquals(Button::class.java.name, nodeInfo.className)
    }

    @Test
    fun `accessibility node info has ACTION_CLICK action`() {
        val delegate = ViewCompat.getAccessibilityDelegate(chatHeadView)
            ?: error("Accessibility delegate must be set on ChatHeadView")
        val nodeInfo = obtainNodeInfo()
        delegate.onInitializeAccessibilityNodeInfo(chatHeadView, nodeInfo)

        val hasClickAction = nodeInfo.actionList.any { it.id == AccessibilityNodeInfoCompat.ACTION_CLICK }
        assertTrue("ACTION_CLICK must be present in accessibility actions", hasClickAction)
    }

    /**
     * The bubble carried a `ChatHeadConfiguration` built from `UiTheme` until it started resolving
     * `gliaBrandPrimaryColor` / `gliaBaseLightColor` / `gliaIconPlaceholder` / `gliaIconOnHold` from
     * its own context. Snapshots render the bubble under the default theme only, so the customized
     * case is locked here.
     */
    @Test
    fun `bubble colours and icons follow the composed theme`() {
        val customized = themed(R.style.Test_Glia_Customization)
        val binding = ChatHeadViewBinding.bind(ChatHeadView(customized))

        val brand = customized.color(R.color.glia_test_customization_brand)
        val light = customized.color(R.color.glia_light_color)

        assertEquals(brand, binding.chatBubbleBadge.backgroundTintList?.defaultColor)
        assertEquals(light, binding.chatBubbleBadge.textColors.defaultColor)
        assertEquals(light, binding.placeholderView.imageTintList?.defaultColor)
        assertEquals(light, binding.queueingLottieAnimationPlaceholder.imageTintList?.defaultColor)
        assertEquals(light, binding.onHoldIcon.imageTintList?.defaultColor)
    }

    @Test
    fun `bubble colours fall back to the SDK defaults when nothing is customized`() {
        val default = themed(customizationStyle = null)
        val binding = ChatHeadViewBinding.bind(ChatHeadView(default))

        assertEquals(default.color(R.color.glia_primary_color), binding.chatBubbleBadge.backgroundTintList?.defaultColor)
        assertEquals(default.color(R.color.glia_light_color), binding.placeholderView.imageTintList?.defaultColor)
    }

    private fun themed(@StyleRes customizationStyle: Int?): Context =
        ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.Theme_Glia_Internal).also { context ->
            customizationStyle?.also { context.applyGliaThemeOverlays(customizationStyle = it) }
        }

    private fun Context.color(@ColorRes colorRes: Int): Int = ContextCompat.getColor(this, colorRes)

    @Suppress("DEPRECATION")
    private fun obtainNodeInfo(): AccessibilityNodeInfoCompat =
        AccessibilityNodeInfoCompat.wrap(AccessibilityNodeInfo.obtain())
}
