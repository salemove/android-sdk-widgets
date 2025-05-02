package com.glia.widgets.snapshotutils

import android.view.View
import android.widget.EditText
import androidx.annotation.DrawableRes
import com.glia.widgets.R
import com.glia.widgets.chat.ChatContract
import com.glia.widgets.chat.ChatView
import com.glia.widgets.chat.controller.ChatController
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.glia.widgets.databinding.ChatActivityBinding
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.entrywidget.EntryWidgetContract
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.Executor

internal interface SnapshotChatView : SnapshotContent, SnapshotTheme, SnapshotActivityWindow, SnapshotProviders,
    SnapshotGetImageFile, SnapshotSchedulers, SnapshotAttachment, SnapshotPicasso, SnapshotLottie, SnapshotThemeConfiguration {

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

        lottieMock()

        val controllerFactoryMock = mock<ControllerFactory>()
        val chatControllerMock = mock<ChatController>()
        val entryWidgetControllerMock = mock<EntryWidgetContract.Controller>()
        whenever(controllerFactoryMock.chatController).thenReturn(chatControllerMock)
        whenever(controllerFactoryMock.entryWidgetController).thenReturn(entryWidgetControllerMock)
        Dependencies.controllerFactory = controllerFactoryMock

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
        fileAttachments: List<LocalAttachment>? = null,
        @DrawableRes imageResources: List<Int>? = null,
        message: String? = null,
        executor: Executor? = Executor(Runnable::run),
        unifiedTheme: UnifiedTheme? = null
    ): ViewData {
        setUnifiedTheme(unifiedTheme)

        val mock = chatViewMock()

        imageResources?.let { picassoMock(imageResources) }

        val chatViewCaptor: KArgumentCaptor<ChatContract.View> = argumentCaptor()
        val chatActivityBinding = ChatActivityBinding.inflate(layoutInflater)
        val root = chatActivityBinding.root
        val chatView = chatActivityBinding.chatView
        verify(mock.chatControllerMock).setView(chatViewCaptor.capture())

        chatView.executor = executor

        val chatViewCallback = chatViewCaptor.lastValue
        chatState?.let { chatViewCallback.emitState(it) }
        chatItems?.let { chatViewCallback.emitItems(it) }
        fileAttachments?.let { chatViewCallback.emitUploadAttachments(it) }
        message?.let { chatView.findViewById<EditText>(R.id.chat_edit_text).setText(it) }

        return ViewData(root, chatView, mock)
    }

    fun unifiedThemeWithoutChat(): UnifiedTheme = unifiedTheme(R.raw.test_unified_config) { unifiedTheme ->
        unifiedTheme.remove("chatScreen")
    }
}
