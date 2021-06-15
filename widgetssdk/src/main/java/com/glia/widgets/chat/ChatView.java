package com.glia.widgets.chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.adapter.ChatAdapter;
import com.glia.widgets.chat.adapter.ChatItem;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.dialog.DialogController;
import com.glia.widgets.head.ChatHeadService;
import com.glia.widgets.head.ChatHeadsController;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.model.ChatHeadInput;
import com.glia.widgets.model.DialogsState;
import com.glia.widgets.notification.NotificationFactory;
import com.glia.widgets.notification.device.NotificationManager;
import com.glia.widgets.screensharing.ScreenSharingController;
import com.glia.widgets.view.AppBarView;
import com.glia.widgets.view.DialogOfferType;
import com.glia.widgets.view.Dialogs;
import com.glia.widgets.view.OperatorStatusView;
import com.glia.widgets.view.SingleChoiceCardView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.shape.MarkerEdgeTreatment;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;

import java.util.List;

public class ChatView extends ConstraintLayout {

    private final static String TAG = "ChatView";
    private AlertDialog alertDialog;

    private ChatViewCallback callback;
    private ChatController controller;
    private ChatHeadsController chatHeadsController;

    private DialogController.Callback dialogCallback;
    private DialogController dialogController;

    private ScreenSharingController screenSharingController;
    private ScreenSharingController.ViewCallback screenSharingCallback;

    private RecyclerView chatRecyclerView;
    private ImageButton sendButton;
    private EditText chatEditText;
    private ChatAdapter adapter;
    private AppBarView appBar;
    private View dividerView;
    private RelativeLayout newMessagesLayout;
    private MaterialCardView newMessagesCardView;
    private OperatorStatusView newMessagesOperatorStatusView;
    private TextView newMessagesCountBadgeView;

    private boolean isInBottom = true;

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
    private final SingleChoiceCardView.OnImageLoadedListener onImageLoadedListener = new SingleChoiceCardView.OnImageLoadedListener() {
        @Override
        public void onLoaded() {
            Logger.d(TAG, "onSingleChoiceCardViewImageLoaded, scroll to bottom");
            chatRecyclerView.smoothScrollToPosition(adapter.getItemCount());
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
        initControls();
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
     * @param companyName        Text shown in the chat while waiting in a queue.
     * @param queueId            The queue id to which you would like to queue to and speak to operators from.
     * @param contextUrl         Provide some context as to from where are you initiating the chat from.
     * @param useOverlays        Used to set if the user opted to use overlays or not.
     *                           See {@link com.glia.widgets.GliaWidgets}.USE_OVERLAY to see its full
     *                           usage description.
     *                           Important! This parameter is ignored if the view is not used in the sdk's
     *                           ChatActivity
     * @param savedInstanceState Used to see if the activity is being created for the first time.
     */
    public void startChat(
            String companyName,
            String queueId,
            String contextUrl,
            boolean useOverlays,
            Bundle savedInstanceState) {
        Activity activity = Utils.getActivity(this.getContext());
        boolean isConfigurationChange = activity instanceof ChatActivity &&
                savedInstanceState != null;
        if (!isConfigurationChange) {
            if (chatHeadsController != null) {
                chatHeadsController.onNavigatedToChat(
                        new ChatHeadInput(
                                companyName,
                                queueId,
                                contextUrl,
                                this.theme
                        ),
                        activity instanceof ChatActivity,
                        activity instanceof ChatActivity && useOverlays
                );
            }
        }
        if (controller != null) {
            controller.initChat(
                    companyName,
                    queueId,
                    contextUrl,
                    Settings.canDrawOverlays(this.getContext()),
                    NotificationManager.areNotificationsEnabled(this.getContext(), NotificationFactory.NOTIFICATION_CALL_CHANNEL_ID),
                    NotificationManager.areNotificationsEnabled(this.getContext(), NotificationFactory.NOTIFICATION_SCREEN_SHARING_CHANNEL_ID)
            );
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
    public void backPressed() {
        if (controller != null) {
            controller.onBackArrowClicked();
        }
        if (chatHeadsController != null) {
            chatHeadsController.onChatBackButtonPressed();
        }
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
    public void onResume() {
        if (controller != null) {
            controller.onResume(Settings.canDrawOverlays(this.getContext()),
                    NotificationManager.areNotificationsEnabled(this.getContext(), NotificationFactory.NOTIFICATION_CALL_CHANNEL_ID),
                    NotificationManager.areNotificationsEnabled(this.getContext(), NotificationFactory.NOTIFICATION_SCREEN_SHARING_CHANNEL_ID));
            if (screenSharingCallback != null)
                screenSharingController.setGliaScreenSharingCallback(screenSharingCallback);
        }
        if (screenSharingController != null) {
            screenSharingController.onResume(this.getContext());
        }
    }

    /**
     * Use this method to notify the view that your activity or fragment's view is being destroyed.
     * Used to dispose of any loose resources.
     */
    public void onDestroyView() {
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

        if (screenSharingController != null) {
            screenSharingController.onDestroy(true);
        }

        if (dialogController != null) {
            dialogController.removeCallback(dialogCallback);
            dialogController = null;
        }
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

    private void initControls() {
        callback = new ChatViewCallback() {
            @Override
            public void emitState(ChatState chatState) {
                post(() -> {
                    switch (chatState.chatInputMode) {
                        case SINGLE_CHOICE_CARD:
                            chatEditText.setHint(R.string.chat_single_choice_card_hint);
                            break;
                        case ENABLED_NO_ENGAGEMENT:
                            if (chatState.lastTypedText.isEmpty()) {
                                chatEditText.setHint(R.string.chat_not_started_hint);
                            } else {
                                chatEditText.setHint("");
                            }
                            break;
                        default:
                            chatEditText.setHint(R.string.chat_enter_message);
                            break;
                    }
                    chatEditText.setEnabled(chatState.chatInputMode == ChatInputMode.ENABLED ||
                            chatState.chatInputMode == ChatInputMode.ENABLED_NO_ENGAGEMENT);
                    if (chatState.isOperatorOnline()) {
                        appBar.showEndButton();
                    } else if (chatState.engagementRequested) {
                        appBar.showXButton();
                    } else {
                        appBar.hideLeaveButtons();
                    }

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
                });
            }

            @Override
            public void emitItems(List<ChatItem> items) {
                post(() -> adapter.submitList(items));
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
            public void setLastTypedText(String lastTypedText) {
                post(() -> chatEditText.setText(lastTypedText));
            }

            @Override
            public void smoothScrollToBottom() {
                post(() -> chatRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1));
            }

            @Override
            public void scrollToBottomImmediate() {
                post(() -> chatRecyclerView.scrollToPosition(adapter.getItemCount() - 1));
            }
        };

        screenSharingCallback = exception -> Toast.makeText(getContext(), exception.debugMessage, Toast.LENGTH_SHORT).show();

        controller = Dependencies
                .getControllerFactory()
                .getChatController(Utils.getActivity(this.getContext()), callback);

        chatHeadsController = Dependencies.getControllerFactory().getChatHeadsController();

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
                post(() -> showEndEngagementDialog(
                        ((DialogsState.EndEngagementDialog) dialogsState).operatorName));
            } else if (dialogsState instanceof DialogsState.UpgradeDialog) {
                post(() -> showUpgradeDialog(((DialogsState.UpgradeDialog) dialogsState).type));
            } else if (dialogsState instanceof DialogsState.NoMoreOperatorsDialog) {
                post(this::showNoMoreOperatorsAvailableDialog);
            } else if (dialogsState instanceof DialogsState.StartScreenSharingDialog) {
                post(this::showScreenSharingDialog);
            } else if (dialogsState instanceof DialogsState.EndScreenSharingDialog) {
                post(this::showScreenSharingEndDialog);
            } else if (dialogsState instanceof DialogsState.EnableNotificationChannelDialog) {
                post(this::showAllowNotificationsDialog);
            } else if (dialogsState instanceof DialogsState.EnableScreenSharingNotificationsAndStartSharingDialog) {
                post(this::showAllowScreenSharingNotificationsAndStartSharingDialog);
            }
        };

        dialogController = Dependencies
                .getControllerFactory()
                .getDialogController(dialogCallback);

        screenSharingController = Dependencies
                .getControllerFactory()
                .getScreenSharingController(screenSharingCallback);
    }

    private void showAllowScreenSharingNotificationsAndStartSharingDialog() {
        if (alertDialog == null || !alertDialog.isShowing()) {
            alertDialog = Dialogs.showOptionsDialog(
                    this.getContext(),
                    this.theme,
                    resources.getString(R.string.dialog_screen_sharing_offer_enable_notifications_title),
                    resources.getString(R.string.dialog_screen_sharing_offer_enable_notifications_message),
                    resources.getString(R.string.dialog_yes),
                    resources.getString(R.string.dialog_no),
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
                    resources.getString(R.string.dialog_allow_notifications_title),
                    resources.getString(R.string.dialog_allow_notifications_message),
                    resources.getString(R.string.dialog_yes),
                    resources.getString(R.string.dialog_no),
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
                    resources.getText(R.string.dialog_screen_sharing_offer_title).toString(),
                    resources.getText(R.string.dialog_screen_sharing_offer_message).toString(),
                    R.string.dialog_accept,
                    R.string.dialog_decline,
                    view -> screenSharingController.onScreenSharingAccepted(getContext()),
                    view -> screenSharingController.onScreenSharingDeclined()
            );
        }
    }

    private void showScreenSharingEndDialog() {
        if (alertDialog == null || !alertDialog.isShowing()) {
            alertDialog = Dialogs.showScreenSharingDialog(
                    this.getContext(),
                    theme,
                    resources.getString(R.string.dialog_screen_sharing_end_title),
                    resources.getString(R.string.dialog_screen_sharing_end_message),
                    R.string.dialog_cancel,
                    R.string.dialog_end_sharing,
                    view -> screenSharingController.onDismissEndScreenSharing(),
                    view -> screenSharingController.onEndScreenSharing()
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
        chatEditText = view.findViewById(R.id.chat_edit_text);
        appBar = view.findViewById(R.id.app_bar_view);
        dividerView = view.findViewById(R.id.divider_view);
        newMessagesLayout = view.findViewById(R.id.new_messages_indicator_layout);
        newMessagesCardView = view.findViewById(R.id.new_messages_indicator_card);
        newMessagesOperatorStatusView = view.findViewById(R.id.new_messages_indicator_image);
        newMessagesCountBadgeView = view.findViewById(R.id.new_messages_badge_view);
    }

    private void setupViewAppearance() {
        adapter = new ChatAdapter(
                this.theme, this.onOptionClickedListener, this.onImageLoadedListener
        );
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        adapter.registerAdapterDataObserver(dataObserver);
        chatRecyclerView.setAdapter(adapter);
        chatRecyclerView.addOnScrollListener(onScrollListener);

        appBar.setTheme(this.theme);

        //icons
        sendButton.setImageResource(this.theme.getIconSendMessage());

        // new messages indicator shape
        ShapeAppearanceModel shapeAppearanceModel = newMessagesCardView.getShapeAppearanceModel()
                .toBuilder()
                .setBottomEdge(new MarkerEdgeTreatment(
                        resources.getDimension(R.dimen.chat_new_messages_bottom_edge_radius)
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
                        this.theme.getBrandPrimaryColor()));
        chatEditText.setTextColor(ContextCompat.getColor(
                this.getContext(), this.theme.getBaseDarkColor()));
        chatEditText.setHintTextColor(ContextCompat.getColor(
                this.getContext(), this.theme.getBaseNormalColor()));
        setBackgroundColor(
                ContextCompat.getColor(this.getContext(), this.theme.getBaseLightColor()));
        // fonts
        if (this.theme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(
                    this.getContext(),
                    this.theme.getFontRes());
            chatEditText.setTypeface(fontFamily);
        }
        // texts
        if (this.theme.getAppBarTitle() != null) {
            showToolbar(this.theme.getAppBarTitle());
        }
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
        chatEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    sendButton.setVisibility(VISIBLE);
                } else {
                    sendButton.setVisibility(GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String message = editable.toString().trim();
                if (controller != null) {
                    controller.sendMessagePreview(message);
                }
            }
        });

        sendButton.setOnClickListener(view -> {
            String message = chatEditText.getText().toString().trim();
            if (controller != null && controller.sendMessage(message)) {
                chatEditText.setText("");
            }
        });

        appBar.setOnBackClickedListener(() -> {
            if (controller != null) {
                controller.onBackArrowClicked();
            }
            if (chatHeadsController != null) {
                chatHeadsController.onChatBackButtonPressed();
            }
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

    private void showExitQueueDialog() {
        showOptionsDialog(resources.getString(R.string.dialog_leave_queue_title),
                resources.getString(R.string.dialog_leave_queue_message),
                resources.getString(R.string.dialog_yes),
                resources.getString(R.string.dialog_no),
                v -> {
                    dismissAlertDialog();
                    if (controller != null) {
                        controller.endEngagementDialogYesClicked();
                    }
                    if (chatHeadsController != null) {
                        chatHeadsController.chatEndedByUser();
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
                }
        );
    }

    private void showEndEngagementDialog(String operatorName) {
        showOptionsDialog(resources.getString(R.string.dialog_end_engagement_title),
                resources.getString(R.string.dialog_end_engagement_message, operatorName),
                resources.getString(R.string.dialog_yes),
                resources.getString(R.string.dialog_no),
                v -> {
                    dismissAlertDialog();
                    if (controller != null) {
                        controller.endEngagementDialogYesClicked();
                    }
                    if (chatHeadsController != null) {
                        chatHeadsController.chatEndedByUser();
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
                }
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

    private void showNoMoreOperatorsAvailableDialog() {
        showAlertDialog(
                R.string.dialog_operators_unavailable_title,
                R.string.dialog_operators_unavailable_message,
                v -> {
                    dismissAlertDialog();
                    if (controller != null) {
                        controller.noMoreOperatorsAvailableDismissed();
                    }
                    if (chatHeadsController != null) {
                        chatHeadsController.chatEndedByUser();
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
                R.string.dialog_unexpected_error_title,
                R.string.dialog_unexpected_error_message,
                v -> {
                    dismissAlertDialog();
                    if (controller != null) {
                        controller.unexpectedErrorDialogDismissed();
                    }
                    if (chatHeadsController != null) {
                        chatHeadsController.chatEndedByUser();
                    }
                    if (onEndListener != null) {
                        onEndListener.onEnd();
                    }
                }
        );
    }

    private void showOverlayPermissionsDialog() {
        showOptionsDialog(
                resources.getString(R.string.dialog_overlay_permissions_title),
                resources.getString(R.string.dialog_overlay_permissions_message),
                resources.getString(R.string.dialog_ok),
                resources.getString(R.string.dialog_no),
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
        this.getContext().stopService(new Intent(this.getContext(), ChatHeadService.class));
        Dependencies.getControllerFactory().destroyControllers();
    }

    private void dismissAlertDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
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
