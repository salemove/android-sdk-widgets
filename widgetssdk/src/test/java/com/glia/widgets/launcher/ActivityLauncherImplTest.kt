package com.glia.widgets.launcher

import android.CONTEXT_EXTENSIONS_CLASS_PATH
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.chat.Intention
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.helper.IntentHelper
import com.glia.widgets.helper.safeStartActivity
import com.glia.widgets.locale.LocaleString
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class ActivityLauncherImplTest {

    private lateinit var activityLauncher: ActivityLauncherImpl
    private lateinit var intentHelper: IntentHelper
    private lateinit var engagementRepository: EngagementRepository
    private lateinit var context: Context
    private lateinit var activity: Activity
    private lateinit var uri: Uri
    private lateinit var attachmentFile: AttachmentFile
    private lateinit var localAttachment: LocalAttachment
    private lateinit var survey: Survey
    private lateinit var localeString: LocaleString

    @Before
    fun setUp() {
        mockkStatic(CONTEXT_EXTENSIONS_CLASS_PATH)
        intentHelper = mockk(relaxed = true)
        engagementRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)
        activity = mockk(relaxed = true)
        uri = mockk(relaxed = true)
        attachmentFile = mockk(relaxed = true)
        localAttachment = mockk(relaxed = true)
        survey = mockk(relaxed = true)
        localeString = mockk(relaxed = true)

        every { any<Context>().safeStartActivity(any(), any(), any()) } answers {
            val onSuccess = thirdArg<() -> Unit>()
            onSuccess.invoke()
        }

        activityLauncher = ActivityLauncherImpl(intentHelper, engagementRepository)
    }

    @After
    fun tearDown() {
        unmockkStatic(CONTEXT_EXTENSIONS_CLASS_PATH)
    }

    @Test
    fun `launchChat updates secure messaging status and starts chat activity`() {
        val intention = Intention.RETURN_TO_CHAT
        every { context.startActivity(any()) } just Runs

        activityLauncher.launchChat(context, intention)

        verify { engagementRepository.updateIsSecureMessagingRequested(intention.isSecureConversation) }
        verify { context.startActivity(any()) }
    }

    @Test
    fun `launchCall updates secure messaging status and starts call activity`() {
        val mediaType = Engagement.MediaType.AUDIO
        every { context.startActivity(any()) } just Runs

        activityLauncher.launchCall(context, mediaType, true)

        verify { engagementRepository.updateIsSecureMessagingRequested(false) }
        verify { context.startActivity(any()) }
    }

    @Test
    fun `launchSecureMessagingWelcomeScreen updates secure messaging status and starts welcome screen`() {
        every { activity.startActivity(any()) } just Runs

        activityLauncher.launchSecureMessagingWelcomeScreen(activity)

        verify { engagementRepository.updateIsSecureMessagingRequested(true) }
        verify { activity.startActivity(any()) }
    }

    @Test
    fun `launchEndScreenSharing starts end screen sharing activity`() {
        every { context.startActivity(any()) } just Runs

        activityLauncher.launchEndScreenSharing(context)

        verify { context.startActivity(any()) }
    }

    @Test
    fun `launchWebBrowser starts web browser activity`() {
        val url = "https://example.com"
        every { context.startActivity(any()) } just Runs

        activityLauncher.launchWebBrowser(context, localeString, url)

        verify { context.startActivity(any()) }
    }

    @Test
    fun `launchOverlayPermission starts overlay permission activity`() {
        val onSuccess = mockk<() -> Unit>(relaxed = true)
        val onFailure = mockk<() -> Unit>(relaxed = true)

        activityLauncher.launchOverlayPermission(context, onSuccess, onFailure)

        verify { context.safeStartActivity(any(), onFailure, onSuccess) }
    }

    @Test
    fun `launchImagePreview with AttachmentFile starts image preview activity`() {
        val options = mockk<Bundle>(relaxed = true)
        every { context.startActivity(any(), any()) } just Runs

        activityLauncher.launchImagePreview(context, attachmentFile, options)

        verify { context.startActivity(any(), any()) }
    }

    @Test
    fun `launchImagePreview with LocalAttachment starts image preview activity`() {
        val options = mockk<Bundle>(relaxed = true)
        every { context.startActivity(any(), any()) } just Runs

        activityLauncher.launchImagePreview(context, localAttachment, options)

        verify { context.startActivity(any(), any()) }
    }

    @Test
    fun `launchEmailClient starts email client activity`() {
        val onFailure = mockk<() -> Unit>(relaxed = true)

        activityLauncher.launchEmailClient(context, uri, onFailure)

        verify { context.safeStartActivity(any(), onFailure) }
    }

    @Test
    fun `launchDialer starts dialer activity`() {
        val onFailure = mockk<() -> Unit>(relaxed = true)

        activityLauncher.launchDialer(context, uri, onFailure)

        verify { context.safeStartActivity(any(), onFailure) }
    }

    @Test
    fun `launchUri starts uri activity`() {
        val onFailure = mockk<() -> Unit>(relaxed = true)

        activityLauncher.launchUri(context, uri, onFailure)

        verify { context.safeStartActivity(any(), onFailure) }
    }

    @Test
    fun `launchFileReader starts file reader activity`() {
        val fileContentType = "application/pdf"
        val onFailure = mockk<() -> Unit>(relaxed = true)

        activityLauncher.launchFileReader(context, uri, fileContentType, onFailure)

        verify { context.safeStartActivity(any(), onFailure) }
    }

    @Test
    fun `launchShareImage starts share image activity`() {
        val fileName = "image.png"
        every { activity.startActivity(any()) } just Runs

        activityLauncher.launchShareImage(activity, fileName)

        verify { activity.startActivity(any()) }
    }

    @Test
    fun `launchEntryWidget starts entry widget activity`() {
        every { activity.startActivity(any()) } just Runs

        activityLauncher.launchEntryWidget(activity)

        verify { activity.startActivity(any()) }
    }

    @Test
    fun `launchSurvey starts survey activity`() {
        every { activity.startActivity(any()) } just Runs

        activityLauncher.launchSurvey(activity, survey)

        verify { activity.startActivity(any()) }
    }
}
