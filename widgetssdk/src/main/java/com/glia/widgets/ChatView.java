package com.glia.widgets;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
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

import com.glia.widgets.chat.ChatAdapter;
import com.glia.widgets.chat.ChatController;
import com.glia.widgets.chat.ChatItem;
import com.glia.widgets.chat.ChatViewCallback;
import com.glia.widgets.chat.GliaRepository;
import com.glia.widgets.chat.OperatorStatusItem;
import com.glia.widgets.chat.ReceiveMessageItem;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;

import java.util.List;

public class ChatView extends LinearLayout {

    private boolean started = false;
    private boolean isVisible = false;
    private boolean exitDialogShowing = false;
    private boolean noOperatorsAvailableDialogShowing = false;
    private boolean unexpectedErrorDialogShowing = false;

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

    private UiTheme theme;
    // needed for setting status bar color back when view is gone
    private Integer defaultStatusbarColor;

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
        initConfigurations();
        initViews();
        readTypedArray(attrs, defStyleAttr, defStyleRes);
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
        builder.setBrandPrimaryColor(brandPriamryColorRes);
        builder.setSystemAgentBubbleColor(systemAgentBubbleColorRes);
        builder.setFontRes(fontRes);
        builder.setSystemNegativeColor(systemNegativeColorRes);
        this.theme = builder.build();
        setupViews();
        handleStatusbarColor();
    }

    private void setTitle(String title) {
        toolbarTitle.setText(title);
        toolbarLayout.setVisibility(VISIBLE);
    }

    public void startChat(String companyName, String queueId) {
        isVisible = true;
        startChatInternal(companyName, queueId);
    }

    public boolean isStarted() {
        return started;
    }

    private void startChatInternal(String companyName, String queueId) {
        setupViews();
        setupViewActions();
        initController(companyName, queueId);
        controller.init(queueId);
        started = true;
        if (isVisible) {
            show();
        }
    }

    private void initController(String companyName, String queueId) {
        View view = this;
        callback = new ChatViewCallback() {

            @Override
            public void queueing(OperatorStatusItem item) {
                view.post(() -> {
                    adapter.changeOperatorStatus(item);
                    toolbar.getMenu().findItem(R.id.close_button).setVisible(true);
                    chatEndButton.setVisibility(GONE);
                    chatEditText.setEnabled(false);
                });
            }

            @Override
            public void chatStarted(OperatorStatusItem item) {
                view.post(() -> {
                    adapter.changeOperatorStatus(item);
                    toolbar.getMenu().findItem(R.id.close_button).setVisible(false);
                    chatEndButton.setVisibility(VISIBLE);
                    chatEditText.setEnabled(true);
                    chatEditText.performClick();
                });
            }

            @Override
            public void appendItem(ChatItem item) {
                view.post(() -> {
                    adapter.addItem(item);
                    chatRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                });
            }

            @Override
            public synchronized void replaceReceiverItem(ReceiveMessageItem item) {
                Log.d("ChatController", "Replacing: " + item.toString());
                view.post(() -> {
                    adapter.replaceReceiverItem(item);
                    chatRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                });
            }

            @Override
            public void replaceItems(List<ChatItem> items) {
                view.post(() -> adapter.replaceAllItems(items));
            }

            @Override
            public void engagementEndShowNoMoreOperatorsDialog() {
                view.post(() -> showNoMoreOperatorsAvailableDialog());
            }

            @Override
            public void engagementEndNoDialog() {
                resetView();
            }

            @Override
            public void unexpectedError() {
                view.post(() -> showUnexpectedErrorDialog());
            }
        };
        GliaRepository repository = new GliaRepository();
        controller = new ChatController(callback, repository, companyName, queueId);
    }

    public void show() {
        if (started) {
            setVisibility(VISIBLE);
            isVisible = true;
            handleStatusbarColor();
        }
    }

    private void hide() {
        if (started) {
            setVisibility(INVISIBLE);
            if (defaultStatusbarColor != null && getActivity() != null) {
                getActivity().getWindow().setStatusBarColor(defaultStatusbarColor);
            }
            hideSoftKeyboard();
            isVisible = false;
        }
    }

    private void stop() {
        if (controller != null) {
            callback = null;
            controller.stop(false);
        }
    }

    private void resetView() {
        chatEndButton.setVisibility(GONE);
        toolbar.getMenu().findItem(R.id.close_button).setVisible(true);
        chatEditText.setText("");
        hide();
        started = false;
    }

    public void onDestroyView() {
        if (controller != null) {
            controller.onDestroy();
        }
        controller = null;
        callback = null;
        chatRecyclerView.setAdapter(null);
    }

    private void readTypedArray(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = this.getContext().obtainStyledAttributes(attrs, R.styleable.ChatView, defStyleAttr, defStyleRes);
        setDefaultTheme(typedArray);
        typedArray.recycle();
    }

    private void setDefaultTheme(TypedArray typedArray) {
        UiTheme.UiThemeBuilder defaultThemeBuilder = new UiTheme.UiThemeBuilder();
        defaultThemeBuilder.setAppBarTitle(getTitle(typedArray));
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

    private String getTitle(TypedArray typedArray) {
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
            return typedValue.data;
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
    }

    private void setupViews() {
        adapter = new ChatAdapter(this.theme);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        chatRecyclerView.setAdapter(adapter);

        toolbar.setBackgroundTintList(
                ContextCompat.getColorStateList(
                        this.getContext(),
                        this.theme.getBrandPrimaryColor()));
        DrawableCompat.setTint(toolbar.getMenu().findItem(R.id.close_button).getIcon(),
                ResourcesCompat.getColor(
                        this.getResources(),
                        this.theme.getBaseLightColor(),
                        this.getContext().getTheme()));
        toolbarTitle.setTextColor(ResourcesCompat.getColor(
                this.getResources(),
                this.theme.getBaseLightColor(),
                this.getContext().getTheme()));
        DrawableCompat.setTint(
                toolbar.getNavigationIcon(),
                ResourcesCompat.getColor(
                        this.getResources(),
                        this.theme.getBaseLightColor(),
                        this.getContext().getTheme()));
        chatEndButton.setBackgroundTintList(
                ContextCompat.getColorStateList(
                        this.getContext(),
                        this.theme.getSystemNegativeColor()));
        chatEndButton.setTextColor(ResourcesCompat.getColor(
                this.getResources(),
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
            setTitle(this.theme.getAppBarTitle());
        }
        setBackgroundColor(
                ContextCompat.getColor(this.getContext(), this.theme.getBaseLightColor()));
    }

    private void handleStatusbarColor() {
        if (getActivity() != null && (defaultStatusbarColor == null ||
                !defaultStatusbarColor.equals(this.theme.getBrandPrimaryColor()))) {
            defaultStatusbarColor = getActivity().getWindow().getStatusBarColor();
            if (isVisible) {
                getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(
                        this.getContext(), this.theme.getBrandPrimaryColor()));
            }
        }
    }

    private void setupViewActions() {
        chatEditText.setOnClickListener(view -> chatRecyclerView.smoothScrollToPosition(adapter.getItemCount()));
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
                controller.sendMessagePreview(message);
            }
        });

        sendButton.setOnClickListener(view -> {
            String message = chatEditText.getText().toString().trim();
            controller.sendMessage(message);
            chatEditText.setText("");
            hideSoftKeyboard();
        });

        toolbar.setOnMenuItemClickListener(item -> {
            showExitDialog();
            return true;
        });
        chatEndButton.setOnClickListener(v -> showExitDialog());
        toolbar.setNavigationOnClickListener(view -> hide());
    }

    @Nullable
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        ChatSaveState saveState = new ChatSaveState(superState);
        saveState.started = started;
        saveState.defaultStatusbarColor = defaultStatusbarColor;
        if (controller != null) {
            saveState.queueId = controller.getQueueId();
            saveState.companyName = controller.getCompanyName();
        }
        saveState.visible = isVisible;
        saveState.exitDialogShowing = exitDialogShowing;
        saveState.noOperatorsAvailableDialogShowing = noOperatorsAvailableDialogShowing;
        saveState.unexpectedErrorDialogShowing = unexpectedErrorDialogShowing;
        return saveState;
    }

    protected void onRestoreInstanceState(@Nullable Parcelable state) {
        if (!(state instanceof ChatSaveState)) {
            super.onRestoreInstanceState(state);
        } else {
            ChatSaveState saveState = (ChatSaveState) state;
            super.onRestoreInstanceState(saveState.getSuperState());
            defaultStatusbarColor = saveState.defaultStatusbarColor;
            isVisible = saveState.visible;
            if (saveState.started) {
                this.startChatInternal(saveState.companyName, saveState.queueId);
            }
            if (saveState.exitDialogShowing) {
                showExitDialog();
            } else if (saveState.noOperatorsAvailableDialogShowing) {
                showNoMoreOperatorsAvailableDialog();
            } else if (saveState.unexpectedErrorDialogShowing) {
                showUnexpectedErrorDialog();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        onDestroyView();
    }

    private void showExitDialog() {
        if (!exitDialogShowing) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this.getContext())
                    .setMessage(R.string.chat_dialog_leave_message)
                    .setPositiveButton(R.string.chat_dialog_yes, (dialogInterface, i) -> {
                        stop();
                        dialogInterface.dismiss();
                        exitDialogShowing = false;

                    })
                    .setNeutralButton(R.string.chat_dialog_no, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        exitDialogShowing = false;
                    })
                    .setOnCancelListener(dialog -> exitDialogShowing = false);
            if (theme.getFontRes() != null) {
                Typeface fontFamily = ResourcesCompat.getFont(this.getContext(), theme.getFontRes());

                TextView titleView = new TextView(this.getContext());
                titleView.setTextColor(ContextCompat.getColor(this.getContext(), theme.getBaseDarkColor()));
                TypedValue typedValue = new TypedValue();
                Resources.Theme resourceTheme = this.getContext().getTheme();
                resourceTheme.resolveAttribute(R.attr.materialAlertDialogTitleTextStyle, typedValue, true);
                titleView.setTextAppearance(typedValue.data);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                int horizontalPadding = (int) getResources().getDimension(R.dimen.large_x_large);
                int verticalPadding = (int) getResources().getDimension(R.dimen.medium);
                titleView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, 0);
                titleView.setLayoutParams(lp);
                titleView.setText(R.string.chat_dialog_leave_title);
                titleView.setTypeface(fontFamily);
                builder.setCustomTitle(titleView);
            } else {
                builder.setTitle(R.string.chat_dialog_leave_title);
            }

            AlertDialog dialog = builder.show();

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setBackgroundTintList(
                    ContextCompat.getColorStateList(this.getContext(), this.theme.getBrandPrimaryColor()));
            positiveButton.setTextColor(ContextCompat.getColor(
                    this.getContext(),
                    theme.getBaseLightColor()
            ));
            Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            neutralButton.setBackgroundTintList(
                    ContextCompat.getColorStateList(this.getContext(), this.theme.getSystemNegativeColor()));
            neutralButton.setTextColor(ContextCompat.getColor(
                    this.getContext(),
                    theme.getBaseLightColor()
            ));
            if (theme.getFontRes() != null) {
                Typeface fontFamily = ResourcesCompat.getFont(this.getContext(), theme.getFontRes());

                TextView messageView = dialog.getWindow().findViewById(android.R.id.message);
                messageView.setTypeface(fontFamily);
                positiveButton.setTypeface(fontFamily);
                neutralButton.setTypeface(fontFamily);
            }
            dialog.getWindow().getDecorView().getBackground().setTint(ContextCompat.getColor(
                    this.getContext(), theme.getBaseLightColor()));

            exitDialogShowing = true;
        }
    }

    private void showResetViewDialog(@StringRes int title, @StringRes int message,
                                     DialogInterface.OnClickListener buttonClickListener) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this.getContext())
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton(R.string.chat_dialog_ok, buttonClickListener);
        if (theme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(this.getContext(), theme.getFontRes());

            TextView titleView = new TextView(this.getContext());
            titleView.setTextColor(ContextCompat.getColor(this.getContext(), theme.getBaseDarkColor()));
            TypedValue typedValue = new TypedValue();
            Resources.Theme resourceTheme = this.getContext().getTheme();
            resourceTheme.resolveAttribute(R.attr.materialAlertDialogTitleTextStyle, typedValue, true);
            titleView.setTextAppearance(typedValue.data);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            int horizontalPadding = (int) getResources().getDimension(R.dimen.large_x_large);
            int verticalPadding = (int) getResources().getDimension(R.dimen.medium);
            titleView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, 0);
            titleView.setLayoutParams(lp);
            titleView.setText(title);
            titleView.setTypeface(fontFamily);
            builder.setCustomTitle(titleView);
        } else {
            builder.setTitle(title);
        }

        AlertDialog dialog = builder.show();
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setBackgroundTintList(
                ContextCompat.getColorStateList(this.getContext(), this.theme.getBrandPrimaryColor()));
        negativeButton.setTextColor(ContextCompat.getColor(
                this.getContext(),
                theme.getBaseLightColor()
        ));
        TextView messageView = dialog.getWindow().findViewById(android.R.id.message);
        if (theme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(this.getContext(), theme.getFontRes());
            messageView.setTypeface(fontFamily);
            negativeButton.setTypeface(fontFamily);
        }
        dialog.getWindow().getDecorView().getBackground().setTint(ContextCompat.getColor(
                this.getContext(), theme.getBaseLightColor()));

        resetView();
    }

    private void showNoMoreOperatorsAvailableDialog() {
        if (!noOperatorsAvailableDialogShowing) {
            showResetViewDialog(R.string.chat_dialog_operators_unavailable_title,
                    R.string.chat_dialog_operators_unavailable_message,
                    (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        noOperatorsAvailableDialogShowing = false;
                    });
            noOperatorsAvailableDialogShowing = true;
        }
    }

    private void showUnexpectedErrorDialog() {
        if (!unexpectedErrorDialogShowing) {
            showResetViewDialog(
                    R.string.chat_dialog_unexpected_error_title,
                    R.string.chat_dialog_unexpected_error_message,
                    (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        unexpectedErrorDialogShowing = false;
                    }
            );
            unexpectedErrorDialogShowing = true;
        }
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
}
