package com.glia.widgets.snapshotutils

import android.widget.EditText
import androidx.annotation.DrawableRes
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.messagecenter.MessageCenterContract
import com.glia.widgets.messagecenter.MessageCenterController
import com.glia.widgets.messagecenter.MessageCenterState
import com.glia.widgets.messagecenter.MessageCenterView
import com.glia.widgets.messagecenter.MessageView
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.Executor

internal interface SnapshotMessageCenterView : SnapshotTestLifecycle, SnapshotContent,
    SnapshotActivityWindow, SnapshotProviders, SnapshotAttachment, SnapshotPicasso, SnapshotTheme {

    data class Mock(
        val activityMock: SnapshotActivityWindow.Mock,
        val controllerFactoryMock: ControllerFactory,
        val messageCenterControllerMock: MessageCenterContract.Controller
    )

    fun messageCenterViewMock(): Mock {
        val activityMock = activityWindowMock()

        stringProviderMock()
        resourceProviderMock()

        val messageCenterControllerMock = mock<MessageCenterController>()
        val controllerFactoryMock = mock<ControllerFactory>()
        whenever(controllerFactoryMock.getMessageCenterController(any())).thenReturn(messageCenterControllerMock)
        Dependencies.setControllerFactory(controllerFactoryMock)

        setOnEndListener {
            Dependencies.setControllerFactory(null)
        }

        return Mock(activityMock, controllerFactoryMock, messageCenterControllerMock)
    }

    data class ViewData(
        val view: MessageCenterView,
        val mock: Mock
    )

    fun setupView(
        state: MessageCenterState = MessageCenterState(),
        fileAttachments: List<FileAttachment>? = null,
        @DrawableRes imageResources: List<Int>? = null,
        message: String? = null,
        executor: Executor? = Executor(Runnable::run),
        unifiedTheme: UnifiedTheme? = null,
        uiTheme: UiTheme? = null,
        callback: ((MessageCenterContract.View) -> Unit)? = null
    ): ViewData {
        val mock = messageCenterViewMock()

        unifiedTheme?.let { Dependencies.getGliaThemeManager().theme = it }

        imageResources?.let { picassoMock(imageResources) }

        val messageCenterView = MessageCenterView(context)

        messageCenterView.executor = executor
        messageCenterView.setupViewAppearance()

        messageCenterView.findViewById<MessageView>(R.id.message_view).executor = executor

        messageCenterView.onStateUpdated(state)
        fileAttachments?.let { messageCenterView.emitUploadAttachments(it) }

        message?.let { messageCenterView.findViewById<EditText>(R.id.message_edit_text).setText(it) }

        callback?.invoke(messageCenterView)

        setOnEndListener {
            Dependencies.getGliaThemeManager().theme = null
        }

        return ViewData(messageCenterView, mock)
    }

    fun unifiedThemeWithoutWelcomeScreen(): UnifiedTheme = unifiedTheme(R.raw.test_unified_config) { unifiedTheme ->
        unifiedTheme.remove("secureConversationsWelcomeScreen")
    }

    fun unifiedThemeWithoutConfirmationScreen(): UnifiedTheme = unifiedTheme(R.raw.test_unified_config) { unifiedTheme ->
        unifiedTheme.remove("secureConversationsConfirmationScreen")
    }
}
