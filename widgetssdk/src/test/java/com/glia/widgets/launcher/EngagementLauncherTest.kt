package com.glia.widgets.launcher

import android.app.Activity
import io.mockk.*
import org.junit.Before
import org.junit.Test

class EngagementLauncherTest {

    private lateinit var activity: Activity
    private lateinit var activityLauncher: ActivityLauncher
    private lateinit var engagementLauncher: EngagementLauncher
    private lateinit var configurationManager: ConfigurationManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        activity = mockk(relaxed = true)
        activityLauncher = mockk<ActivityLauncher> {
            every { launchChat(any()) } returns  Unit
            every { launchCall(any(), any(), any()) } returns  Unit
            every { launchSecureMessagingWelcomeScreen(any()) } returns  Unit
        }
        configurationManager = mockk<ConfigurationManager> {
            every { setVisitorContextAssetId(any()) } just Runs
        }
        engagementLauncher = EngagementLauncherImpl(activityLauncher, configurationManager)
    }

    @Test
    fun `startChat with visitorContextAssetId calls setVisitorContextAssetId`() {
        val visitorContextAssetId = "visitor_context"
        engagementLauncher.startChat(activity, visitorContextAssetId)

        verify { configurationManager.setVisitorContextAssetId(visitorContextAssetId) }
    }

    @Test
    fun `startChat without visitorContextAssetId does not call setVisitorContextAssetId`() {
        engagementLauncher.startChat(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(any()) }
    }

    @Test
    fun `startChat with visitorContextAssetId null does not call setVisitorContextAssetId`() {
        val visitorContextAssetId = null
        engagementLauncher.startChat(activity, visitorContextAssetId)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(any()) }
    }

    @Test
    fun `startAudioCall with visitorContextAssetId calls setVisitorContextAssetId`() {
        val visitorContextAssetId = "visitor_context"
        engagementLauncher.startAudioCall(activity, visitorContextAssetId)

        verify { configurationManager.setVisitorContextAssetId(visitorContextAssetId) }
    }

    @Test
    fun `startAudioCall without visitorContextAssetId does not call setVisitorContextAssetId`() {
        engagementLauncher.startAudioCall(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(any()) }
    }

    @Test
    fun `startAudioCall with visitorContextAssetId null does not call setVisitorContextAssetId`() {
        val visitorContextAssetId = null
        engagementLauncher.startAudioCall(activity, visitorContextAssetId)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(any()) }
    }

    @Test
    fun `startSecureMessaging with visitorContextAssetId calls setVisitorContextAssetId`() {
        val visitorContextAssetId = "visitor_context"
        engagementLauncher.startSecureMessaging(activity, visitorContextAssetId)

        verify { configurationManager.setVisitorContextAssetId(visitorContextAssetId) }
    }

    @Test
    fun `startSecureMessaging without visitorContextAssetId does not call setVisitorContextAssetId`() {
        engagementLauncher.startSecureMessaging(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(any()) }
    }

    @Test
    fun `startSecureMessaging with visitorContextAssetId null does not call setVisitorContextAssetId`() {
        val visitorContextAssetId = null
        engagementLauncher.startSecureMessaging(activity, visitorContextAssetId)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(any()) }
    }
}
