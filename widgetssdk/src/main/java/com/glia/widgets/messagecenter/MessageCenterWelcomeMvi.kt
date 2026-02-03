package com.glia.widgets.messagecenter

import android.net.Uri
import com.glia.widgets.base.UiEffect
import com.glia.widgets.base.UiIntent
import com.glia.widgets.base.UiState
import com.glia.widgets.internal.fileupload.model.LocalAttachment

/**
 * UI state for MessageCenter welcome/compose screen.
 */
internal data class MessageCenterWelcomeUiState(
    val addAttachmentButtonVisible: Boolean = false,
    val addAttachmentButtonEnabled: Boolean = true,
    val isLibraryAttachmentVisible: Boolean = true,
    val isTakePhotoAttachmentVisible: Boolean = true,
    val isBrowseAttachmentVisible: Boolean = true,
    val messageEditTextEnabled: Boolean = true,
    val sendMessageButtonState: ButtonState = ButtonState.DISABLE,
    val showMessageLimitError: Boolean = false,
    val showSendMessageGroup: Boolean = false,
    val attachments: List<LocalAttachment> = emptyList()
) : UiState {
    enum class ButtonState {
        NORMAL,
        DISABLE,
        PROGRESS
    }
}

/**
 * User intents for MessageCenter welcome screen.
 */
internal sealed interface MessageCenterWelcomeIntent : UiIntent {
    data object Initialize : MessageCenterWelcomeIntent
    data class MessageTextChanged(val message: String) : MessageCenterWelcomeIntent
    data object SendMessageClicked : MessageCenterWelcomeIntent
    data object CheckMessagesClicked : MessageCenterWelcomeIntent
    data object CloseClicked : MessageCenterWelcomeIntent
    data object AddAttachmentClicked : MessageCenterWelcomeIntent
    data object GalleryClicked : MessageCenterWelcomeIntent
    data object BrowseClicked : MessageCenterWelcomeIntent
    data object TakePhotoClicked : MessageCenterWelcomeIntent
    data class ImageCaptured(val captured: Boolean) : MessageCenterWelcomeIntent
    data class ContentChosen(val uri: Uri) : MessageCenterWelcomeIntent
    data class RemoveAttachment(val attachment: LocalAttachment) : MessageCenterWelcomeIntent
    data object SystemBack : MessageCenterWelcomeIntent
}

/**
 * One-time effects for MessageCenter welcome screen.
 */
internal sealed interface MessageCenterWelcomeEffect : UiEffect {
    data object NavigateToConfirmation : MessageCenterWelcomeEffect
    data object NavigateToMessaging : MessageCenterWelcomeEffect
    data object ReturnToLiveChat : MessageCenterWelcomeEffect
    data object Finish : MessageCenterWelcomeEffect
    data object HideSoftKeyboard : MessageCenterWelcomeEffect
    data object ShowAttachmentPopup : MessageCenterWelcomeEffect
    data class SelectMediaAttachmentFile(val types: List<String>) : MessageCenterWelcomeEffect
    data class SelectAttachmentFile(val types: List<String>) : MessageCenterWelcomeEffect
    data class TakePhoto(val uri: Uri) : MessageCenterWelcomeEffect
    data object RequestCameraPermission : MessageCenterWelcomeEffect
    data object ShowUnexpectedErrorDialog : MessageCenterWelcomeEffect
    data object ShowMessageCenterUnavailableDialog : MessageCenterWelcomeEffect
    data object ShowUnauthenticatedDialog : MessageCenterWelcomeEffect
    data object DismissDialogs : MessageCenterWelcomeEffect
}