package com.glia.widgets.chat;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
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
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleableRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.adapter.ChatAdapter;
import com.glia.widgets.chat.adapter.ChatItem;
import com.glia.widgets.chat.head.ChatHeadService;
import com.glia.widgets.model.GliaRepository;
import com.glia.widgets.view.Dialogs;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;

import java.util.List;
import java.util.Objects;

import static androidx.lifecycle.Lifecycle.State.INITIALIZED;

public class ChatView extends LinearLayout {

    private AlertDialog alertDialog;

    private ChatViewCallback callback;
    private ChatController controller;

    private RecyclerView chatRecyclerView;
    private ImageButton sendButton;
    private EditText chatEditText;
    private ChatAdapter adapter;
    private AppBarLayout toolbarLayout;
    private MaterialToolbar toolbar;
    private TextView toolbarTitle;
    private Button chatEndButton;
    private View dividerView;

    private UiTheme theme;
    // needed for setting status bar color back when view is gone
    private Integer defaultStatusbarColor;
    private OnBackClickedListener onBackClickedListener;
    private OnEndListener onEndListener;

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
                        chatEndButton.setVisibility(VISIBLE);
                        toolbar.getMenu().findItem(R.id.close_button).setVisible(false);
                    } else {
                        chatEndButton.setVisibility(GONE);
                        toolbar.getMenu().findItem(R.id.close_button).setVisible(true);
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
                post(() -> {
                    adapter.submitList(items);
                });
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
                startChatHeadService(show);
            }
        };
        controller = GliaWidgets.getChatControllerFactory().getChatController(getActivity(), callback);
    }

    private void showChat() {
        setVisibility(VISIBLE);
        handleStatusbarColor();
    }

    private void hideChat() {
        setVisibility(INVISIBLE);
        if (defaultStatusbarColor != null && getActivity() != null) {
            getActivity().getWindow().setStatusBarColor(defaultStatusbarColor);
            defaultStatusbarColor = null;
        }
        hideSoftKeyboard();
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
    public void startChatActivityChat(ChatActivity chatActivity, String companyName, String queueId, String contextUrl, String lastTypedText) {
        if (!chatActivity.getLifecycle().getCurrentState().isAtLeast(INITIALIZED)) {
            throw new IllegalArgumentException("Only intended for internal use. Please see javadoc.");
        }
        startChatInternal(companyName, queueId, contextUrl);
        if (lastTypedText != null) {
            chatEditText.setText(lastTypedText);
        }
    }

    /***
     * Call when using chat in an embedded view and do not want to use the
     * chat heads functionality.
     */
    public void startEmbeddedChat(String companyName, String queueId, String contextUrl) {
        startChatInternal(companyName, queueId, contextUrl);
    }

    private void startChatInternal(
            String companyName,
            String queueId,
            String contextUrl) {
        GliaRepository repository = GliaWidgets.getChatControllerFactory().getGliaRepository();
        if (controller != null) {
            controller.initChat(repository, companyName, queueId, contextUrl, getActivity() instanceof ChatActivity);
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
        toolbarTitle.setText(title);
        toolbarLayout.setVisibility(VISIBLE);
    }

    public boolean isStarted() {
        return controller != null && controller.isStarted();
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

    public void onDestroyView() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
        onEndListener = null;
        onBackClickedListener = null;
        destroyController();
        callback = null;
        adapter.unregisterAdapterDataObserver(dataObserver);
        chatRecyclerView.setAdapter(null);
    }

    private void destroyController() {
        if (controller != null) {
            controller.onDestroy(getActivity() instanceof ChatActivity);
        }
        controller = null;
    }

    private void readTypedArray(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = this.getContext().obtainStyledAttributes(attrs, R.styleable.ChatView, defStyleAttr, defStyleRes);
        setDefaultTheme(typedArray);
        typedArray.recycle();
    }

    private void setDefaultTheme(TypedArray typedArray) {
        UiTheme.UiThemeBuilder defaultThemeBuilder = new UiTheme.UiThemeBuilder();
        defaultThemeBuilder.setAppBarTitle(getAppBarTitleValue(typedArray));
        defaultThemeBuilder.setBrandPrimaryColor(
                getTypedArrayValue(
                        typedArray,
                        R.styleable.ChatView_brandPrimaryColor,
                        R.attr.gliaBrandPrimaryColor));
        defaultThemeBuilder.setBaseLightColor(
                getTypedArrayValue(
                        typedArray,
                        R.styleable.ChatView_baseLightColor,
                        R.attr.gliaBaseLightColor
                )
        );
        defaultThemeBuilder.setBaseDarkColor(
                getTypedArrayValue(
                        typedArray,
                        R.styleable.ChatView_baseDarkColor,
                        R.attr.gliaBaseDarkColor
                )
        );
        defaultThemeBuilder.setBaseNormalColor(
                getTypedArrayValue(
                        typedArray,
                        R.styleable.ChatView_baseNormalColor,
                        R.attr.gliaBaseNormalColor
                )
        );
        defaultThemeBuilder.setBaseShadeColor(
                getTypedArrayValue(
                        typedArray,
                        R.styleable.ChatView_baseShadeColor,
                        R.attr.gliaBaseShadeColor
                )
        );
        defaultThemeBuilder.setSystemAgentBubbleColor(
                getTypedArrayValue(
                        typedArray,
                        R.styleable.ChatView_systemAgentBubbleColor,
                        R.attr.gliaSystemAgentBubbleColor
                )
        );
        defaultThemeBuilder.setSystemNegativeColor(
                getTypedArrayValue(
                        typedArray,
                        R.styleable.ChatView_systemNegativeColor,
                        R.attr.gliaSystemNegativeColor
                )
        );
        defaultThemeBuilder.setFontRes(
                getTypedArrayValue(
                        typedArray,
                        R.styleable.ChatView_android_fontFamily,
                        R.attr.fontFamily
                )
        );
        this.theme = defaultThemeBuilder.build();
    }

    private String getAppBarTitleValue(TypedArray typedArray) {
        if (typedArray.hasValue(R.styleable.ChatView_appBarTitle)) {
            return typedArray.getString(R.styleable.ChatView_appBarTitle);
        } else {
            return null;
        }
    }

    private Integer getTypedArrayValue(TypedArray typedArray,
                                       @StyleableRes int index,
                                       @AttrRes int defaultValue) {
        if (typedArray.hasValue(index)) {
            return typedArray.getResourceId(index, 0);
        } else {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = this.getContext().getTheme();
            theme.resolveAttribute(defaultValue, typedValue, true);
            return typedValue.resourceId;
        }
    }

    private void initConfigurations() {
        setOrientation(VERTICAL);
        setVisibility(INVISIBLE);
        // needed to overlap existing app bar in existing view with this view's app bar.
        ViewCompat.setElevation(this, 100.0f);
    }

    private void initViews() {
        TypedValue typedValue = new TypedValue();
        this.getContext().getTheme().resolveAttribute(R.attr.gliaBaseDarkColor, typedValue, true);

        View view = View.inflate(this.getContext(), R.layout.chat_view, this);
        chatRecyclerView = view.findViewById(R.id.chat_recycler_view);
        sendButton = view.findViewById(R.id.send_button);
        chatEditText = view.findViewById(R.id.chat_edit_text);
        toolbarLayout = view.findViewById(R.id.chat_tool_bar);
        toolbar = view.findViewById(R.id.chat_top_app_bar);
        toolbarTitle = toolbar.findViewById(R.id.title);
        chatEndButton = toolbar.findViewById(R.id.chat_end_button);
        dividerView = view.findViewById(R.id.divider_view);
    }

    private void setupViewAppearance() {
        adapter = new ChatAdapter(this.theme);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        adapter.registerAdapterDataObserver(dataObserver);
        chatRecyclerView.setAdapter(adapter);

        toolbar.setBackgroundTintList(
                ContextCompat.getColorStateList(
                        this.getContext(),
                        this.theme.getBrandPrimaryColor()));
        DrawableCompat.setTint(toolbar.getMenu().findItem(R.id.close_button).getIcon(),
                ResourcesCompat.getColor(
                        resources,
                        this.theme.getBaseLightColor(),
                        this.getContext().getTheme()));
        toolbarTitle.setTextColor(ResourcesCompat.getColor(
                resources,
                this.theme.getBaseLightColor(),
                this.getContext().getTheme()));
        DrawableCompat.setTint(
                Objects.requireNonNull(toolbar.getNavigationIcon()),
                ResourcesCompat.getColor(
                        resources,
                        this.theme.getBaseLightColor(),
                        this.getContext().getTheme()));
        chatEndButton.setBackgroundTintList(
                ContextCompat.getColorStateList(
                        this.getContext(),
                        this.theme.getSystemNegativeColor()));
        dividerView.setBackgroundColor(ContextCompat.getColor(
                this.getContext(),
                theme.getBaseShadeColor()));
        chatEndButton.setTextColor(ResourcesCompat.getColor(
                resources,
                this.theme.getBaseLightColor(),
                this.getContext().getTheme()));
        sendButton.setImageTintList(
                ContextCompat.getColorStateList(
                        this.getContext(),
                        this.theme.getBrandPrimaryColor()));
        chatEditText.setTextColor(ContextCompat.getColor(
                this.getContext(), theme.getBaseDarkColor()));
        chatEditText.setHintTextColor(ContextCompat.getColor(
                this.getContext(), theme.getBaseNormalColor()));
        if (this.theme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(
                    this.getContext(),
                    this.theme.getFontRes());
            chatEditText.setTypeface(fontFamily);
            toolbarTitle.setTypeface(fontFamily);
            chatEndButton.setTypeface(fontFamily);
        }
        if (this.theme.getAppBarTitle() != null) {
            showToolbar(this.theme.getAppBarTitle());
        }
        setBackgroundColor(
                ContextCompat.getColor(this.getContext(), this.theme.getBaseLightColor()));
    }

    private void handleStatusbarColor() {
        if (getActivity() != null && defaultStatusbarColor == null) {
            defaultStatusbarColor = getActivity().getWindow().getStatusBarColor();
            if (controller != null && controller.isChatVisible()) {
                getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(
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
            hideSoftKeyboard();
        });

        toolbar.setOnMenuItemClickListener(item -> {
            if (controller != null) {
                controller.leaveChatQueueClicked();
            }
            return true;
        });
        chatEndButton.setOnClickListener(v -> {
            if (controller != null) {
                controller.leaveChatClicked();
            }
        });
        toolbar.setNavigationOnClickListener(view -> {
            if (controller != null) {
                controller.onBackArrowClicked();
            }
            if (onBackClickedListener != null) {
                onBackClickedListener.onBackClicked();
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        onDestroyView();
        super.onDetachedFromWindow();
    }

    private void showExitQueueDialog() {
        showOptionsDialog(resources.getString(R.string.chat_dialog_leave_queue_title),
                resources.getString(R.string.chat_dialog_leave_queue_message),
                resources.getString(R.string.chat_dialog_yes),
                resources.getString(R.string.chat_dialog_no),
                (dialogInterface, i) -> {
                    if (controller != null) {
                        controller.endEngagementDialogYesClicked();
                    }
                    if (onEndListener != null) {
                        onEndListener.onEnd();
                    }
                    chatEnded();
                    alertDialog = null;
                    dialogInterface.dismiss();
                },
                (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    if (controller != null) {
                        controller.endEngagementDialogDismissed();
                    }
                    alertDialog = null;
                },
                dialog -> {
                    if (controller != null) {
                        controller.endEngagementDialogDismissed();
                    }
                    alertDialog = null;
                }
        );
    }

    private void showEndEngagementDialog(String operatorName) {
        showOptionsDialog(resources.getString(R.string.chat_dialog_end_engagement_title),
                resources.getString(R.string.chat_dialog_end_engagement_message, operatorName),
                resources.getString(R.string.chat_dialog_yes),
                resources.getString(R.string.chat_dialog_no),
                (dialogInterface, i) -> {
                    if (controller != null) {
                        controller.endEngagementDialogYesClicked();
                    }
                    if (onEndListener != null) {
                        onEndListener.onEnd();
                    }
                    chatEnded();
                    alertDialog = null;
                    dialogInterface.dismiss();
                },
                (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    if (controller != null) {
                        controller.endEngagementDialogDismissed();
                    }
                    alertDialog = null;
                },
                dialog -> {
                    if (controller != null) {
                        controller.endEngagementDialogDismissed();
                    }
                    alertDialog = null;
                }
        );
    }

    private void showOptionsDialog(String title,
                                   String message,
                                   String positiveButtonText,
                                   String neutralButtonText,
                                   DialogInterface.OnClickListener positiveButtonClickListener,
                                   DialogInterface.OnClickListener neutralButtonClickListener,
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

    private void showAlertDialog(@StringRes int title, @StringRes int message, @StringRes int buttonText,
                                 DialogInterface.OnClickListener buttonClickListener) {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
        alertDialog = Dialogs.showAlertDialog(
                this.getContext(),
                this.theme,
                title,
                message,
                buttonText,
                buttonClickListener);
    }

    private void showNoMoreOperatorsAvailableDialog() {
        showAlertDialog(
                R.string.chat_dialog_operators_unavailable_title,
                R.string.chat_dialog_operators_unavailable_message,
                R.string.chat_dialog_ok,
                (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    if (controller != null) {
                        controller.noMoreOperatorsAvailableDismissed();
                    }
                    if (onEndListener != null) {
                        onEndListener.onEnd();
                    }
                    alertDialog = null;
                });
    }

    private void showUpgradeDialog(String operatorName) {
        alertDialog = Dialogs.showUpgradeDialog(
                this.getContext(),
                theme,
                getResources().getString(R.string.chat_dialog_upgrade_title, operatorName),
                v -> {
                    if (controller != null) {
                        controller.upgradeToAudioClicked();
                    }
                    alertDialog.dismiss();
                },
                v -> {
                    if (controller != null) {
                        controller.closeUpgradeDialogClicked();
                    }
                    alertDialog.dismiss();
                });
    }

    private void showUnexpectedErrorDialog() {
        showAlertDialog(
                R.string.chat_dialog_unexpected_error_title,
                R.string.chat_dialog_unexpected_error_message,
                R.string.chat_dialog_ok,
                (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    if (controller != null) {
                        controller.unexpectedErrorDialogDismissed();
                    }
                    if (onEndListener != null) {
                        onEndListener.onEnd();
                    }
                    alertDialog = null;
                }
        );
    }

    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    private void showOverlayPermissionsDialog() {
        showOptionsDialog(resources.getString(R.string.chat_dialog_overlay_permissions_title),
                resources.getString(R.string.chat_dialog_overlay_permissions_message),
                resources.getString(R.string.chat_dialog_ok),
                resources.getString(R.string.chat_dialog_no),
                (dialogInterface, i) -> {
                    if (controller != null) {
                        controller.overlayPermissionsDialogDismissed();
                    }
                    Intent overlayIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + this.getContext().getPackageName()));
                    overlayIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    this.getContext().startActivity(overlayIntent);
                },
                (dialogInterface, i) -> {
                    if (controller != null) {
                        controller.overlayPermissionsDialogDismissed();
                    }
                    dialogInterface.dismiss();
                },
                dialog -> {
                    if (controller != null) {
                        controller.overlayPermissionsDialogDismissed();
                    }
                }
        );
    }

    public void onResume() {
        if (controller != null) {
            controller.onResume(Settings.canDrawOverlays(this.getContext()));
        }
    }

    private void startChatHeadService(boolean showChatHead) {
        Intent newIntent = new Intent(this.getContext(), ChatHeadService.class);
        if (controller != null) {
            newIntent.putExtra(GliaWidgets.COMPANY_NAME, controller.getCompanyName());
            newIntent.putExtra(GliaWidgets.QUEUE_ID, controller.getQueueId());
            newIntent.putExtra(GliaWidgets.CONTEXT_URL, controller.getContextUrl());
        }
        newIntent.putExtra(GliaWidgets.UI_THEME, theme);
        newIntent.putExtra(ChatHeadService.IS_VISIBLE, showChatHead);
        newIntent.putExtra(ChatActivity.LAST_TYPED_TEXT, chatEditText.getText().toString());
        this.getContext().startService(newIntent);
    }

    private void chatEnded() {
        this.getContext().stopService(new Intent(this.getContext(), ChatHeadService.class));
        GliaWidgets.getChatControllerFactory().destroyChatController(getActivity());
    }

    public interface OnBackClickedListener {
        void onBackClicked();
    }

    public interface OnEndListener {
        void onEnd();
    }
}
