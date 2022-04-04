package com.glia.widgets.chat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.model.KeyPath;
import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.androidsdk.screensharing.ScreenSharing;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.adapter.ChatAdapter;
import com.glia.widgets.chat.adapter.UploadAttachmentAdapter;
import com.glia.widgets.chat.controller.ChatController;
import com.glia.widgets.chat.helper.FileHelper;
import com.glia.widgets.chat.model.ChatInputMode;
import com.glia.widgets.chat.model.ChatState;
import com.glia.widgets.chat.model.history.ChatItem;
import com.glia.widgets.chat.model.history.OperatorAttachmentItem;
import com.glia.widgets.chat.model.history.VisitorAttachmentItem;
import com.glia.widgets.core.configuration.GliaSdkConfiguration;
import com.glia.widgets.core.dialog.DialogController;
import com.glia.widgets.core.dialog.DialogsState;
import com.glia.widgets.core.fileupload.model.FileAttachment;
import com.glia.widgets.core.notification.device.NotificationManager;
import com.glia.widgets.core.screensharing.ScreenSharingController;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.filepreview.ui.FilePreviewActivity;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.view.DialogOfferType;
import com.glia.widgets.view.Dialogs;
import com.glia.widgets.view.OperatorStatusView;
import com.glia.widgets.view.SingleChoiceCardView;
import com.glia.widgets.view.head.controller.ServiceChatHeadController;
import com.glia.widgets.view.header.AppBarView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.shape.MarkerEdgeTreatment;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ChatView extends ConstraintLayout implements
        ChatAdapter.OnFileItemClickListener,
        ChatAdapter.OnImageItemClickListener {

    private final static String TAG = "ChatView";
    private AlertDialog alertDialog;

    private ChatViewCallback callback;
    private ChatController controller;

    private DialogController.Callback dialogCallback;
    private DialogController dialogController;

    private final ScreenSharingController.ViewCallback screenSharingViewCallback = exception ->
            showToast(exception.debugMessage);
    private ScreenSharingController screenSharingController;

    private ServiceChatHeadController serviceChatHeadController;

    private ChatRecyclerView chatRecyclerView;
    private ImageButton sendButton;
    private ImageButton addAttachmentButton;
    private View chatMessageLayout;
    private RecyclerView attachmentsRecyclerView;
    private UploadAttachmentAdapter uploadAttachmentAdapter;
    private EditText chatEditText;
    private LottieAnimationView operatorTypingAnimation;
    private ChatAdapter adapter;
    private AppBarView appBar;
    private View dividerView;
    private RelativeLayout newMessagesLayout;
    private MaterialCardView newMessagesCardView;
    private OperatorStatusView newMessagesOperatorStatusView;
    private TextView newMessagesCountBadgeView;

    private boolean isInBottom = true;

    private static final int OPEN_DOCUMENT_ACTION_REQUEST = 100;
    private static final int CAPTURE_IMAGE_ACTION_REQUEST = 101;
    private static final int CAPTURE_VIDEO_ACTION_REQUEST = 102;
    private static final int CAMERA_PERMISSION_REQUEST = 1010;
    private static final int WRITE_PERMISSION_REQUEST = 1001001;

    private AttachmentFile downloadFileHolder = null;

    private UiTheme theme;
    // needed for setting status bar color back when view is gone
    private Integer defaultStatusbarColor;
    private OnBackClickedListener onBackClickedListener;
    private OnEndListener onEndListener;
    private OnNavigateToCallListener onNavigateToCallListener;
    private final SingleChoiceCardView.OnOptionClickedListener onOptionClickedListener = new SingleChoiceCardView.OnOptionClickedListener() {
        @Override
        public void onClicked(String id, int indexInList, int optionIndex) {
            Logger.d(TAG, "singleChoiceCardClicked");
            if (controller != null) {
                controller.singleChoiceOptionClicked(
                        id,
                        indexInList,
                        optionIndex
                );
            }
        }
    };
    private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (controller != null) {
                controller.onRecyclerviewPositionChanged(!recyclerView.canScrollVertically(1));
            }
        }
    };

    private final Resources resources;

    private final RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            int totalItemCount = adapter.getItemCount();
            int lastIndex = totalItemCount - 1;
            if (isInBottom) {
                chatRecyclerView.scrollToPosition(lastIndex);
            }
        }
    };

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (controller != null) {
                controller.onMessageTextChanged(editable.toString().trim());
            }
        }
    };

    public ChatView(Context context) {
        this(context, null);
    }

    public ChatView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.gliaChatStyle);
    }

    public ChatView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat);
    }

    public ChatView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(
                MaterialThemeOverlay.wrap(
                        context,
                        attrs,
                        defStyleAttr,
                        defStyleRes),
                attrs,
                defStyleAttr,
                defStyleRes
        );

        this.resources = getResources();

        initConfigurations();
        initViews();
        readTypedArray(attrs, defStyleAttr, defStyleRes);
        setupViewAppearance();
        setupViewActions();
        setupControllers();
    }

    /**
     * @param uiTheme sets this view's appearance using the parameters provided in the
     *                {@link com.glia.widgets.UiTheme}
     */
    public void setTheme(UiTheme uiTheme) {
        if (uiTheme == null) return;
        this.theme = Utils.getFullHybridTheme(uiTheme, this.theme);
        setupViewAppearance();
        if (getVisibility() == VISIBLE) {
            handleStatusbarColor();
        }
    }

    /**
     * Used to start the chat functionality.
     *
     * @param companyName Text shown in the chat while waiting in a queue.
     * @param queueId     The queue id to which you would like to queue to and speak to operators from.
     * @param contextUrl  Provide some context as to from where are you initiating the chat from.
     */
    public void startChat(
            String companyName,
            String queueId,
            String contextUrl) {
        startChat(companyName, queueId, contextUrl, false, null);
    }

    /**
     * @param companyName Text shown in the chat while waiting in a queue.
     * @param queueId     The queue id to which you would like to queue to and speak to operators from.
     * @param contextUrl  Provide some context as to from where are you initiating the chat from.
     * @param useOverlays Used to set if the user opted to use overlays or not.
     *                    See {@link com.glia.widgets.GliaWidgets}.USE_OVERLAY to see its full
     *                    usage description.
     *                    Important! This parameter is ignored if the view is not used in the sdk's
     *                    ChatActivity
     */
    public void startChat(
            String companyName,
            String queueId,
            String contextUrl,
            boolean useOverlays,
            ScreenSharing.Mode screenSharingMode
    ) {
        Dependencies.getSdkConfigurationManager().setUseOverlay(useOverlays);
        Dependencies.getSdkConfigurationManager().setScreenSharingMode(screenSharingMode);
        if (controller != null) {
            controller.initChat(companyName, queueId, contextUrl);
        }
        if (serviceChatHeadController != null) {
            serviceChatHeadController.init();
        }
    }

    /**
     * Used to force the view to be visible. Will show the current state of the view.
     */
    public void show() {
        if (controller != null) {
            controller.show();
        }
    }

    /**
     * Used to tell the view that the user has pressed the back button so that the view can
     * set its state accordingly.
     */
    public boolean backPressed() {
        if (controller != null) controller.onBackArrowClicked();
        return true;
    }

    /**
     * Add a listener here if you wish to be notified when the user clicks the up button on the
     * appbar.
     *
     * @param onBackClicked The callback which is fired when the button is clicked.
     */
    public void setOnBackClickedListener(OnBackClickedListener onBackClicked) {
        this.onBackClickedListener = onBackClicked;
    }

    /**
     * Add a listener here to be notified if for any reason the chat should end.
     *
     * @param onEndListener The callback which is fired when the chat ends.
     */
    public void setOnEndListener(OnEndListener onEndListener) {
        this.onEndListener = onEndListener;
    }

    /**
     * Add a listener here for when the user has accepted an audio or video call and should navigate
     * to a call.
     * Important! Should be used together with {@link #navigateToCallSuccess()} to notify the view
     * of a completed navigation.
     *
     * @param onNavigateToCallListener The callback which is fired when the user accepts a media
     *                                 upgrade offer.
     */
    public void setOnNavigateToCallListener(OnNavigateToCallListener onNavigateToCallListener) {
        this.onNavigateToCallListener = onNavigateToCallListener;
    }

    /**
     * Use this method to notify the view when your activity or fragment is back in its resumed
     * state.
     */
    public void onResume(boolean needToInitialize) {
        if (controller != null) {
            controller.onResume();
        }
        if (screenSharingController != null) {
            screenSharingController.setViewCallback(screenSharingViewCallback);
            screenSharingController.onResume(this.getContext());
        }
        if (dialogController != null) {
            dialogController.addCallback(dialogCallback);
        }
        if (needToInitialize && serviceChatHeadController != null) {
            serviceChatHeadController.onResume(this);
        }
    }

    public void onPause() {
        if (controller != null) {
            controller.onPause();
        }
        if (screenSharingController != null) {
            screenSharingController.removeViewCallback(screenSharingViewCallback);
        }
        if (dialogController != null) {
            dialogController.removeCallback(dialogCallback);
        }
    }

    /**
     * Use this method to notify the view that your activity or fragment's view is being destroyed.
     * Used to dispose of any loose resources.
     *
     * @param isFinishing - indicates if activity is being recreated - "isFinishing = false" or is being completely destroyed - "isFinishing = true"
     */
    public void onDestroyView(boolean isFinishing) {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
        onEndListener = null;
        onBackClickedListener = null;
        onNavigateToCallListener = null;
        destroyController();
        callback = null;
        adapter.unregisterAdapterDataObserver(dataObserver);
        chatRecyclerView.setAdapter(null);
        chatRecyclerView.removeOnScrollListener(onScrollListener);
        attachmentsRecyclerView.setAdapter(null);
        if (isFinishing) serviceChatHeadController.onDestroy();
        dialogController = null;
    }

    /**
     * Use this method together with {@link #setOnNavigateToCallListener(OnNavigateToCallListener)}
     * to notify the view that you have finished navigating.
     */
    public void navigateToCallSuccess() {
        if (controller != null) {
            controller.navigateToCallSuccess();
        }
    }

    private void setupControllers() {
        setupChatStateCallback();

        controller = Dependencies
                .getControllerFactory()
                .getChatController(callback);

        setupDialogCallback();

        dialogController = Dependencies
                .getControllerFactory()
                .getDialogController();

        screenSharingController = Dependencies
                .getControllerFactory()
                .getScreenSharingController();

        serviceChatHeadController = Dependencies
                .getControllerFactory()
                .getChatHeadController();
    }

    private void setupChatStateCallback() {
        callback = new ChatViewCallback() {
            @Override
            public void emitUploadAttachments(List<FileAttachment> attachments) {
                post(() -> uploadAttachmentAdapter.submitList(attachments));
            }

            @Override
            public void emitState(ChatState chatState) {
                // some state updates on core-sdk are coming from the computation thread
                // need to update state on uiThread
                post(() -> {
                    updateShowSendButton(chatState);
                    updateChatEditText(chatState);
                    updateAppBar(chatState);

                    newMessagesLayout.setVisibility(
                            chatState.showMessagesUnseenIndicator() ? VISIBLE : GONE
                    );
                    newMessagesOperatorStatusView.showPlaceHolder();
                    if (chatState.operatorProfileImgUrl != null) {
                        newMessagesOperatorStatusView.showProfileImage(chatState.operatorProfileImgUrl);
                    } else {
                        newMessagesOperatorStatusView.showPlaceHolder();
                    }
                    isInBottom = chatState.isChatInBottom;
                    newMessagesCountBadgeView.setText(String.valueOf(chatState.messagesNotSeen));

                    if (chatState.isVisible) {
                        showChat();
                    } else {
                        hideChat();
                    }

                    if (chatState.isOperatorTyping) {
                        operatorTypingAnimation.setVisibility(VISIBLE);
                    } else {
                        operatorTypingAnimation.setVisibility(GONE);
                    }

                    updateAttachmentButton(chatState);
                });
            }

            @Override
            public void emitItems(List<ChatItem> items) {
                List<ChatItem> updatedItems = items.stream()
                        .map(chatItem -> updateIsFileDownloaded(chatItem))
                        .collect(Collectors.toList());

                post(() -> adapter.submitList(updatedItems));
            }

            @Override
            public void navigateToCall(String mediaType) {
                if (onNavigateToCallListener != null) {
                    onNavigateToCallListener.call(theme, mediaType);
                }
            }

            @Override
            public void destroyView() {
                if (onEndListener != null) {
                    onEndListener.onEnd();
                }
            }

            @Override
            public void smoothScrollToBottom() {
                post(() -> chatRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1));
            }

            @Override
            public void scrollToBottomImmediate() {
                post(() -> chatRecyclerView.scrollToPosition(adapter.getItemCount() - 1));
            }

            @Override
            public void fileDownloadError(AttachmentFile attachmentFile, Throwable error) {
                fileDownloadFailed(attachmentFile);
            }

            @Override
            public void fileDownloadSuccess(AttachmentFile attachmentFile) {
                fileDownloadCompleted(attachmentFile);
            }

            @Override
            public void clearMessageInput() {
                chatEditText.removeTextChangedListener(textWatcher);
                chatEditText.getText().clear();
                chatEditText.addTextChangedListener(textWatcher);
            }
        };
    }

    private void setupDialogCallback() {
        dialogCallback = dialogsState -> {
            if (dialogsState instanceof DialogsState.NoDialog) {
                post(() -> {
                    if (alertDialog != null) {
                        alertDialog.dismiss();
                        alertDialog = null;
                    }
                });
            } else if (dialogsState instanceof DialogsState.UnexpectedErrorDialog) {
                post(this::showUnexpectedErrorDialog);
            } else if (dialogsState instanceof DialogsState.ExitQueueDialog) {
                post(this::showExitQueueDialog);
            } else if (dialogsState instanceof DialogsState.OverlayPermissionsDialog) {
                post(this::showOverlayPermissionsDialog);
            } else if (dialogsState instanceof DialogsState.EndEngagementDialog) {
                post(() -> showEndEngagementDialog(((DialogsState.EndEngagementDialog) dialogsState).operatorName));
            } else if (dialogsState instanceof DialogsState.UpgradeDialog) {
                post(() -> showUpgradeDialog(((DialogsState.UpgradeDialog) dialogsState).type));
            } else if (dialogsState instanceof DialogsState.NoMoreOperatorsDialog) {
                post(this::showNoMoreOperatorsAvailableDialog);
            } else if (dialogsState instanceof DialogsState.EngagementEndedDialog) {
                post(this::showEngagementEndedDialog);
            } else if (dialogsState instanceof DialogsState.StartScreenSharingDialog) {
                post(this::showScreenSharingDialog);
            } else if (dialogsState instanceof DialogsState.EnableNotificationChannelDialog) {
                post(this::showAllowNotificationsDialog);
            } else if (dialogsState instanceof DialogsState.EnableScreenSharingNotificationsAndStartSharingDialog) {
                post(this::showAllowScreenSharingNotificationsAndStartSharingDialog);
            }
        };
    }

    private void updateAttachmentButton(ChatState chatState) {
        addAttachmentButton.setEnabled(chatState.isAttachmentButtonEnabled);
        addAttachmentButton.setVisibility(chatState.isAttachmentButtonVisible() ? VISIBLE : GONE);
    }

    private ChatItem updateIsFileDownloaded(ChatItem item) {
        if (item instanceof OperatorAttachmentItem) {
            OperatorAttachmentItem oldItem = (OperatorAttachmentItem) item;
            boolean isFileDownloaded = FileHelper.isFileDownloaded(oldItem.attachmentFile);
            return new OperatorAttachmentItem(
                    oldItem.getId(),
                    oldItem.getViewType(),
                    oldItem.showChatHead,
                    oldItem.attachmentFile,
                    oldItem.operatorProfileImgUrl,
                    isFileDownloaded,
                    oldItem.isDownloading
            );
        } else if (item instanceof VisitorAttachmentItem) {
            VisitorAttachmentItem oldItem = (VisitorAttachmentItem) item;
            boolean isFileDownloaded = FileHelper.isFileDownloaded(oldItem.attachmentFile);
            return new VisitorAttachmentItem(
                    oldItem.getId(),
                    oldItem.getViewType(),
                    oldItem.attachmentFile,
                    isFileDownloaded,
                    oldItem.isDownloading,
                    oldItem.showDelivered
            );
        } else {
            return item;
        }
    }

    private void updateShowSendButton(ChatState chatState) {
        if (chatState.showSendButton && sendButton.getVisibility() != VISIBLE) {
            sendButton.setVisibility(VISIBLE);
        }

        if (!chatState.showSendButton && sendButton.getVisibility() == VISIBLE) {
            sendButton.setVisibility(GONE);
        }
    }

    private void updateChatEditText(ChatState chatState) {
        switch (chatState.chatInputMode) {
            case SINGLE_CHOICE_CARD:
                chatEditText.setHint(R.string.glia_chat_single_choice_card_hint);
                break;
            case ENABLED_NO_ENGAGEMENT:
                chatEditText.setHint(R.string.glia_chat_not_started_hint);
                break;
            default:
                chatEditText.setHint(R.string.glia_chat_enter_message);
                break;
        }

        chatEditText.setEnabled(chatState.chatInputMode == ChatInputMode.ENABLED ||
                chatState.chatInputMode == ChatInputMode.ENABLED_NO_ENGAGEMENT);
    }

    private void updateAppBar(ChatState chatState) {
        if (chatState.isOperatorOnline()) {
            appBar.showEndButton();
        } else if (chatState.engagementRequested) {
            appBar.showXButton();
        } else {
            appBar.hideLeaveButtons();
        }
    }

    private void showAllowScreenSharingNotificationsAndStartSharingDialog() {
        if (alertDialog == null || !alertDialog.isShowing()) {
            alertDialog = Dialogs.showOptionsDialog(
                    this.getContext(),
                    this.theme,
                    resources.getString(R.string.glia_dialog_screen_sharing_offer_enable_notifications_title),
                    resources.getString(R.string.glia_dialog_screen_sharing_offer_enable_notifications_message),
                    resources.getString(R.string.glia_dialog_screen_sharing_offer_enable_notifications_yes),
                    resources.getString(R.string.glia_dialog_screen_sharing_offer_enable_notifications_no),
                    view -> {
                        dismissAlertDialog();
                        NotificationManager.openNotificationChannelScreen(this.getContext());
                    },
                    view -> {
                        dismissAlertDialog();
                        controller.notificationsDialogDismissed();
                        screenSharingController.onScreenSharingDeclined();
                    },
                    dialog -> {
                        dialog.dismiss();
                        controller.notificationsDialogDismissed();
                        screenSharingController.onScreenSharingDeclined();
                    }
            );
        }
    }

    private void showAllowNotificationsDialog() {
        if (alertDialog == null || !alertDialog.isShowing()) {
            alertDialog = Dialogs.showOptionsDialog(
                    this.getContext(),
                    this.theme,
                    resources.getString(R.string.glia_dialog_allow_notifications_title),
                    resources.getString(R.string.glia_dialog_allow_notifications_message),
                    resources.getString(R.string.glia_dialog_allow_notifications_yes),
                    resources.getString(R.string.glia_dialog_allow_notifications_no),
                    view -> {
                        dismissAlertDialog();
                        controller.notificationsDialogDismissed();
                        NotificationManager.openNotificationChannelScreen(this.getContext());
                    },
                    view -> {
                        dismissAlertDialog();
                        controller.notificationsDialogDismissed();
                    },
                    dialog -> {
                        dialog.dismiss();
                        controller.notificationsDialogDismissed();
                    }
            );
        }
    }

    private void showScreenSharingDialog() {
        if (alertDialog == null || !alertDialog.isShowing()) {
            alertDialog = Dialogs.showScreenSharingDialog(
                    this.getContext(),
                    theme,
                    resources.getText(R.string.glia_dialog_screen_sharing_offer_title).toString(),
                    resources.getText(R.string.glia_dialog_screen_sharing_offer_message).toString(),
                    R.string.glia_dialog_screen_sharing_offer_accept,
                    R.string.glia_dialog_screen_sharing_offer_decline,
                    view -> screenSharingController.onScreenSharingAccepted(getContext()),
                    view -> screenSharingController.onScreenSharingDeclined()
            );
        }
    }

    private void showChat() {
        setVisibility(VISIBLE);
        handleStatusbarColor();
    }

    private void hideChat() {
        setVisibility(INVISIBLE);
        Activity activity = Utils.getActivity(this.getContext());
        if (defaultStatusbarColor != null && activity != null) {
            activity.getWindow().setStatusBarColor(defaultStatusbarColor);
            defaultStatusbarColor = null;
        }
        Utils.hideSoftKeyboard(this.getContext(), getWindowToken());
    }

    private void showToolbar(String title) {
        appBar.setTitle(title);
        appBar.showToolbar();
    }

    private void destroyController() {
        if (controller != null) {
            controller.onDestroy(Utils.getActivity(this.getContext()) instanceof ChatActivity);
        }
        controller = null;
    }

    private void readTypedArray(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        @SuppressLint("CustomViewStyleable") TypedArray typedArray = this.getContext().obtainStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes);
        setDefaultTheme(typedArray);
        typedArray.recycle();
    }

    private void setDefaultTheme(TypedArray typedArray) {
        this.theme = Utils.getThemeFromTypedArray(typedArray, this.getContext());
    }

    private void initConfigurations() {
        setVisibility(INVISIBLE);
        // needed to overlap existing app bar in existing view with this view's app bar.
        ViewCompat.setElevation(this, 100.0f);
    }

    private void initViews() {
        View view = View.inflate(this.getContext(), R.layout.chat_view, this);
        chatRecyclerView = view.findViewById(R.id.chat_recycler_view);
        sendButton = view.findViewById(R.id.send_button);
        addAttachmentButton = view.findViewById(R.id.add_attachment_button);
        chatEditText = view.findViewById(R.id.chat_edit_text);
        appBar = view.findViewById(R.id.app_bar_view);
        dividerView = view.findViewById(R.id.divider_view);
        newMessagesLayout = view.findViewById(R.id.new_messages_indicator_layout);
        newMessagesCardView = view.findViewById(R.id.new_messages_indicator_card);
        newMessagesOperatorStatusView = view.findViewById(R.id.new_messages_indicator_image);
        newMessagesCountBadgeView = view.findViewById(R.id.new_messages_badge_view);
        chatMessageLayout = view.findViewById(R.id.chat_message_layout);
        attachmentsRecyclerView = view.findViewById(R.id.add_attachment_queue);
        operatorTypingAnimation = view.findViewById(R.id.operator_typing_animation_view);
    }

    private void setupViewAppearance() {
        adapter = new ChatAdapter(
                this.theme,
                this.onOptionClickedListener,
                this,
                this,
                Dependencies.getUseCaseFactory().createGetImageFileFromCacheUseCase(),
                Dependencies.getUseCaseFactory().createGetImageFileFromDownloadsUseCase(),
                Dependencies.getUseCaseFactory().createGetImageFileFromNetworkUseCase()
        );
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        adapter.registerAdapterDataObserver(dataObserver);
        chatRecyclerView.setAdapter(adapter);
        chatRecyclerView.addOnScrollListener(onScrollListener);

        uploadAttachmentAdapter = new UploadAttachmentAdapter();
        uploadAttachmentAdapter.setItemCallback(attachment -> controller.onRemoveAttachment(attachment));
        uploadAttachmentAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                attachmentsRecyclerView.smoothScrollToPosition(uploadAttachmentAdapter.getItemCount());
            }
        });

        attachmentsRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        attachmentsRecyclerView.setAdapter(uploadAttachmentAdapter);

        appBar.setTheme(this.theme);

        //icons
        sendButton.setImageResource(this.theme.getIconSendMessage());

        // new messages indicator shape
        ShapeAppearanceModel shapeAppearanceModel = newMessagesCardView.getShapeAppearanceModel()
                .toBuilder()
                .setBottomEdge(new MarkerEdgeTreatment(
                        resources.getDimension(R.dimen.glia_chat_new_messages_bottom_edge_radius)
                ))
                .build();
        newMessagesOperatorStatusView.isRippleAnimationShowing(false);
        newMessagesCardView.setShapeAppearanceModel(shapeAppearanceModel);
        newMessagesCountBadgeView.setBackgroundTintList(
                ContextCompat.getColorStateList(
                        this.getContext(), theme.getBrandPrimaryColor()
                )
        );
        newMessagesCountBadgeView.setTextColor(
                ContextCompat.getColor(this.getContext(), theme.getBaseLightColor())
        );

        // colors
        dividerView.setBackgroundColor(ContextCompat.getColor(
                this.getContext(),
                this.theme.getBaseShadeColor()));
        sendButton.setImageTintList(
                ContextCompat.getColorStateList(
                        this.getContext(),
                        this.theme.getSendMessageButtonTintColor()));
        chatEditText.setTextColor(ContextCompat.getColor(
                this.getContext(), this.theme.getBaseDarkColor()));
        chatEditText.setHintTextColor(ContextCompat.getColor(
                this.getContext(), this.theme.getBaseNormalColor()));
        setBackgroundColor(
                ContextCompat.getColor(this.getContext(), this.theme.getGliaChatBackgroundColor()));
        // fonts
        if (this.theme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(
                    this.getContext(),
                    this.theme.getFontRes()
            );
            chatEditText.setTypeface(fontFamily);
        }
        // texts
        if (this.theme.getAppBarTitle() != null) {
            showToolbar(this.theme.getAppBarTitle());
        }

        operatorTypingAnimation.addValueCallback(
                new KeyPath("**"),
                LottieProperty.COLOR_FILTER,
                frameInfo -> new PorterDuffColorFilter(ContextCompat.getColor(this.getContext(), this.theme.getBrandPrimaryColor()), PorterDuff.Mode.SRC_ATOP)
        );
    }

    private void handleStatusbarColor() {
        Activity activity = Utils.getActivity(this.getContext());
        if (activity != null && defaultStatusbarColor == null) {
            defaultStatusbarColor = activity.getWindow().getStatusBarColor();
            if (controller != null && controller.isChatVisible()) {
                activity.getWindow().setStatusBarColor(ContextCompat.getColor(
                        this.getContext(), this.theme.getBrandPrimaryColor()));
            }
        }
    }

    private void setupViewActions() {
        chatEditText.addTextChangedListener(textWatcher);

        sendButton.setOnClickListener(view -> {
            String message = chatEditText.getText().toString().trim();
            if (controller != null) {
                controller.sendMessage(message);
            }
        });

        setupAddAttachmentButton();

        appBar.setOnBackClickedListener(() -> {
            if (onBackClickedListener != null) {
                onBackClickedListener.onBackClicked();
            }
        });
        appBar.setOnEndChatClickedListener(() -> {
            if (controller != null) {
                controller.leaveChatClicked();
            }
        });
        appBar.setOnXClickedListener(() -> {
            if (controller != null) {
                controller.leaveChatQueueClicked();
            }
        });

        newMessagesCardView.setOnClickListener(v -> {
            if (controller != null) {
                controller.newMessagesIndicatorClicked();
            }
        });
    }

    private void setupAddAttachmentButton() {
        addAttachmentButton.setOnClickListener(view ->
                AttachmentPopup.show(
                        chatMessageLayout,
                        new AttachmentPopup.Callback() {
                            @Override
                            public void onGalleryClicked() {
                                Intent intent = new Intent();
                                intent.setType("image/*");
                                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                                Utils.getActivity(getContext()).startActivityForResult(
                                        Intent.createChooser(intent, "Select Picture"),
                                        OPEN_DOCUMENT_ACTION_REQUEST
                                );
                            }

                            @Override
                            public void onTakePhotoClicked() {
                                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    dispatchImageCapture();
                                } else {
                                    Utils.getActivity(getContext())
                                            .requestPermissions(
                                                    new String[]{Manifest.permission.CAMERA},
                                                    CAMERA_PERMISSION_REQUEST
                                            );
                                }
                            }

                            @Override
                            public void onBrowseClicked() {
                                Intent intent = new Intent();
                                intent.setType("*/*");
                                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                                Utils.getActivity(getContext()).startActivityForResult(
                                        Intent.createChooser(intent, "Select file"),
                                        OPEN_DOCUMENT_ACTION_REQUEST
                                );
                            }
                        }
                ));
    }

    private void dispatchImageCapture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = Utils.createTempPhotoFile(getContext());
        } catch (IOException exception) {
            Logger.e(TAG, "Create photo file failed: " + exception.getMessage());
        }

        if (photoFile != null) {
            controller.setPhotoCaptureFileUri(
                    FileProvider.getUriForFile(
                            getContext(),
                            FileHelper.getFileProviderAuthority(getContext()),
                            photoFile)
            );
            if (controller.getPhotoCaptureFileUri() != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, controller.getPhotoCaptureFileUri());
                Utils.getActivity(getContext()).startActivityForResult(
                        intent,
                        CAPTURE_IMAGE_ACTION_REQUEST
                );
            }
        }
    }

    private void showExitQueueDialog() {
        alertDialog = Dialogs.showOptionsDialog(
                this.getContext(),
                this.theme,
                resources.getString(R.string.glia_dialog_leave_queue_title),
                resources.getString(R.string.glia_dialog_leave_queue_message),
                resources.getString(R.string.glia_dialog_leave_queue_yes),
                resources.getString(R.string.glia_dialog_leave_queue_no),
                v -> {
                    dismissAlertDialog();
                    if (controller != null) {
                        controller.endEngagementDialogYesClicked();
                    }
                    if (onEndListener != null) {
                        onEndListener.onEnd();
                    }
                    chatEnded();
                },
                v -> {
                    dismissAlertDialog();
                    if (controller != null) {
                        controller.endEngagementDialogDismissed();
                    }
                },
                dialog -> {
                    dialog.dismiss();
                    if (controller != null) {
                        controller.endEngagementDialogDismissed();
                    }
                },
                true
        );
    }

    private void showEndEngagementDialog(String operatorName) {
        alertDialog = Dialogs.showOptionsDialog(
                this.getContext(),
                this.theme,
                resources.getString(R.string.glia_dialog_end_engagement_title),
                resources.getString(R.string.glia_dialog_end_engagement_message, operatorName),
                resources.getString(R.string.glia_dialog_end_engagement_yes),
                resources.getString(R.string.glia_dialog_end_engagement_no),
                v -> {
                    dismissAlertDialog();
                    if (controller != null) {
                        controller.endEngagementDialogYesClicked();
                    }
                    if (onEndListener != null) {
                        onEndListener.onEnd();
                    }
                    chatEnded();
                },
                v -> {
                    dismissAlertDialog();
                    if (controller != null) {
                        controller.endEngagementDialogDismissed();
                    }
                },
                dialog -> {
                    if (controller != null) {
                        controller.endEngagementDialogDismissed();
                    }
                    dialog.dismiss();
                },
                true
        );
    }

    private void showOptionsDialog(String title,
                                   String message,
                                   String positiveButtonText,
                                   String neutralButtonText,
                                   View.OnClickListener positiveButtonClickListener,
                                   View.OnClickListener neutralButtonClickListener,
                                   DialogInterface.OnCancelListener cancelListener) {
        alertDialog = Dialogs.showOptionsDialog(
                this.getContext(),
                this.theme,
                title,
                message,
                positiveButtonText,
                neutralButtonText,
                positiveButtonClickListener,
                neutralButtonClickListener,
                cancelListener
        );
    }

    private void showAlertDialog(@StringRes int title,
                                 @StringRes int message,
                                 View.OnClickListener buttonClickListener) {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
        alertDialog = Dialogs.showAlertDialog(
                this.getContext(),
                this.theme,
                title,
                message,
                buttonClickListener);
    }

    private void showEngagementEndedDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
        alertDialog = Dialogs.showOperatorEndedEngagementDialog(
                this.getContext(),
                this.theme,
                v -> {
                    dismissAlertDialog();
                    if (controller != null) {
                        controller.noMoreOperatorsAvailableDismissed();
                    }
                    if (onEndListener != null) {
                        onEndListener.onEnd();
                    }
                    chatEnded();
                });
    }

    private void showNoMoreOperatorsAvailableDialog() {
        showAlertDialog(
                R.string.glia_dialog_operators_unavailable_title,
                R.string.glia_dialog_operators_unavailable_message,
                v -> {
                    dismissAlertDialog();
                    if (controller != null) {
                        controller.noMoreOperatorsAvailableDismissed();
                    }
                    if (onEndListener != null) {
                        onEndListener.onEnd();
                    }
                    chatEnded();
                });
    }

    private void showUpgradeDialog(DialogOfferType type) {
        alertDialog = Dialogs.showUpgradeDialog(
                this.getContext(),
                theme,
                type,
                v -> {
                    if (controller != null) {
                        controller.acceptUpgradeOfferClicked(type.getUpgradeOffer());
                    }
                },
                v -> {
                    if (controller != null) {
                        controller.declineUpgradeOfferClicked(type.getUpgradeOffer());
                    }
                });
    }

    private void showUnexpectedErrorDialog() {
        showAlertDialog(
                R.string.glia_dialog_unexpected_error_title,
                R.string.glia_dialog_unexpected_error_message,
                v -> {
                    dismissAlertDialog();
                    if (controller != null) {
                        controller.unexpectedErrorDialogDismissed();
                    }
                    if (onEndListener != null) {
                        onEndListener.onEnd();
                    }
                }
        );
    }

    private void showOverlayPermissionsDialog() {
        showOptionsDialog(
                resources.getString(R.string.glia_dialog_overlay_permissions_title),
                resources.getString(R.string.glia_dialog_overlay_permissions_message),
                resources.getString(R.string.glia_dialog_overlay_permissions_ok),
                resources.getString(R.string.glia_dialog_overlay_permissions_no),
                v -> {
                    dismissAlertDialog();
                    if (controller != null) {
                        controller.overlayPermissionsDialogDismissed();
                    }
                    Intent overlayIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + this.getContext().getPackageName()));
                    overlayIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    this.getContext().startActivity(overlayIntent);
                },
                v -> {
                    dismissAlertDialog();
                    if (controller != null) {
                        controller.overlayPermissionsDialogDismissed();
                    }
                },
                dialog -> {
                    if (controller != null) {
                        controller.overlayPermissionsDialogDismissed();
                    }
                    dialog.dismiss();
                }
        );
    }

    private void chatEnded() {
        Dependencies.getControllerFactory().destroyControllers();
    }

    private void dismissAlertDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (resultCode != Activity.RESULT_OK) return;

        switch (requestCode) {
            case OPEN_DOCUMENT_ACTION_REQUEST:
            case CAPTURE_VIDEO_ACTION_REQUEST:
                handleDocumentOrVideoActionResult(intent);
                break;
            case CAPTURE_IMAGE_ACTION_REQUEST:
                handleCaptureImageActionResult();
                break;
        }
    }

    private void handleDocumentOrVideoActionResult(Intent intent) {
        Uri uri = intent != null ? intent.getData() : null;
        if (uri != null) {
            controller.onAttachmentReceived(Utils.mapUriToFileAttachment(getContext().getContentResolver(), uri));
        }
    }

    private void handleCaptureImageActionResult() {
        Uri photoCaptureFileUri = controller != null ? controller.getPhotoCaptureFileUri() : null;
        if (photoCaptureFileUri != null) {
            FileHelper.fixCapturedPhotoRotation(getContext(), photoCaptureFileUri);
            FileAttachment fa = Utils.mapUriToFileAttachment(
                    getContext().getContentResolver(),
                    photoCaptureFileUri
            );
            controller.onAttachmentReceived(fa);
        }
    }

    @Override
    public void onFileDownloadClick(AttachmentFile attachmentFile) {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            onFileDownload(attachmentFile);
        } else {
            Utils.getActivity(getContext()).requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
            downloadFileHolder = attachmentFile;
        }
    }

    private void onFileDownload(AttachmentFile attachmentFile) {
        submitUpdatedItems(attachmentFile, true, false);
        controller.onFileDownloadClicked(attachmentFile);
    }

    public void fileDownloadFailed(AttachmentFile file) {
        submitUpdatedItems(file, false, false);
        Toast.makeText(this.getContext(), this.getContext().getString(R.string.glia_chat_file_download_failed_msg), Toast.LENGTH_SHORT).show();
    }

    public void fileDownloadCompleted(AttachmentFile file) {
        submitUpdatedItems(file, false, true);
        Toast.makeText(this.getContext(), getContext().getString(R.string.glia_chat_file_download_success_message), Toast.LENGTH_LONG).show();
    }

    private void submitUpdatedItems(AttachmentFile attachmentFile, boolean isDownloading, boolean isFileExists) {
        List<ChatItem> updatedItems = adapter.getCurrentList()
                .stream()
                .map(currentItem -> updatedDownloadingItemState(attachmentFile, currentItem, isDownloading, isFileExists))
                .collect(Collectors.toList());

        adapter.submitList(updatedItems);
    }

    @NonNull
    private ChatItem updatedDownloadingItemState(AttachmentFile attachmentFile, ChatItem currentChatItem, boolean isDownloading, boolean isFileExists) {
        if (currentChatItem instanceof VisitorAttachmentItem) {
            VisitorAttachmentItem currentItem = (VisitorAttachmentItem) currentChatItem;
            if (currentItem.attachmentFile.getId().equals(attachmentFile.getId())) {
                return new VisitorAttachmentItem(
                        currentItem.getId(),
                        currentItem.getViewType(),
                        currentItem.attachmentFile,
                        isFileExists,
                        isDownloading,
                        currentItem.showDelivered
                );
            }
        } else if (currentChatItem instanceof OperatorAttachmentItem) {
            OperatorAttachmentItem currentItem = (OperatorAttachmentItem) currentChatItem;
            if (currentItem.attachmentFile.getId().equals(attachmentFile.getId())) {
                return new OperatorAttachmentItem(
                        currentItem.getId(),
                        currentItem.getViewType(),
                        currentItem.showChatHead,
                        currentItem.attachmentFile,
                        currentItem.operatorProfileImgUrl,
                        isFileExists,
                        isDownloading);
            }
        }
        return currentChatItem;
    }

    @Override
    public void onFileOpenClick(AttachmentFile attachment) {
        Uri contentUri;
        Context context = getContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Uri downloadsContentUri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL);
            String[] projection = new String[]{
                    MediaStore.Downloads._ID,
                    MediaStore.Downloads.DISPLAY_NAME,
                    MediaStore.Downloads.SIZE
            };
            String selection = MediaStore.Downloads.DISPLAY_NAME +
                    " == ?";
            String[] selectionArgs = new String[]{FileHelper.getFileName(attachment)};
            String sortOrder = MediaStore.Downloads.DISPLAY_NAME + " ASC";
            try (Cursor cursor = context.getContentResolver().query(
                    downloadsContentUri,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
            )) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID);
                cursor.moveToFirst();
                long id = cursor.getLong(idColumn);
                cursor.close();
                contentUri = ContentUris.withAppendedId(downloadsContentUri, id);
            }
        } else {
            File file = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(),
                    FileHelper.getFileName(attachment)
            );
            contentUri = FileProvider.getUriForFile(getContext(), FileHelper.getFileProviderAuthority(context), file);
        }

        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        openIntent.setDataAndType(contentUri, attachment.getContentType());
        openIntent.setClipData(ClipData.newRawUri("", contentUri));
        context.startActivity(openIntent);
    }

    @Override
    public void onImageItemClick(AttachmentFile file) {
        this.getContext().startActivity(FilePreviewActivity.intent(this.getContext(), file.getId(), file.getName(), theme));
    }

    public void onRequestPermissionsResult(int requestCode, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case CAMERA_PERMISSION_REQUEST:
                    dispatchImageCapture();
                    break;
                case WRITE_PERMISSION_REQUEST:
                    if (downloadFileHolder != null) onFileDownload(downloadFileHolder);
                    break;
            }
        }
    }

    public void setConfiguration(GliaSdkConfiguration configuration) {
        serviceChatHeadController.setBuildTimeTheme(theme);
        serviceChatHeadController.setSdkConfiguration(configuration);
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    public interface OnBackClickedListener {
        /**
         * Callback which is used to notify the enclosing activity or fragment when the user
         * clicks on the view's top app bar's up button.
         */
        void onBackClicked();
    }

    public interface OnEndListener {
        /**
         * Callback which is fired when the chat is ended. End can happen due to the user clicking
         * on the end engagement button or the leave queue button.
         */
        void onEnd();
    }

    public interface OnNavigateToCallListener {
        /**
         * Callback which is fired when the user has accepted a media upgrade offer and should be
         * navigated to a view where they can visually see data about their media upgrade.
         *
         * @param theme Used to pass the finalized {@link UiTheme}
         *              to the activity which is being navigated to.
         */
        void call(UiTheme theme, String mediaType);
    }
}
