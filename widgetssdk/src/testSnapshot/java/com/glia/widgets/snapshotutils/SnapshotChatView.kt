package com.glia.widgets.snapshotutils

import android.view.View
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.ChatContract
import com.glia.widgets.chat.ChatView
import com.glia.widgets.chat.controller.ChatController
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.databinding.ChatActivityBinding
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.Executor

internal interface SnapshotChatView : SnapshotContent, SnapshotActivityWindow, SnapshotProviders, SnapshotGetImageFile, SnapshotSchedulers {

    data class Mock(
        val activityMock: SnapshotActivityWindow.Mock,
        val imageFileMock: SnapshotGetImageFile.Mock,
        val schedulersMock: SnapshotSchedulers.Mock,
        val controllerFactoryMock: ControllerFactory,
        val chatControllerMock: ChatController
    )

    fun chatViewMock(): Mock {
        val activityMock = activityWindowMock()
        val imageFileMock = getImageFileMock()
        val schedulersMock = schedulersMock()

        stringProviderMock()
        resourceProviderMock()

        val controllerFactoryMock = mock<ControllerFactory>()
        val chatControllerMock = mock<ChatController>()
        whenever(controllerFactoryMock.chatController).thenReturn(chatControllerMock)
        Dependencies.setControllerFactory(controllerFactoryMock)

        setOnEndListener {
            Dependencies.setControllerFactory(null)
        }

        return Mock(activityMock, imageFileMock, schedulersMock, controllerFactoryMock, chatControllerMock)
    }

    data class ViewData(
        val root: View,
        val chatView: ChatView,
        val mock: Mock
    )

    fun setupView(
        chatState: ChatState? = null,
        chatItems: List<ChatItem>? = null,
        fileAttachments: List<FileAttachment>? = null,
        executor: Executor? = Executor(Runnable::run),
        unifiedTheme: UnifiedTheme? = null,
        uiTheme: UiTheme? = null
    ): ViewData {
        val mock = chatViewMock()

        unifiedTheme?.let { Dependencies.getGliaThemeManager().theme = it }
        val chatViewCaptor: KArgumentCaptor<ChatContract.View> = argumentCaptor()

        val chatActivityBinding = ChatActivityBinding.inflate(layoutInflater)
        val root = chatActivityBinding.root
        val chatView = chatActivityBinding.chatView
        verify(mock.chatControllerMock).setView(chatViewCaptor.capture())

        chatView.setUiTheme(uiTheme)

        chatView.executor = executor

        val chatViewCallback = chatViewCaptor.lastValue
        chatState?.let { chatViewCallback.emitState(it) }
        chatItems?.let { chatViewCallback.emitItems(it) }
        fileAttachments?.let { chatViewCallback.emitUploadAttachments(it) }

        setOnEndListener {
            Dependencies.getGliaThemeManager().theme = null
        }

        return ViewData(root, chatView, mock)
    }
}
