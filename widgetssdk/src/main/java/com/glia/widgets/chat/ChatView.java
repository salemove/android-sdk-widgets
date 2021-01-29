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
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.adapter.ChatAdapter;
import com.glia.widgets.chat.adapter.ChatItem;
import com.glia.widgets.chat.head.ChatHeadService;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.model.DialogsState;
import com.glia.widgets.view.AppBarView;
import com.glia.widgets.view.Dialogs;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;

import java.util.List;

import static androidx.lifecycle.Lifecycle.State.INITIALIZED;

public class ChatView extends LinearLayout {

    private AlertDialog alertDialog;

    private ChatViewCallback callback;
    private ChatController controller;

    private RecyclerView chatRecyclerView;
    private ImageButton sendButton;
    private EditText chatEditText;
    private ChatAdapter adapter;
    private AppBarView appBar;
    private View dividerView;

    private UiTheme theme;
    // needed for setting status bar color back when view is gone
    private Integer defaultStatusbarColor;
    private OnBackClickedListener onBackClickedListener;
    private OnEndListener onEndListener;
    private OnNavigateToCallListener onNavigateToCallListener;
    private OnBubbleListener onBubbleListener;

    private final Resources resources;

    private final RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            int totalItemCount = adapter.getItemCount();
            int lastIndex = totalItemCount - 1;
            boolean scrollToBottom = positionStart + itemCount >= lastIndex;
            if (scrollToBottom) {
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

    private void initControls() {
        callback = new ChatViewCallback() {

            @Override
            public void emitState(ChatState chatState) {
                post(() -> {
                    chatEditText.setEnabled(chatState.isOperatorOnline());
                    if (chatState.isOperatorOnline()) {
                        appBar.showEndButton();
                    } else {
                        appBar.showXButton();
                        // idk if good idea but not setting because needs to remember previously
                        // typed message if opened from floating chat head
                        if (!chatState.useFloatingChatHeads) {
                            chatEditText.setText("");
                        }
                    }

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
            public void emitDialog(DialogsState dialogsState) {
                if (dialogsState instanceof DialogsState.NoDialog) {
                    post(() -> {
                        if (alertDialog != null) {
                            alertDialog.dismiss();
                            alertDialog = null;
                        }
                    });
                } else if (dialogsState instanceof DialogsState.UnexpectedErrorDialog) {
                    post(() -> showUnexpectedErrorDialog());
                } else if (dialogsState instanceof DialogsState.ExitQueueDialog) {
                    post(() -> showExitQueueDialog());
                } else if (dialogsState instanceof DialogsState.OverlayPermissionsDialog) {
                    post(() -> showOverlayPermissionsDialog());
                } else if (dialogsState instanceof DialogsState.EndEngagementDialog) {
                    post(() -> showEndEngagementDialog(
                            ((DialogsState.EndEngagementDialog) dialogsState).operatorName));
                } else if (dialogsState instanceof DialogsState.UpgradeAudioDialog) {
                    post(() -> showUpgradeDialog(((DialogsState.UpgradeAudioDialog) dialogsState).operatorName));
                } else if (dialogsState instanceof DialogsState.NoMoreOperatorsDialog) {
                    post(() -> showNoMoreOperatorsAvailableDialog());
                }
            }

            @Override
            public void handleFloatingChatHead(boolean show) {
                if (onBubbleListener != null) {
                    onBubbleListener.call(theme, chatEditText.getText().toString(), show);
                }
            }

            @Override
            public void navigateToCall() {
                if (onNavigateToCallListener != null) {
                    onNavigateToCallListener.call(chatEditText.getText().toString());
                }
            }

            @Override
            public void destroyView() {
                if (onEndListener != null) {
                    onEndListener.onEnd();
                }
            }
        };
        controller = GliaWidgets
                .getControllerFactory()
                .getChatController(Utils.getActivity(this.getContext()), callback);
        controller.setOverlayPermissions(Settings.canDrawOverlays(this.getContext()));
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

    /**
     * Method called by ChatActivity to enable chat heads functionality.
     * And activates chat head functionality if true, else asks for permissions.
     * The integrator should not call this method, instead please use
     * {@link #startEmbeddedChat(String, String, String)}
     * Intended for use in ChatActivity to get chat heads functionality.
     *
     * @param chatActivity used to make sure that this is only called from ChatActivity
     */
    public void startChatActivityChat(ChatActivity chatActivity,
                                      String companyName,
                                      String queueId,
                                      String contextUrl,
                                      String lastTypedText,
                                      boolean isOriginCall) {
        if (!chatActivity.getLifecycle().getCurrentState().isAtLeast(INITIALIZED)) {
            throw new IllegalArgumentException("Only intended for internal use. Please see javadoc.");
        }
        startChatInternal(
                companyName,
                queueId,
                contextUrl,
                isOriginCall ? isOriginCall : Utils.getActivity(this.getContext()) instanceof ChatActivity);
        if (lastTypedText != null) {
            chatEditText.setText(lastTypedText);
        }
    }

    /***
     * Call when using chat in an embedded view and do not want to use the
     * chat heads functionality.
     */
    public void startEmbeddedChat(String companyName, String queueId, String contextUrl) {
        startChatInternal(companyName, queueId, contextUrl, false);
    }

    private void startChatInternal(
            String companyName,
            String queueId,
            String contextUrl,
            boolean useChatHeads) {
        if (controller != null) {
            controller.initChat(
                    companyName,
                    queueId,
                    contextUrl,
                    useChatHeads);
        }
    }

    public void setTheme(UiTheme uiTheme) {
        if (uiTheme == null) return;
        String title = uiTheme.getAppBarTitle() != null ? uiTheme.getAppBarTitle() : this.theme.getAppBarTitle();
        Integer baseLightColorRes = uiTheme.getBaseLightColor() != null ?
                uiTheme.getBaseLightColor() : this.theme.getBaseLightColor();
        Integer baseDarkColorRes = uiTheme.getBaseDarkColor() != null ?
                uiTheme.getBaseDarkColor() : this.theme.getBaseDarkColor();
        Integer baseNormalColorRes = uiTheme.getBaseNormalColor() != null ?
                uiTheme.getBaseNormalColor() : this.theme.getBaseNormalColor();
        Integer baseShadeColorRes = uiTheme.getBaseShadeColor() != null ?
                uiTheme.getBaseShadeColor() : this.theme.getBaseShadeColor();
        Integer brandPriamryColorRes = uiTheme.getBrandPrimaryColor() != null ?
                uiTheme.getBrandPrimaryColor() : this.theme.getBrandPrimaryColor();
        Integer systemAgentBubbleColorRes = uiTheme.getSystemAgentBubbleColor() != null ?
                uiTheme.getSystemAgentBubbleColor() : this.theme.getSystemAgentBubbleColor();
        Integer fontRes = uiTheme.getFontRes() != null ? uiTheme.getFontRes() : this.theme.getFontRes();
        Integer systemNegativeColorRes = uiTheme.getSystemNegativeColor() != null ?
                uiTheme.getSystemNegativeColor() : this.theme.getSystemNegativeColor();

        UiTheme.UiThemeBuilder builder = new UiTheme.UiThemeBuilder();
        builder.setAppBarTitle(title);
        builder.setBaseLightColor(baseLightColorRes);
        builder.setBaseDarkColor(baseDarkColorRes);
        builder.setBaseNormalColor(baseNormalColorRes);
        builder.setBaseShadeColor(baseShadeColorRes);
        builder.setBrandPrimaryColor(brandPriamryColorRes);
        builder.setSystemAgentBubbleColor(systemAgentBubbleColorRes);
        builder.setFontRes(fontRes);
        builder.setSystemNegativeColor(systemNegativeColorRes);
        this.theme = builder.build();
        setupViewAppearance();
        if (controller.isStarted()) {
            handleStatusbarColor();
        }
    }

    private void showToolbar(String title) {
        appBar.showToolbar(title);
    }

    public void show() {
        if (controller != null) {
            controller.show();
        }
    }

    public void backPressed() {
        if (controller != null) {
            controller.onBackArrowClicked();
        }
    }

    public void setOnBackClickedListener(OnBackClickedListener onBackClicked) {
        this.onBackClickedListener = onBackClicked;
    }

    public void setOnEndListener(OnEndListener onEndListener) {
        this.onEndListener = onEndListener;
    }

    public void setOnNavigateToCallListener(OnNavigateToCallListener onNavigateToCallListener) {
        this.onNavigateToCallListener = onNavigateToCallListener;
    }

    public void setOnBubbleListener(OnBubbleListener onBubbleListener) {
        this.onBubbleListener = onBubbleListener;
    }

    public void onDestroyView() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
        onEndListener = null;
        onBackClickedListener = null;
        onNavigateToCallListener = null;
        onBubbleListener = null;
        destroyController();
        callback = null;
        adapter.unregisterAdapterDataObserver(dataObserver);
        chatRecyclerView.setAdapter(null);
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
        setOrientation(VERTICAL);
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
    }

    private void setupViewAppearance() {
        adapter = new ChatAdapter(this.theme);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        adapter.registerAdapterDataObserver(dataObserver);
        chatRecyclerView.setAdapter(adapter);

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
        appBar.setTheme(this.theme);
        if (this.theme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(
                    this.getContext(),
                    this.theme.getFontRes());
            chatEditText.setTypeface(fontFamily);
        }
        if (this.theme.getAppBarTitle() != null) {
            showToolbar(this.theme.getAppBarTitle());
        }
        setBackgroundColor(
                ContextCompat.getColor(this.getContext(), this.theme.getBaseLightColor()));
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
            if (controller != null) {
                controller.sendMessage(message);
            }
            chatEditText.setText("");
            Utils.hideSoftKeyboard(this.getContext(), getWindowToken());
        });

        appBar.setOnBackClickedListener(() -> {
            if (controller != null) {
                controller.onBackArrowClicked();
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
    }

    private void showExitQueueDialog() {
        showOptionsDialog(resources.getString(R.string.chat_dialog_leave_queue_title),
                resources.getString(R.string.chat_dialog_leave_queue_message),
                resources.getString(R.string.chat_dialog_yes),
                resources.getString(R.string.chat_dialog_no),
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
                }
        );
    }

    private void showEndEngagementDialog(String operatorName) {
        showOptionsDialog(resources.getString(R.string.chat_dialog_end_engagement_title),
                resources.getString(R.string.chat_dialog_end_engagement_message, operatorName),
                resources.getString(R.string.chat_dialog_yes),
                resources.getString(R.string.chat_dialog_no),
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
                R.string.chat_dialog_operators_unavailable_title,
                R.string.chat_dialog_operators_unavailable_message,
                v -> {
                    dismissAlertDialog();
                    if (controller != null) {
                        controller.noMoreOperatorsAvailableDismissed();
                    }
                    if (onEndListener != null) {
                        onEndListener.onEnd();
                    }
                });
    }

    private void showUpgradeDialog(String operatorName) {
        alertDialog = Dialogs.showUpgradeDialog(
                this.getContext(),
                theme,
                getResources().getString(R.string.chat_dialog_upgrade_audio_title, operatorName),
                v -> {
                    dismissAlertDialog();
                    if (controller != null) {
                        controller.upgradeToAudioClicked();
                    }
                },
                v -> {
                    dismissAlertDialog();
                    if (controller != null) {
                        controller.closeUpgradeDialogClicked();
                    }
                });
    }

    private void showUnexpectedErrorDialog() {
        showAlertDialog(
                R.string.chat_dialog_unexpected_error_title,
                R.string.chat_dialog_unexpected_error_message,
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
                resources.getString(R.string.chat_dialog_overlay_permissions_title),
                resources.getString(R.string.chat_dialog_overlay_permissions_message),
                resources.getString(R.string.chat_dialog_ok),
                resources.getString(R.string.chat_dialog_no),
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

    public void onResume() {
        checkOverlayPermissions();
    }

    private void checkOverlayPermissions() {
        if (controller != null) {
            controller.onResume(Settings.canDrawOverlays(this.getContext()));
        }
    }

    private void chatEnded() {
        this.getContext().stopService(new Intent(this.getContext(), ChatHeadService.class));
        GliaWidgets.getControllerFactory().destroyControllers();
    }

    private void dismissAlertDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    public void navigateToCallSuccess() {
        if (controller != null) {
            controller.navigateToCallSuccess();
        }
    }

    public interface OnBackClickedListener {
        void onBackClicked();
    }

    public interface OnEndListener {
        void onEnd();
    }

    public interface OnNavigateToCallListener {
        void call(String lastTypedText);
    }

    public interface OnBubbleListener {
        void call(UiTheme theme, String lastTypedText, boolean isVisible);
    }
}
