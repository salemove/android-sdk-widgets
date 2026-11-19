package com.glia.widgets.view.head

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import androidx.annotation.ColorRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.glia.widgets.R
import com.glia.widgets.databinding.ChatHeadViewBinding
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.di.UseCaseFactory
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.helper.applyGliaThemeOverlays
import com.glia.widgets.helper.wrapWithGliaTheme
import io.reactivex.rxjava3.core.Observable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

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

        val controllerFactory = mock<ControllerFactory>()
        val chatHeadController = mock<ChatHeadContract.Controller>()
        whenever(controllerFactory.chatHeadController) doReturn chatHeadController
        Dependencies.controllerFactory = controllerFactory

        val useCaseFactory = mock<UseCaseFactory>()
        val isCallVisualizerUseCase = mock<IsCurrentEngagementCallVisualizerUseCase>()
        whenever(useCaseFactory.isCurrentEngagementCallVisualizer) doReturn isCallVisualizerUseCase
        Dependencies.useCaseFactory = useCaseFactory

        // In production ChatHeadView is always hosted by an already-composed context -
        // ChatHeadService's base context or ChatHeadLayout - so it does not wrap on its own.
        chatHeadView = ChatHeadView(application.wrapWithGliaTheme())
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
