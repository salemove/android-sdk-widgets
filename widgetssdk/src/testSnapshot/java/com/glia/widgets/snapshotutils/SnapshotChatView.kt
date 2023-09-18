package com.glia.widgets.snapshotutils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.Window
import androidx.core.graphics.toColorInt
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.ChatView
import com.glia.widgets.chat.ChatViewCallback
import com.glia.widgets.chat.controller.ChatController
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.databinding.ChatActivityBinding
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.di.UseCaseFactory
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.helper.requireActivity
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import io.mockk.every
import io.mockk.mockkStatic
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.Executor

internal interface SnapshotChatView : SnapshotContent {
    val chatViewMock: Mock

    class Mock(private val snapshotContent: SnapshotContent) {
        lateinit var activityMock: Activity
        lateinit var windowMock: Window

        lateinit var controllerFactoryMock: ControllerFactory
        lateinit var chatControllerMock: ChatController

        lateinit var useCaseFactoryMock: UseCaseFactory
        lateinit var getImageFileFromCacheUseCaseMock: GetImageFileFromCacheUseCase
        lateinit var getImageFileFromDownloadsUseCaseMock: GetImageFileFromDownloadsUseCase
        lateinit var getImageFileFromNetworkUseCaseMock: GetImageFileFromNetworkUseCase

        lateinit var chatViewCallbackCaptor: ArgumentCaptor<ChatViewCallback>

        fun setUp(statusBarColor: Int = "#123456".toColorInt()) {
            activityMock = mock()
            windowMock = mock()
            whenever(activityMock.window).thenReturn(windowMock)
            whenever(windowMock.statusBarColor).thenReturn(statusBarColor)
            mockkStatic("com.glia.widgets.helper.ContextExtensionsKt")
            every { any<Context>().requireActivity() } returns activityMock

            chatViewCallbackCaptor = ArgumentCaptor.forClass(ChatViewCallback::class.java)
            chatControllerMock = mock()
            controllerFactoryMock = mock()
            whenever(controllerFactoryMock.getChatController(chatViewCallbackCaptor.capture())).thenReturn(chatControllerMock)

            getImageFileFromCacheUseCaseMock = mock()
            getImageFileFromDownloadsUseCaseMock = mock()
            getImageFileFromNetworkUseCaseMock = mock()
            useCaseFactoryMock = mock()
            whenever(useCaseFactoryMock.createGetImageFileFromCacheUseCase()).thenReturn(getImageFileFromCacheUseCaseMock)
            whenever(useCaseFactoryMock.createGetImageFileFromDownloadsUseCase()).thenReturn(getImageFileFromDownloadsUseCaseMock)
            whenever(useCaseFactoryMock.createGetImageFileFromNetworkUseCase()).thenReturn(getImageFileFromNetworkUseCaseMock)

            Dependencies.setControllerFactory(controllerFactoryMock)
            Dependencies.setUseCaseFactory(useCaseFactoryMock)
            Dependencies.setResourceProvider(ResourceProvider(snapshotContent.context))
        }

        fun tearDown() {
            Dependencies.getGliaThemeManager().theme = null
        }
    }

    data class ViewData(
        val root: View,
        val chatView: ChatView
    )

    fun setupView(
        chatState: ChatState? = null,
        chatItems: List<ChatItem>? = null,
        fileAttachments: List<FileAttachment>? = null,
        executor: Executor? = Executor(Runnable::run),
        unifiedTheme: UnifiedTheme? = null,
        uiTheme: UiTheme? = null,
        callback: ((ChatViewCallback) -> Unit)? = null
    ): ViewData {
        unifiedTheme?.let { Dependencies.getGliaThemeManager().theme = it }

        val chatActivityBinding = ChatActivityBinding.inflate(layoutInflater)
        val root = chatActivityBinding.root
        val chatView = chatActivityBinding.chatView

        chatView.setUiTheme(uiTheme)

        chatView.executor = executor

        val chatViewCallback = chatViewMock.chatViewCallbackCaptor.value
        chatState?.let { chatViewCallback.emitState(it) }
        chatItems?.let { chatViewCallback.emitItems(it) }
        fileAttachments?.let { chatViewCallback.emitUploadAttachments(it) }
        callback?.invoke(chatViewCallback)

        return ViewData(root, chatView)
    }
}