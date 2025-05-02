package com.glia.widgets.view.head

import com.glia.widgets.R
import com.glia.widgets.SnapshotTest
import com.glia.widgets.internal.callvisualizer.domain.IsCallVisualizerScreenSharingUseCase
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.di.UseCaseFactory
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.snapshotutils.SnapshotChatView
import com.glia.widgets.snapshotutils.SnapshotLottie
import com.glia.widgets.snapshotutils.SnapshotPicasso
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.snapshotutils.SnapshotThemeConfiguration
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.Executor

internal class ChatHeadViewSnapshotTest : SnapshotTest(
    maxPercentDifference = 0.01
), SnapshotChatView, SnapshotProviders, SnapshotLottie, SnapshotPicasso, SnapshotThemeConfiguration {

    // MARK: Default state

    @Test
    fun defaultState() {
        snapshot(setupView())
    }

    // MARK: Operator Image

    private fun showOperatorImageView(
        unifiedTheme: UnifiedTheme? = null
    ) = setupView(
        unifiedTheme = unifiedTheme
    ).also {
        it.showOperatorImage("https://operator.avatar")
    }

    @Test
    fun showOperatorImage() {
        snapshot(
            showOperatorImageView()
        )
    }

    @Test
    fun showOperatorImageWithGlobalColors() {
        snapshot(
            showOperatorImageView(unifiedTheme = unifiedThemeWithGlobalColors())
        )
    }

    @Test
    fun showOperatorImageWithUnifiedTheme() {
        snapshot(
            showOperatorImageView(unifiedTheme = unifiedTheme())
        )
    }

    @Test
    fun showOperatorImageWithUnifiedThemeWithoutChat() {
        snapshot(
            showOperatorImageView(unifiedTheme = unifiedThemeWithoutChat())
        )
    }

    // MARK: Unread messages budge

    private fun showUnreadMessageCountView(
        unifiedTheme: UnifiedTheme? = null
    ) = showPlaceholderView(
        unifiedTheme = unifiedTheme
    ).also {
        it.showUnreadMessageCount(5)
    }

    @Test
    fun showUnreadMessageCount() {
        snapshot(
            showUnreadMessageCountView()
        )
    }

    @Test
    fun showUnreadMessageCountWithGlobalColors() {
        snapshot(
            showUnreadMessageCountView(unifiedTheme = unifiedThemeWithGlobalColors())
        )
    }

    @Test
    fun showUnreadMessageCountWithUnifiedTheme() {
        snapshot(
            showUnreadMessageCountView(unifiedTheme = unifiedTheme())
        )
    }

    @Test
    fun showUnreadMessageCountWithUnifiedThemeWithoutChat() {
        snapshot(
            showUnreadMessageCountView(unifiedTheme = unifiedThemeWithoutChat())
        )
    }

    // MARK: Placeholder

    private fun showPlaceholderView(
        unifiedTheme: UnifiedTheme? = null
    ) = setupView(
        unifiedTheme = unifiedTheme
    ).also {
        it.showPlaceholder()
    }

    @Test
    fun showPlaceholder() {
        snapshot(
            showPlaceholderView()
        )
    }

    @Test
    fun showPlaceholderWithGlobalColors() {
        snapshot(
            showPlaceholderView(unifiedTheme = unifiedThemeWithGlobalColors())
        )
    }

    @Test
    fun showPlaceholderWithUnifiedTheme() {
        snapshot(
            showPlaceholderView(unifiedTheme = unifiedTheme())
        )
    }

    @Test
    fun showPlaceholderWithUnifiedThemeWithoutChat() {
        snapshot(
            showPlaceholderView(unifiedTheme = unifiedThemeWithoutChat())
        )
    }

    // MARK: Queueing

    private fun showQueueingView(
        unifiedTheme: UnifiedTheme? = null
    ) = showPlaceholderView(
        unifiedTheme = unifiedTheme
    ).also {
        it.showPlaceholder()
    }

    @Test
    fun showQueueing() {
        snapshot(
            showQueueingView()
        )
    }

    @Test
    fun showQueueingWithGlobalColors() {
        snapshot(
            showQueueingView(unifiedTheme = unifiedThemeWithGlobalColors())
        )
    }

    @Test
    fun showQueueingWithUnifiedTheme() {
        snapshot(
            showQueueingView(unifiedTheme = unifiedTheme())
        )
    }

    @Test
    fun showQueueingWithUnifiedThemeWithoutChat() {
        snapshot(
            showQueueingView(unifiedTheme = unifiedThemeWithoutChat())
        )
    }

    // MARK: ScreenSharing

    private fun showScreenSharingView(
        unifiedTheme: UnifiedTheme? = null
    ) = showPlaceholderView(
        unifiedTheme = unifiedTheme
    ).also {
        it.showScreenSharing()
    }

    @Test
    fun showScreenSharing() {
        snapshot(
            showScreenSharingView()
        )
    }

    @Test
    fun showScreenSharingWithGlobalColors() {
        snapshot(
            showScreenSharingView(unifiedTheme = unifiedThemeWithGlobalColors())
        )
    }

    @Test
    fun showScreenSharingWithUnifiedTheme() {
        snapshot(
            showScreenSharingView(unifiedTheme = unifiedTheme())
        )
    }

    @Test
    fun showScreenSharingWithUnifiedThemeWithoutChat() {
        snapshot(
            showScreenSharingView(unifiedTheme = unifiedThemeWithoutChat())
        )
    }

    // MARK: OnHold

    private fun showOnHoldView(
        unifiedTheme: UnifiedTheme? = null
    ) = showOperatorImageView(
        unifiedTheme = unifiedTheme
    ).also {
        it.showOnHold()
    }

    @Test
    fun showOnHold() {
        snapshot(
            showOnHoldView()
        )
    }

    @Test
    fun showOnHoldWithGlobalColors() {
        snapshot(
            showOnHoldView(unifiedTheme = unifiedThemeWithGlobalColors())
        )
    }

    @Test
    fun showOnHoldWithUnifiedTheme() {
        snapshot(
            showOnHoldView(unifiedTheme = unifiedTheme())
        )
    }

    @Test
    fun showOnHoldWithUnifiedThemeWithoutChat() {
        snapshot(
            showOnHoldView(unifiedTheme = unifiedThemeWithoutChat())
        )
    }

    // MARK: hide OnHold

    private fun hideOnHoldView(
        unifiedTheme: UnifiedTheme? = null
    ) = showOnHoldView(
        unifiedTheme = unifiedTheme
    ).also {
        it.hideOnHold()
    }

    @Test
    fun hideOnHold() {
        snapshot(
            hideOnHoldView()
        )
    }

    @Test
    fun hideOnHoldWithGlobalColors() {
        snapshot(
            hideOnHoldView(unifiedTheme = unifiedThemeWithGlobalColors())
        )
    }

    @Test
    fun hideOnHoldWithUnifiedTheme() {
        snapshot(
            hideOnHoldView(unifiedTheme = unifiedTheme())
        )
    }

    @Test
    fun hideOnHoldWithUnifiedThemeWithoutChat() {
        snapshot(
            hideOnHoldView(unifiedTheme = unifiedThemeWithoutChat())
        )
    }

    // MARK: utils for tests

    private fun setupView(
        unifiedTheme: UnifiedTheme? = null,
        executor: Executor? = Executor(Runnable::run),
        isCallVisualizerScreenSharingUseCase: Boolean = false
    ): ChatHeadView {
        lottieMock()
        localeProviderMock()
        resourceProviderMock()
        picassoMock(listOf(R.drawable.test_launcher2))

        val controllerFactoryMock = mock<ControllerFactory>()
        val chatHeadControllerMock = mock<ChatHeadContract.Controller>()
        whenever(controllerFactoryMock.chatHeadController).thenReturn(chatHeadControllerMock)
        Dependencies.controllerFactory = controllerFactoryMock

        val useCaseFactoryMock = mock<UseCaseFactory>()
        val isCallVisualizerScreenSharingUseCaseMock = mock<IsCallVisualizerScreenSharingUseCase>()
        whenever(isCallVisualizerScreenSharingUseCaseMock.invoke()).thenReturn(isCallVisualizerScreenSharingUseCase)
        whenever(useCaseFactoryMock.createIsCallVisualizerScreenSharingUseCase()).thenReturn(isCallVisualizerScreenSharingUseCaseMock)
        val isCurrentEngagementCallVisualizerUseCaseMock = mock<IsCurrentEngagementCallVisualizerUseCase>()
        whenever(useCaseFactoryMock.isCurrentEngagementCallVisualizer).thenReturn(isCurrentEngagementCallVisualizerUseCaseMock)
        Dependencies.useCaseFactory = useCaseFactoryMock

        setUnifiedTheme(unifiedTheme)

        return ChatHeadView(context).also {
            it.executor = executor
        }
    }

}
