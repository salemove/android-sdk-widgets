package com.glia.widgets.call;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.glia.androidsdk.comms.MediaState;
import com.glia.androidsdk.comms.VideoView;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.head.ChatHeadService;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.model.DialogsState;
import com.glia.widgets.view.AppBarView;
import com.glia.widgets.view.DialogOfferType;
import com.glia.widgets.view.Dialogs;
import com.glia.widgets.view.OperatorStatusView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;
import com.google.android.material.transition.MaterialFade;
import com.google.android.material.transition.SlideDistanceProvider;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

public class CallView extends ConstraintLayout {

    private CallViewCallback callback;
    private CallController controller;

    private AlertDialog alertDialog;
    private AppBarView appBar;
    private OperatorStatusView operatorStatusView;
    private TextView operatorNameView;
    private TextView callTimerView;
    private FrameLayout operatorVideoContainer;
    private VideoView operatorVideoView;
    private MaterialCardView visitorVideoContainer;
    private VideoView visitorVideoView;
    private TextView chatButtonLabel;
    private TextView videoButtonLabel;
    private TextView muteButtonLabel;
    private TextView speakerButtonLabel;
    private TextView minimizeButtonLabel;
    private View buttonsLayoutBackground;
    private View buttonsLayout;
    private FloatingActionButton chatButton;
    private FloatingActionButton videoButton;
    private FloatingActionButton minimizeButton;
    private TextView chatButtonBadgeView;

    private OnBackClickedListener onBackClickedListener;
    private OnEndListener onEndListener;
    private OnNavigateToChatListener onNavigateToChatListener;
    private OnBubbleListener onBubbleListener;

    private UiTheme theme;

    private final Resources resources;
    private Integer defaultStatusbarColor;

    public CallView(Context context) {
        this(context, null);
    }

    public CallView(Context context, @Nullable AttributeSet attrs) {
        // using defStyleAttr = 0 so overriding not allowed by integrator
        this(context, attrs, 0);
    }

    public CallView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat);
    }

    public CallView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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

    private void setupViewActions() {
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
        chatButton.setOnClickListener(v -> {
            if (controller != null) {
                controller.chatButtonClicked();
            }
        });
        minimizeButton.setOnClickListener(v -> {
            if (controller != null) {
                controller.minimizeButtonClicked();
            }
            if (onEndListener != null) {
                onEndListener.onEnd();
            }
        });
    }

    public void startCall() {
        if (controller != null) {
            controller.initCall();
        }
    }

    public void onDestroy() {
        if (operatorVideoView != null) {
            operatorVideoView.release();
            operatorVideoView = null;
        }
        if (visitorVideoView != null) {
            visitorVideoView.release();
            visitorVideoView = null;
        }
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
        onEndListener = null;
        onBackClickedListener = null;
        onNavigateToChatListener = null;
        onBubbleListener = null;
        destroyController();
        callback = null;
    }

    public void onResume() {
        if (controller != null) {
            controller.onResume(Settings.canDrawOverlays(this.getContext()));
            if (operatorVideoView != null) {
                operatorVideoView.resumeRendering();
            }
            if (visitorVideoView != null) {
                visitorVideoView.resumeRendering();
            }
        }
    }

    public void onPause() {
        if (operatorVideoView != null) {
            operatorVideoView.pauseRendering();
        }
        if (visitorVideoView != null) {
            visitorVideoView.resumeRendering();
        }
    }

    private void destroyController() {
        GliaWidgets.getControllerFactory().destroyCallController(true);
        controller = null;
    }

    private void initControls() {
        callback = new CallViewCallback() {
            @Override
            public void emitState(CallState callState) {
                post(() -> {
                    if (callState.isCallOngoing()) {
                        appBar.showEndButton();
                    } else {
                        appBar.showXButton();
                    }
                    if (callState.isAudioCall()) {
                        appBar.setTitle(resources.getString(R.string.call_audio_app_bar_title));
                    } else if (callState.isVideoCall()) {
                        appBar.setTitle(resources.getString(R.string.call_video_app_bar_title));
                    }
                    if (resources.getConfiguration().orientation == ORIENTATION_LANDSCAPE &&
                            callState.isVideoCall()) {
                        ColorStateList blackTransparentColorStateList =
                                ContextCompat.getColorStateList(getContext(), R.color.transparent_black_bg);
                        appBar.setBackgroundTintList(blackTransparentColorStateList);
                    } else {
                        ColorStateList transparentColorStateList =
                                ContextCompat.getColorStateList(getContext(), android.R.color.transparent);
                        appBar.setBackgroundTintList(transparentColorStateList);
                    }

                    if (callState.isCallOngoing()) {
                        operatorStatusView.setOperatorImage(android.R.drawable.star_on, false);
                        operatorNameView.setText(callState.callStatus.getFormattedOperatorName());
                        callTimerView.setText(callState.callStatus.getTime());
                    }
                    chatButtonBadgeView.setText(String.valueOf(callState.messagesNotSeen));

                    chatButtonBadgeView.setVisibility(callState.messagesNotSeen > 0 ? VISIBLE : GONE);
                    videoButton.setVisibility(callState.isVideoCall() ? VISIBLE : GONE);
                    videoButtonLabel.setVisibility(callState.isVideoCall() ? VISIBLE : GONE);
                    operatorStatusView.setVisibility(callState.isAudioCall() ? VISIBLE : GONE);
                    operatorNameView.setVisibility(callState.isCallOngoing() ? VISIBLE : GONE);
                    callTimerView.setVisibility(callState.isCallOngoing() ? VISIBLE : GONE);
                    operatorVideoContainer.setVisibility(callState.isVideoCall() ? VISIBLE : GONE);
                    visitorVideoContainer.setVisibility(callState.is2WayVideoCall() ? VISIBLE : GONE);
                    handleControlsVisibility(callState);
                    if (callState.isVisible) {
                        showCall();
                    } else {
                        hideCall();
                    }
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
                } else if (dialogsState instanceof DialogsState.OverlayPermissionsDialog) {
                    post(() -> showOverlayPermissionsDialog());
                } else if (dialogsState instanceof DialogsState.EndEngagementDialog) {
                    post(() -> showEndEngagementDialog(
                            ((DialogsState.EndEngagementDialog) dialogsState).operatorName));
                } else if (dialogsState instanceof DialogsState.NoMoreOperatorsDialog) {
                    post(() -> showNoMoreOperatorsAvailableDialog());
                } else if (dialogsState instanceof DialogsState.UpgradeDialog) {
                    post(() -> showUpgradeDialog(((DialogsState.UpgradeDialog) dialogsState).type));
                }
            }

            @Override
            public void handleFloatingChatHead(String returnDestination) {
                if (onBubbleListener != null) {
                    onBubbleListener.call(returnDestination);
                }
            }

            @Override
            public void navigateToChat() {
                if (onNavigateToChatListener != null) {
                    onNavigateToChatListener.call();
                }
            }

            @Override
            public void startOperatorVideoView(MediaState operatorMediaState) {
                post(() -> showOperatorVideo(operatorMediaState));
            }

            @Override
            public void startVisitorVideoView(MediaState visitorMediaState) {
                post(() -> showVisitorVideo(visitorMediaState));
            }
        };
        controller = GliaWidgets
                .getControllerFactory()
                .getCallController(callback);
    }

    private void handleControlsVisibility(CallState callState) {
        if (resources.getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
            if (shouldFadeInControls(callState)) {
                TransitionSet transitionSet = new TransitionSet();
                MaterialFade appBarFade = new MaterialFade();
                appBarFade.setSecondaryAnimatorProvider(new SlideDistanceProvider(Gravity.TOP));
                transitionSet.addTransition(appBarFade.addTarget(appBar));

                MaterialFade buttonsFade = new MaterialFade();
                buttonsFade.setSecondaryAnimatorProvider(new SlideDistanceProvider(Gravity.BOTTOM));
                transitionSet.addTransition(buttonsFade.addTarget(buttonsLayoutBackground));
                transitionSet.addTransition(buttonsFade.addTarget(buttonsLayout));

                TransitionManager.beginDelayedTransition(this, transitionSet);
            } else if (shouldFadeOutControls(callState)) {
                TransitionSet transitionSet = new TransitionSet();
                MaterialFade appBarFade = new MaterialFade();
                appBarFade.setSecondaryAnimatorProvider(new SlideDistanceProvider(Gravity.BOTTOM));
                transitionSet.addTransition(appBarFade.addTarget(appBar));

                MaterialFade buttonsFade = new MaterialFade();
                buttonsFade.setSecondaryAnimatorProvider(new SlideDistanceProvider(Gravity.TOP));
                transitionSet.addTransition(buttonsFade.addTarget(buttonsLayoutBackground));
                transitionSet.addTransition(buttonsFade.addTarget(buttonsLayout));

                TransitionManager.beginDelayedTransition(this, transitionSet);
            }
            appBar.setVisibility(callState.landscapeLayoutControlsVisible ? VISIBLE : GONE);
            buttonsLayoutBackground.setVisibility(
                    callState.landscapeLayoutControlsVisible &&
                            callState.isVideoCall() ?
                            VISIBLE : GONE
            );
            buttonsLayout.setVisibility(callState.landscapeLayoutControlsVisible ? VISIBLE : GONE);
        } else {
            appBar.setVisibility(VISIBLE);
            buttonsLayoutBackground.setVisibility(GONE);
            buttonsLayout.setVisibility(VISIBLE);
        }
    }

    private boolean shouldFadeInControls(CallState callState) {
        return callState.landscapeLayoutControlsVisible && appBar.getVisibility() == GONE
                && buttonsLayout.getVisibility() == GONE && buttonsLayoutBackground.getVisibility() == GONE;
    }

    private boolean shouldFadeOutControls(CallState callState) {
        return !callState.landscapeLayoutControlsVisible && appBar.getVisibility() == VISIBLE
                && buttonsLayout.getVisibility() == VISIBLE && buttonsLayoutBackground.getVisibility() == VISIBLE;
    }

    private void setupViewAppearance() {
        if (this.theme.getFontRes() != null) {
            appBar.changeFontFamily(this.theme.getFontRes());
            Typeface fontFamily = ResourcesCompat.getFont(
                    this.getContext(),
                    this.theme.getFontRes());
            operatorNameView.setTypeface(fontFamily);
            callTimerView.setTypeface(fontFamily);
            chatButtonLabel.setTypeface(fontFamily);
            videoButtonLabel.setTypeface(fontFamily);
            muteButtonLabel.setTypeface(fontFamily);
            speakerButtonLabel.setTypeface(fontFamily);
            minimizeButtonLabel.setTypeface(fontFamily);
        }
    }

    private void initConfigurations() {
        setVisibility(INVISIBLE);
        setBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.transparent_black_bg));
        // needed to overlap existing app bar in existing view with this view's app bar.
        ViewCompat.setElevation(this, 100.0f);
    }

    private void initViews() {
        View.inflate(this.getContext(), R.layout.call_view, this);
        appBar = findViewById(R.id.top_app_bar);
        operatorStatusView = findViewById(R.id.operator_status_view);
        operatorNameView = findViewById(R.id.operator_name_view);
        callTimerView = findViewById(R.id.call_timer_view);
        operatorVideoContainer = findViewById(R.id.operator_video_container);
        visitorVideoContainer = findViewById(R.id.visitor_video_container);
        chatButtonLabel = findViewById(R.id.chat_button_label);
        videoButtonLabel = findViewById(R.id.video_button_label);
        chatButtonBadgeView = findViewById(R.id.chat_button_badge);
        muteButtonLabel = findViewById(R.id.mute_button_label);
        speakerButtonLabel = findViewById(R.id.speaker_button_label);
        minimizeButtonLabel = findViewById(R.id.minimize_button_label);
        chatButton = findViewById(R.id.chat_button);
        videoButton = findViewById(R.id.video_button);
        minimizeButton = findViewById(R.id.minimize_button);

        buttonsLayoutBackground = findViewById(R.id.buttons_layout_bg);
        buttonsLayout = findViewById(R.id.buttons_layout);
    }

    private void readTypedArray(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        @SuppressLint("CustomViewStyleable") TypedArray typedArray = this.getContext().obtainStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes);
        setDefaultTheme(typedArray);
        typedArray.recycle();
    }

    private void setDefaultTheme(TypedArray typedArray) {
        this.theme = Utils.getThemeFromTypedArray(typedArray, this.getContext());
    }

    public void setTheme(UiTheme uiTheme) {
        if (uiTheme == null) return;
        Integer fontRes = uiTheme.getFontRes() != null ? uiTheme.getFontRes() : this.theme.getFontRes();

        UiTheme.UiThemeBuilder builder = new UiTheme.UiThemeBuilder();
        builder.setTheme(this.theme);
        builder.setFontRes(fontRes);
        this.theme = builder.build();
        setupViewAppearance();
        if (getVisibility() == VISIBLE) {
            handleStatusbarColor();
        }
    }

    public void setOnBackClickedListener(OnBackClickedListener onBackClicked) {
        this.onBackClickedListener = onBackClicked;
    }

    public void setOnEndListener(OnEndListener onEndListener) {
        this.onEndListener = onEndListener;
    }

    public void setOnNavigateToChatListener(OnNavigateToChatListener onNavigateToChatListener) {
        this.onNavigateToChatListener = onNavigateToChatListener;
    }

    public void setOnBubbleListener(OnBubbleListener onBubbleListener) {
        this.onBubbleListener = onBubbleListener;
    }

    public void backPressed() {
        if (controller != null) {
            controller.onBackArrowClicked();
        }
    }

    private void showCall() {
        setVisibility(VISIBLE);
        handleStatusbarColor();
    }

    private void hideCall() {
        setVisibility(INVISIBLE);
        Activity activity = Utils.getActivity(this.getContext());
        if (defaultStatusbarColor != null && activity != null) {
            activity.getWindow().setStatusBarColor(defaultStatusbarColor);
            defaultStatusbarColor = null;
        }
        Utils.hideSoftKeyboard(this.getContext(), getWindowToken());
    }

    private void handleStatusbarColor() {
        Activity activity = Utils.getActivity(this.getContext());
        if (activity != null && defaultStatusbarColor == null) {
            defaultStatusbarColor = activity.getWindow().getStatusBarColor();
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(
                    this.getContext(), R.color.transparent_black_bg));
        }
    }

    private void showEndEngagementDialog(String operatorName) {
        showOptionsDialog(
                resources.getString(R.string.chat_dialog_end_engagement_title),
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
                    callEnded();
                    alertDialog = null;
                },
                v -> {
                    dismissAlertDialog();
                    if (controller != null) {
                        controller.endEngagementDialogDismissed();
                    }
                    alertDialog = null;
                },
                dialog -> {
                    dialog.dismiss();
                    if (controller != null) {
                        controller.endEngagementDialogDismissed();
                    }
                }
        );
    }

    private void showUpgradeDialog(DialogOfferType type) {
        alertDialog = Dialogs.showUpgradeDialog(
                this.getContext(),
                theme,
                type,
                v -> {
                    dismissAlertDialog();
                    if (controller != null) {
                        controller.acceptUpgradeOfferClicked(type.getUpgradeOffer());
                    }
                },
                v -> {
                    dismissAlertDialog();
                    if (controller != null) {
                        controller.declineUpgradeOfferClicked(type.getUpgradeOffer());
                    }
                });
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

    private void showAlertDialog(@StringRes int title, @StringRes int message,
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
                    callEnded();
                    alertDialog = null;
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
        showOptionsDialog(resources.getString(R.string.chat_dialog_overlay_permissions_title),
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
                    dialog.dismiss();
                    if (controller != null) {
                        controller.overlayPermissionsDialogDismissed();
                    }
                }
        );
    }

    private void dismissAlertDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    private void callEnded() {
        this.getContext().stopService(new Intent(this.getContext(), ChatHeadService.class));
        GliaWidgets.getControllerFactory().destroyControllers();
    }

    private void showOperatorVideo(MediaState operatorMediaState) {
        if (operatorVideoView != null) {
            operatorVideoView.release();
            operatorVideoView = null;
        }
        if (operatorMediaState != null && operatorMediaState.getVideo() != null) {
            operatorVideoView = operatorMediaState.getVideo().createVideoView(Utils.getActivity(this.getContext()));
            operatorVideoContainer.removeAllViews();
            operatorVideoContainer.addView(operatorVideoView);
        }
    }

    private void showVisitorVideo(MediaState visitorMediaState) {
        if (visitorVideoView != null) {
            visitorVideoView.release();
            visitorVideoView = null;
        }
        if (visitorMediaState != null && visitorMediaState.getVideo() != null) {
            visitorVideoView = visitorMediaState.getVideo().createVideoView(Utils.getActivity(this.getContext()));
            visitorVideoContainer.removeAllViews();
            visitorVideoContainer.addView(visitorVideoView);
        }
    }

    public void onUserInteraction() {
        if (controller != null) {
            controller.onUserInteraction();
        }
    }

    public interface OnBackClickedListener {
        void onBackClicked();
    }

    public interface OnEndListener {
        void onEnd();
    }

    public interface OnNavigateToChatListener {
        void call();
    }

    public interface OnBubbleListener {
        void call(String returnDestination);
    }
}
