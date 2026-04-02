package com.glia.widgets.view.head

import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.di.UseCaseFactory
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.helper.ResourceProvider
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

        chatHeadView = ChatHeadView(application)
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

    @Suppress("DEPRECATION")
    private fun obtainNodeInfo(): AccessibilityNodeInfoCompat =
        AccessibilityNodeInfoCompat.wrap(AccessibilityNodeInfo.obtain())
}
