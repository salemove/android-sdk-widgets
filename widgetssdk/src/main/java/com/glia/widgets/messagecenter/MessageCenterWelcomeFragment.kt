package com.glia.widgets.messagecenter

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.glia.telemetry_lib.ButtonNames
import com.glia.telemetry_lib.GliaLogger
import com.glia.widgets.HostActivity
import com.glia.widgets.R
import com.glia.widgets.base.BaseFragment
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.logScWelcomeScreenButtonClicked
import com.glia.widgets.internal.fileupload.PickVisualMediaMultipleMimeTypes
import com.glia.widgets.navigation.Destination
import com.glia.widgets.view.Dialogs

/**
 * Fragment for MessageCenter welcome/compose screen with MVI architecture.
 *
 * Displayed as a full-screen fragment within HostActivity.
 * Allows visitors to compose and send messages with attachments.
 */
internal class MessageCenterWelcomeFragment :
    BaseFragment<MessageCenterWelcomeUiState, MessageCenterWelcomeEffect, MessageCenterWelcomeViewModel>(
        R.layout.fragment_message_center_welcome
    ) {

    companion object {
        internal const val ARG_QUEUE_IDS: String = "arg_queue_ids"
    }

    override val viewModel: MessageCenterWelcomeViewModel by viewModels { Dependencies.viewModelFactory }
    private val localeProvider by lazy { Dependencies.localeProvider }

    private var welcomeView: MessageCenterWelcomeView? = null

    // Activity result launchers
    private val pickContentMimeTypes: PickVisualMediaMultipleMimeTypes = PickVisualMediaMultipleMimeTypes()
    private val getMediaContent = registerForActivityResult(pickContentMimeTypes) { uri: Uri? ->
        uri?.let { viewModel.processIntent(MessageCenterWelcomeIntent.ContentChosen(it)) }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { viewModel.processIntent(MessageCenterWelcomeIntent.ContentChosen(it)) }
    }

    private val getImage = registerForActivityResult(ActivityResultContracts.TakePicture()) { captured ->
        viewModel.processIntent(MessageCenterWelcomeIntent.ImageCaptured(captured))
    }

    private val getPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.processIntent(MessageCenterWelcomeIntent.TakePhotoClicked)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        welcomeView = view.findViewById(R.id.message_center_welcome_view)

        super.onViewCreated(view, savedInstanceState)

        // Initialize after views are set up
        if (savedInstanceState == null) {
            viewModel.processIntent(MessageCenterWelcomeIntent.Initialize)
        }
    }

    override fun setupViews() {
        // Setup AppBar through the custom view
        welcomeView?.onCloseClickListener = View.OnClickListener {
            viewModel.processIntent(MessageCenterWelcomeIntent.CloseClicked)
            GliaLogger.logScWelcomeScreenButtonClicked(ButtonNames.CLOSE)
        }

        // Setup MessageView listeners
        welcomeView?.messageView?.apply {
            setOnCheckMessageButtonClickListener {
                viewModel.processIntent(MessageCenterWelcomeIntent.CheckMessagesClicked)
            }
            setOnSendMessageButtonClickListener {
                viewModel.processIntent(MessageCenterWelcomeIntent.SendMessageClicked)
            }
            setOnAttachmentButtonClickListener {
                viewModel.processIntent(MessageCenterWelcomeIntent.AddAttachmentClicked)
            }
            setOnMessageTextChangedListener {
                viewModel.processIntent(MessageCenterWelcomeIntent.MessageTextChanged(it))
            }
            setOnRemoveAttachmentListener {
                viewModel.processIntent(MessageCenterWelcomeIntent.RemoveAttachment(it))
            }
        }
    }

    override fun handleState(state: MessageCenterWelcomeUiState) {
        welcomeView?.messageView?.onStateUpdated(
            MessageCenterState(
                addAttachmentButtonVisible = state.addAttachmentButtonVisible,
                addAttachmentButtonEnabled = state.addAttachmentButtonEnabled,
                isLibraryAttachmentVisible = state.isLibraryAttachmentVisible,
                isTakePhotoAttachmentVisible = state.isTakePhotoAttachmentVisible,
                isBrowseAttachmentVisible = state.isBrowseAttachmentVisible,
                messageEditTextEnabled = state.messageEditTextEnabled,
                sendMessageButtonState = mapButtonState(state.sendMessageButtonState),
                showMessageLimitError = state.showMessageLimitError,
                showSendMessageGroup = state.showSendMessageGroup
            )
        )
        welcomeView?.messageView?.emitUploadAttachments(state.attachments)
    }

    override fun handleEffect(effect: MessageCenterWelcomeEffect) {
        when (effect) {
            MessageCenterWelcomeEffect.NavigateToConfirmation -> {
                (activity as? HostActivity)?.navigateTo(Destination.MessageCenterConfirmation)
            }

            MessageCenterWelcomeEffect.NavigateToMessaging -> {
                Dependencies.activityLauncher.launchChat(requireContext(), com.glia.widgets.chat.Intention.SC_CHAT)
                (activity as? HostActivity)?.finishAndRemoveTask()
            }

            MessageCenterWelcomeEffect.ReturnToLiveChat -> {
                Dependencies.activityLauncher.launchChat(requireContext(), com.glia.widgets.chat.Intention.RETURN_TO_CHAT)
                (activity as? HostActivity)?.finishAndRemoveTask()
            }

            MessageCenterWelcomeEffect.Finish -> {
                (activity as? HostActivity)?.finishAndRemoveTask()
            }

            MessageCenterWelcomeEffect.HideSoftKeyboard -> {
                view?.let { v ->
                    androidx.core.view.WindowInsetsControllerCompat(requireActivity().window, v)
                        .hide(androidx.core.view.WindowInsetsCompat.Type.ime())
                }
            }

            MessageCenterWelcomeEffect.ShowAttachmentPopup -> {
                welcomeView?.messageView?.showAttachmentPopup(
                    onGalleryClicked = { viewModel.processIntent(MessageCenterWelcomeIntent.GalleryClicked) },
                    onTakePhotoClicked = { handleTakePhotoClicked() },
                    onBrowseClicked = { viewModel.processIntent(MessageCenterWelcomeIntent.BrowseClicked) }
                )
            }

            is MessageCenterWelcomeEffect.SelectMediaAttachmentFile -> {
                view?.let { v ->
                    androidx.core.view.WindowInsetsControllerCompat(requireActivity().window, v)
                        .hide(androidx.core.view.WindowInsetsCompat.Type.ime())
                }
                pickContentMimeTypes.mimeTypes = effect.types
                getMediaContent.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                )
            }

            is MessageCenterWelcomeEffect.SelectAttachmentFile -> {
                view?.let { v ->
                    androidx.core.view.WindowInsetsControllerCompat(requireActivity().window, v)
                        .hide(androidx.core.view.WindowInsetsCompat.Type.ime())
                }
                getContent.launch(effect.types.toTypedArray())
            }

            is MessageCenterWelcomeEffect.TakePhoto -> {
                getImage.launch(effect.uri)
            }

            MessageCenterWelcomeEffect.RequestCameraPermission -> {
                getPermission.launch(Manifest.permission.CAMERA)
            }

            MessageCenterWelcomeEffect.ShowUnexpectedErrorDialog -> {
                context?.let { ctx ->
                    val theme = com.glia.widgets.UiTheme()
                    Dialogs.showUnexpectedErrorDialog(ctx, theme) {
                        // Dialog dismissed
                    }
                }
            }

            MessageCenterWelcomeEffect.ShowMessageCenterUnavailableDialog -> {
                context?.let { ctx ->
                    val theme = com.glia.widgets.UiTheme()
                    Dialogs.showMessageCenterUnavailableDialog(ctx, theme)
                }
            }

            MessageCenterWelcomeEffect.ShowUnauthenticatedDialog -> {
                context?.let { ctx ->
                    val theme = com.glia.widgets.UiTheme()
                    Dialogs.showUnAuthenticatedDialog(ctx, theme) {
                        viewModel.processIntent(MessageCenterWelcomeIntent.CloseClicked)
                    }
                }
            }

            MessageCenterWelcomeEffect.DismissDialogs -> {
                // Dialogs are already dismissed via HostActivity back handling
            }
        }
    }

    private fun handleTakePhotoClicked() {
        view?.let { v ->
            androidx.core.view.WindowInsetsControllerCompat(requireActivity().window, v)
                .hide(androidx.core.view.WindowInsetsCompat.Type.ime())
        }

        if (context?.let { androidx.core.content.ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) }
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            viewModel.processIntent(MessageCenterWelcomeIntent.TakePhotoClicked)
        } else {
            viewModel.processIntent(MessageCenterWelcomeIntent.TakePhotoClicked)
        }

        GliaLogger.logScWelcomeScreenButtonClicked(ButtonNames.ADD_ATTACHMENT_CAMERA_PHOTO_OPTION)
    }

    private fun mapButtonState(state: MessageCenterWelcomeUiState.ButtonState): MessageCenterState.ButtonState {
        return when (state) {
            MessageCenterWelcomeUiState.ButtonState.NORMAL -> MessageCenterState.ButtonState.NORMAL
            MessageCenterWelcomeUiState.ButtonState.DISABLE -> MessageCenterState.ButtonState.DISABLE
            MessageCenterWelcomeUiState.ButtonState.PROGRESS -> MessageCenterState.ButtonState.PROGRESS
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        welcomeView = null
    }
}