package com.glia.widgets.call;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
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
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.comms.MediaState;
import com.glia.androidsdk.comms.VideoView;
import com.glia.androidsdk.engagement.Survey;
import com.glia.androidsdk.screensharing.ScreenSharing;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.core.configuration.GliaSdkConfiguration;
import com.glia.widgets.core.dialog.Dialog;
import com.glia.widgets.core.dialog.DialogController;
import com.glia.widgets.core.dialog.model.DialogState;
import com.glia.widgets.core.notification.device.NotificationManager;
import com.glia.widgets.core.screensharing.ScreenSharingController;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.view.Dialogs;
import com.glia.widgets.view.OperatorStatusView;
import com.glia.widgets.view.floatingvisitorvideoview.FloatingVisitorVideoContainer;
import com.glia.widgets.view.head.controller.ServiceChatHeadController;
import com.glia.widgets.view.header.AppBarView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;
import com.google.android.material.transition.MaterialFade;
import com.google.android.material.transition.SlideDistanceProvider;

public class CallView extends ConstraintLayout {
    private final String TAG = CallView.class.getSimpleName();

    private CallViewCallback callback;
    private CallController callController;

    private final ScreenSharingController.ViewCallback screenSharingViewCallback = exception ->
            showToast(exception.debugMessage);
    private ScreenSharingController screenSharingController;

    private ServiceChatHeadController serviceChatHeadController;

    private DialogController.Callback dialogCallback;
    private DialogController dialogController;

    private AlertDialog alertDialog;
    private AppBarView appBar;
    private OperatorStatusView operatorStatusView;
    private TextView operatorNameView;
    private TextView companyNameView;
    private TextView msrView;
    private TextView callTimerView;
    private TextView connectingView;
    private TextView continueBrowsingView;
    private FrameLayout operatorVideoContainer;
    private VideoView operatorVideoView;
    private TextView chatButtonLabel;
    private TextView videoButtonLabel;
    private TextView muteButtonLabel;
    private TextView speakerButtonLabel;
    private TextView minimizeButtonLabel;
    private View buttonsLayoutBackground;
    private View buttonsLayout;
    private FloatingVisitorVideoContainer floatingVisitorVideoContainer;
    private FloatingActionButton chatButton;
    private FloatingActionButton videoButton;
    private FloatingActionButton muteButton;
    private FloatingActionButton speakerButton;
    private FloatingActionButton minimizeButton;
    private TextView chatButtonBadgeView;
    private TextView onHoldTextView;

    private OnBackClickedListener onBackClickedListener;
    private OnEndListener onEndListener;
    private OnMinimizeListener onMinimizeListener;
    private OnNavigateToChatListener onNavigateToChatListener;
    private CallView.OnNavigateToSurveyListener onNavigateToSurveyListener;
    private OnTitleUpdatedListener onTitleUpdatedListener;

    private UiTheme theme;

    private final Resources resources;
    private Integer defaultStatusbarColor;
    private final AudioManager audioManager;

    public CallView(Context context) {
        this(context, null);
    }

    public CallView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.gliaChatStyle);
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
        this.audioManager =
                (AudioManager) this.getContext().getSystemService(Context.AUDIO_SERVICE);
        initConfigurations();
        initViews();
        readTypedArray(attrs, defStyleAttr, defStyleRes);
        setupViewAppearance();
        setupViewActions();
        setupControllers();
    }

    private void setupViewActions() {
        appBar.setOnBackClickedListener(() -> {
            callController.onBackClicked();

            if (onBackClickedListener != null) {
                onBackClickedListener.onBackClicked();
            }
        });
        appBar.setOnEndChatClickedListener(() -> {
            if (callController != null) {
                callController.leaveChatClicked();
            }
        });
        appBar.setOnXClickedListener(() -> {
            if (callController != null) {
                callController.leaveChatQueueClicked();
            }
        });
        chatButton.setOnClickListener(v -> {
            if (callController != null) {
                callController.chatButtonClicked();
            }
        });
        speakerButton.setOnClickListener(v -> callController.onSpeakerButtonPressed());
        minimizeButton.setOnClickListener(v -> {
            if (callController != null) {
                callController.minimizeButtonClicked();
            }
        });
        muteButton.setOnClickListener(v -> {
            if (callController != null) {
                callController.muteButtonClicked();
            }
        });

        videoButton.setOnClickListener(v -> {
            if (callController != null) {
                callController.videoButtonClicked();
            }
        });
    }

    public void startCall(String companyName,
                          String queueId,
                          String  visitorContextAssetId,
                          boolean useOverlays,
                          ScreenSharing.Mode screenSharingMode,
                          Engagement.MediaType mediaType) {
        Dependencies.getSdkConfigurationManager().setUseOverlay(useOverlays);
        Dependencies.getSdkConfigurationManager().setScreenSharingMode(screenSharingMode);
        if (callController != null) {
            callController.initCall(
                    companyName,
                    queueId,
                    visitorContextAssetId,
                    mediaType
            );
        }
        if (serviceChatHeadController != null) {
            serviceChatHeadController.init();
        }
    }

    public void onDestroy(boolean isFinishing) {
        releaseOperatorVideoStream();
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
        onEndListener = null;
        onBackClickedListener = null;
        onNavigateToChatListener = null;
        onNavigateToSurveyListener = null;
        destroyControllers(isFinishing);
        callback = null;
    }

    public void onResume() {
        floatingVisitorVideoContainer.onResume();
        if (callController != null) {
            callController.onResume();
            if (operatorVideoView != null) {
                operatorVideoView.resumeRendering();
            }
        }
        if (screenSharingController != null) {
            screenSharingController.setViewCallback(screenSharingViewCallback);
            screenSharingController.onResume(this.getContext());
        }
        if (serviceChatHeadController != null) {
            serviceChatHeadController.onResume(this);
        }
        if (dialogController != null) {
            dialogController.addCallback(dialogCallback);
        }
    }

    public void onPause() {
        floatingVisitorVideoContainer.onPause();
        if (operatorVideoView != null) {
            operatorVideoView.pauseRendering();
        }
        if (screenSharingController != null) {
            screenSharingController.removeViewCallback(screenSharingViewCallback);
        }
        if (dialogController != null) {
            dialogController.removeCallback(dialogCallback);
        }
        if (serviceChatHeadController != null) {
            serviceChatHeadController.onPause(this);
        }
        if (callController != null) {
            callController.onPause();
        }
    }

    private void destroyControllers(boolean isFinishing) {
        if (serviceChatHeadController != null && isFinishing) {
            serviceChatHeadController.onDestroy();
        }
        callController.setViewCallback(null);
        callController = null;
        screenSharingController = null;
        dialogController = null;
    }

    private void setupControllers() {
        callback = new CallViewCallback() {
            @Override
            public void emitState(CallState callState) {
                post(() -> {
                    if (callState.isMediaEngagementStarted()) {
                        appBar.showEndButton();
                    } else {
                        appBar.showXButton();
                    }
                    if (callState.requestedMediaType == Engagement.MediaType.VIDEO) {
                        setTitle(resources.getString(R.string.glia_call_video_app_bar_title));
                    } else {
                        setTitle(resources.getString(R.string.glia_call_audio_app_bar_title));
                    }
                    operatorNameView.setText(callState.callStatus.getFormattedOperatorName());
                    connectingView.setText(resources.getString(
                            R.string.glia_call_connecting_with,
                            callState.callStatus.getFormattedOperatorName(),
                            callState.callStatus.getTime()
                    ));
                    connectingView.setContentDescription(resources.getString(
                            R.string.glia_call_connecting_with,
                            callState.callStatus.getFormattedOperatorName(),
                            ""
                    ));
                    if (callState.companyName != null) {
                        companyNameView.setText(callState.companyName);
                        msrView.setText(R.string.glia_call_in_queue_message);
                    }
                    chatButtonBadgeView.setText(String.valueOf(callState.messagesNotSeen));
                    if (resources.getConfiguration().orientation == ORIENTATION_LANDSCAPE &&
                            callState.isVideoCall()) {
                        ColorStateList blackTransparentColorStateList =
                                ContextCompat.getColorStateList(getContext(), R.color.glia_transparent_black_bg);
                        appBar.setBackgroundTintList(blackTransparentColorStateList);
                    } else {
                        ColorStateList transparentColorStateList =
                                ContextCompat.getColorStateList(getContext(), android.R.color.transparent);
                        appBar.setBackgroundTintList(transparentColorStateList);
                    }

                    muteButton.setEnabled(callState.isMuteButtonEnabled());
                    speakerButton.setEnabled(callState.isSpeakerButtonEnabled());
                    videoButton.setEnabled(callState.isVideoButtonEnabled());
                    setButtonActivated(
                            videoButton,
                            theme.getIconCallVideoOn(),
                            theme.getIconCallVideoOff(),
                            R.string.glia_call_video_on_content_description,
                            R.string.glia_call_video_off_content_description,
                            callState.hasVideo
                    );
                    setButtonActivated(
                            muteButton,
                            theme.getIconCallAudioOff(),    // mute (eg. mic-off) button activated icon
                            theme.getIconCallAudioOn(),     // mute (eg. mic-off) button deactivated icon
                            R.string.glia_call_mute_content_description,
                            R.string.glia_call_unmute_content_description,
                            callState.isMuted
                    );
                    muteButtonLabel.setText(callState.isMuted ?
                            R.string.glia_call_mute_button_unmute :
                            R.string.glia_call_mute_button_mute
                    );

                    chatButtonBadgeView.setVisibility(callState.messagesNotSeen > 0 ? VISIBLE : GONE);
                    videoButton.setVisibility(callState.is2WayVideoCall() ? VISIBLE : GONE);
                    videoButtonLabel.setVisibility(callState.is2WayVideoCall() ? VISIBLE : GONE);
                    operatorNameView.setVisibility(callState.showOperatorNameView() ? VISIBLE : GONE);
                    companyNameView.setVisibility(callState.showCompanyNameView() ? VISIBLE : GONE);
                    msrView.setVisibility(callState.isCallNotOngoing() ? VISIBLE : GONE);
                    connectingView.setVisibility(callState.isCallOngoingAndOperatorIsConnecting() ? VISIBLE : GONE);
                    onHoldTextView.setVisibility(callState.showOnHold() ? VISIBLE : GONE);
                    handleCallTimerView(callState);
                    handleContinueBrowsingView(callState);
                    handleOperatorStatusViewState(callState);
                    handleOperatorVideoState(callState);
                    handleControlsVisibility(callState);
                    onIsSpeakerOnStateChanged(callState.isSpeakerOn);
                    if (callState.isVisible) {
                        showUIOnCallOngoing();
                    } else {
                        hideUIOnCallEnd();
                    }
                    chatButton.setEnabled(callState.isAudioCall() || callState.isVideoCall() || callState.is2WayVideoCall());
                    chatButton.setContentDescription(callState.messagesNotSeen == 0 ?
                            resources.getString(R.string.glia_call_chat_zero_content_description) :
                            resources.getQuantityString(R.plurals.glia_call_chat_content_description,
                                    callState.messagesNotSeen, callState.messagesNotSeen));
                });
            }

            @Override
            public void navigateToChat() {
                if (onNavigateToChatListener != null) {
                    onNavigateToChatListener.call();
                }
            }

            @Override
            public void navigateToSurvey(@NonNull Survey survey) {
                if (onNavigateToSurveyListener != null) {
                    onNavigateToSurveyListener.onSurvey(survey);
                }
            }

            @Override
            public void destroyView() {
                if (onEndListener != null) {
                    onEndListener.onEnd();
                }
            }

            @Override
            public void minimizeView() {
                if (onMinimizeListener != null) {
                    onMinimizeListener.onMinimize();
                }
            }
        };

        callController = Dependencies
                .getControllerFactory()
                .getCallController(callback);

        dialogCallback = dialogState -> {
            switch (dialogState.getMode()) {
                case Dialog.MODE_NONE:
                    dismissAlertDialog();
                    break;
                case Dialog.MODE_UNEXPECTED_ERROR:
                    post(this::showUnexpectedErrorDialog);
                    break;
                case Dialog.MODE_EXIT_QUEUE:
                    post(this::showExitQueueDialog);
                    break;
                case Dialog.MODE_OVERLAY_PERMISSION:
                    post(this::showOverlayPermissionsDialog);
                    break;
                case Dialog.MODE_END_ENGAGEMENT:
                    post(() -> showEndEngagementDialog(((DialogState.OperatorName) dialogState).getOperatorName()));
                    break;
                case Dialog.MODE_MEDIA_UPGRADE:
                    post(() -> showUpgradeDialog(((DialogState.MediaUpgrade) dialogState)));
                    break;
                case Dialog.MODE_NO_MORE_OPERATORS:
                    post(this::showNoMoreOperatorsAvailableDialog);
                    break;
                case Dialog.MODE_ENGAGEMENT_ENDED:
                    post(this::showEngagementEndedDialog);
                    break;
                case Dialog.MODE_START_SCREEN_SHARING:
                    post(this::showScreenSharingDialog);
                    break;
                case Dialog.MODE_ENABLE_NOTIFICATION_CHANNEL:
                    post(this::showAllowNotificationsDialog);
                    break;
                case Dialog.MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING:
                    post(this::showAllowScreenSharingNotificationsAndStartSharingDialog);
                    break;
            }
        };

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

    private void setTitle(String title) {
        if (onTitleUpdatedListener != null) {
            onTitleUpdatedListener.onTitleUpdated(title);
        }
        appBar.setTitle(title);
    }

    private void handleCallTimerView(CallState callState) {
        callTimerView.setVisibility(callState.showCallTimerView() ? VISIBLE : GONE);
        if (callState.callStatus.getTime() != null) {
            callTimerView.setText(callState.callStatus.getTime());
        }
    }

    private void handleContinueBrowsingView(CallState callState) {
        continueBrowsingView.setVisibility(
                resources.getConfiguration().orientation != ORIENTATION_LANDSCAPE &&
                        callState.showContinueBrowsingView() ? VISIBLE : GONE
        );
        continueBrowsingView.setText(
                getResources().getString(
                        callState.showOnHold() ?
                                R.string.glia_call_continue_browsing_on_hold :
                                R.string.glia_call_continue_browsing
                )
        );
    }

    private void handleOperatorStatusViewState(CallState state) {
        operatorStatusView.setShowRippleAnimation(state.showOperatorStatusViewRippleAnimation());
        operatorStatusView.setShowOnHold(state.showOnHold());
        if (state.isTransferring()) {
            operatorStatusView.showTransferring();
            operatorNameView.setText(R.string.glia_chat_visitor_status_transferring);
        } else {
            handleOperatorStatusViewOperatorImage(state);
        }
        operatorStatusView.setVisibility(state.showOperatorStatusView() ? VISIBLE : GONE);
    }

    private void handleOperatorStatusViewOperatorImage(CallState state) {
        if (state.isCallOngoingAndOperatorConnected()) {
            showOperatorProfileImageOnConnected(state);
        } else if (state.isCallOngoingAndOperatorIsConnecting()) {
            showOperatorProfileImageOnConnecting(state);
        } else {
            operatorStatusView.showPlaceholder();
        }
    }

    private void showOperatorProfileImageOnConnected(CallState state) {
        if (state.callStatus.getOperatorProfileImageUrl() != null) {
            operatorStatusView.showProfileImageOnConnect(state.callStatus.getOperatorProfileImageUrl());
        } else {
            operatorStatusView.showPlaceHolderWithIconPaddingOnConnect();
        }
    }

    private void showOperatorProfileImageOnConnecting(CallState state) {
        if (state.callStatus.getOperatorProfileImageUrl() != null) {
            operatorStatusView.showProfileImage(state.callStatus.getOperatorProfileImageUrl());
        } else {
            operatorStatusView.showPlaceholder();
        }
    }

    private void handleOperatorVideoState(CallState state) {
        if (state.showOperatorVideo() && operatorVideoContainer.getVisibility() == GONE) {
            operatorVideoContainer.setVisibility(VISIBLE);
            showOperatorVideo(state.callStatus.getOperatorMediaState());
        } else if (!state.showOperatorVideo() && operatorVideoContainer.getVisibility() == VISIBLE) {
            operatorVideoContainer.setVisibility(GONE);
            hideOperatorVideo();
        }
    }

    private void onIsSpeakerOnStateChanged(boolean isSpeakerOn) {
        if (isSpeakerOn != audioManager.isSpeakerphoneOn()) {
            post(() -> audioManager.setSpeakerphoneOn(isSpeakerOn));
        }
        setButtonActivated(
                speakerButton,
                theme.getIconCallSpeakerOn(),
                theme.getIconCallSpeakerOff(),
                R.string.glia_call_speaker_on_content_description,
                R.string.glia_call_speaker_off_content_description,
                isSpeakerOn
        );
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
                    if (callController != null) {
                        callController.endEngagementDialogYesClicked();
                    }
                    if (onEndListener != null) {
                        onEndListener.onEnd();
                    }
                    callEnded();
                },
                v -> {
                    dismissAlertDialog();
                    if (callController != null) {
                        callController.endEngagementDialogDismissed();
                    }
                },
                dialog -> {
                    dialog.dismiss();
                    if (callController != null) {
                        callController.endEngagementDialogDismissed();
                    }
                },
                true
        );
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
                        callController.notificationsDialogDismissed();
                        NotificationManager.openNotificationChannelScreen(this.getContext());
                    },
                    view -> {
                        dismissAlertDialog();
                        callController.notificationsDialogDismissed();
                        screenSharingController.onScreenSharingDeclined();
                    },
                    dialog -> {
                        dialog.dismiss();
                        callController.notificationsDialogDismissed();
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
                        callController.notificationsDialogDismissed();
                        NotificationManager.openNotificationChannelScreen(this.getContext());
                    },
                    view -> {
                        dismissAlertDialog();
                        callController.notificationsDialogDismissed();
                    },
                    dialog -> {
                        dialog.dismiss();
                        callController.notificationsDialogDismissed();
                    }
            );
        }
    }

    private void showScreenSharingDialog() {
        if (alertDialog == null || !alertDialog.isShowing())
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

    private void setButtonActivated(FloatingActionButton floatingActionButton,
                                    Integer activatedDrawableRes,
                                    Integer notActivatedDrawableRes,
                                    @StringRes int activatedContentDescription,
                                    @StringRes int notActivatedContentDescription,
                                    boolean isActivated
    ) {
        floatingActionButton.setActivated(isActivated);
        floatingActionButton.setImageResource(isActivated ? activatedDrawableRes : notActivatedDrawableRes);
        floatingActionButton.setContentDescription(resources.getString(isActivated ?
                activatedContentDescription :
                notActivatedContentDescription
        ));
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
        setAppBarTheme();
        // icons
        operatorStatusView.setTheme(theme);
        chatButton.setImageResource(theme.getIconCallChat());
        videoButton.setImageResource(theme.getIconCallVideoOn());
        muteButton.setImageResource(theme.getIconCallAudioOn());
        speakerButton.setImageResource(theme.getIconCallSpeakerOn());
        minimizeButton.setImageResource(theme.getIconCallMinimize());
        chatButtonBadgeView.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), theme.getBrandPrimaryColor()));
        // fonts
        if (theme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(
                    getContext(),
                    theme.getFontRes()
            );
            operatorNameView.setTypeface(fontFamily);
            companyNameView.setTypeface(fontFamily);
            msrView.setTypeface(fontFamily);
            callTimerView.setTypeface(fontFamily);
            connectingView.setTypeface(fontFamily);
            continueBrowsingView.setTypeface(fontFamily);
            chatButtonLabel.setTypeface(fontFamily);
            videoButtonLabel.setTypeface(fontFamily);
            muteButtonLabel.setTypeface(fontFamily);
            speakerButtonLabel.setTypeface(fontFamily);
            minimizeButtonLabel.setTypeface(fontFamily);
        }
    }

    private void setAppBarTheme() {
        UiTheme.UiThemeBuilder builder = new UiTheme.UiThemeBuilder();
        builder.setTheme(theme);
        builder.setSystemNegativeColor(R.color.glia_system_negative_color);
        builder.setBaseLightColor(R.color.glia_base_light_color);
        builder.setBrandPrimaryColor(android.R.color.transparent);
        builder.setGliaChatHeaderTitleTintColor(android.R.color.white);
        builder.setGliaChatHeaderHomeButtonTintColor(android.R.color.white);
        builder.setGliaChatHeaderExitQueueButtonTintColor(android.R.color.white);
        builder.setFontRes(theme.getFontRes());
        appBar.setTheme(builder.build());
    }

    private void initConfigurations() {
        setVisibility(INVISIBLE);
        setBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.glia_transparent_black_bg));
        // needed to overlap existing app bar in existing view with this view's app bar.
        ViewCompat.setElevation(this, 100.0f);
    }

    private void initViews() {
        View.inflate(this.getContext(), R.layout.call_view, this);
        appBar = findViewById(R.id.top_app_bar);
        operatorStatusView = findViewById(R.id.operator_status_view);
        operatorNameView = findViewById(R.id.operator_name_view);
        companyNameView = findViewById(R.id.company_name_view);
        msrView = findViewById(R.id.msr_view);
        callTimerView = findViewById(R.id.call_timer_view);
        connectingView = findViewById(R.id.connecting_view);
        continueBrowsingView = findViewById(R.id.continue_browsing_view);
        operatorVideoContainer = findViewById(R.id.operator_video_container);
        chatButtonLabel = findViewById(R.id.chat_button_label);
        videoButtonLabel = findViewById(R.id.video_button_label);
        chatButtonBadgeView = findViewById(R.id.chat_button_badge);
        muteButtonLabel = findViewById(R.id.mute_button_label);
        speakerButtonLabel = findViewById(R.id.speaker_button_label);
        minimizeButtonLabel = findViewById(R.id.minimize_button_label);
        chatButton = findViewById(R.id.chat_button);
        videoButton = findViewById(R.id.video_button);
        muteButton = findViewById(R.id.mute_button);
        speakerButton = findViewById(R.id.speaker_button);
        minimizeButton = findViewById(R.id.minimize_button);

        buttonsLayoutBackground = findViewById(R.id.buttons_layout_bg);
        buttonsLayout = findViewById(R.id.buttons_layout);
        onHoldTextView = findViewById(R.id.on_hold_text);

        floatingVisitorVideoContainer = findViewById(R.id.floating_visitor_video);
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
        this.theme = Utils.getFullHybridTheme(uiTheme, this.theme);
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

    public void setOnMinimizeListener(OnMinimizeListener onMinimizeListener) {
        this.onMinimizeListener = onMinimizeListener;
    }

    public void setOnNavigateToChatListener(OnNavigateToChatListener onNavigateToChatListener) {
        this.onNavigateToChatListener = onNavigateToChatListener;
    }

    public void setOnNavigateToSurveyListener(CallView.OnNavigateToSurveyListener onNavigateToSurveyListener) {
        this.onNavigateToSurveyListener = onNavigateToSurveyListener;
    }

    public void setOnTitleUpdatedListener(OnTitleUpdatedListener onTitleUpdatedListener) {
        this.onTitleUpdatedListener = onTitleUpdatedListener;
    }

    private void showUIOnCallOngoing() {
        setVisibility(VISIBLE);
        handleStatusbarColor();
    }

    private void hideUIOnCallEnd() {
        setVisibility(INVISIBLE);
        Activity activity = Utils.getActivity(this.getContext());
        hideOperatorVideo();
        hideVisitorVideo();
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
                    this.getContext(), R.color.glia_transparent_black_bg));
        }
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
                    if (callController != null) {
                        callController.endEngagementDialogYesClicked();
                    }
                    alertDialog = null;
                },
                v -> {
                    dismissAlertDialog();
                    if (callController != null) {
                        callController.endEngagementDialogDismissed();
                    }
                    alertDialog = null;
                },
                dialog -> {
                    dialog.dismiss();
                    if (callController != null) {
                        callController.endEngagementDialogDismissed();
                    }
                },
                true
        );
    }

    private void showUpgradeDialog(DialogState.MediaUpgrade mediaUpgrade) {
        alertDialog = Dialogs.showUpgradeDialog(
                this.getContext(),
                theme,
                mediaUpgrade,
                v -> {
                    if (callController != null) {
                        callController.acceptUpgradeOfferClicked(mediaUpgrade.getMediaUpgradeOffer());
                    }
                },
                v -> {
                    if (callController != null) {
                        callController.declineUpgradeOfferClicked(mediaUpgrade.getMediaUpgradeOffer());
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
        dismissAlertDialog();
        alertDialog = Dialogs.showAlertDialog(
                this.getContext(),
                this.theme,
                title,
                message,
                buttonClickListener);
    }

    private void showEngagementEndedDialog() {
        dismissAlertDialog();
        alertDialog = Dialogs.showOperatorEndedEngagementDialog(
                this.getContext(),
                this.theme,
                v -> {
                    dismissAlertDialog();
                    if (callController != null) {
                        callController.noMoreOperatorsAvailableDismissed();
                    }
                    if (onEndListener != null) {
                        onEndListener.onEnd();
                    }
                    callEnded();
                });
    }

    private void showNoMoreOperatorsAvailableDialog() {
        showAlertDialog(
                R.string.glia_dialog_operators_unavailable_title,
                R.string.glia_dialog_operators_unavailable_message,
                v -> {
                    dismissAlertDialog();
                    if (callController != null) {
                        callController.noMoreOperatorsAvailableDismissed();
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
                R.string.glia_dialog_unexpected_error_title,
                R.string.glia_dialog_unexpected_error_message,
                v -> {
                    dismissAlertDialog();
                    if (callController != null) {
                        callController.unexpectedErrorDialogDismissed();
                    }
                    if (onEndListener != null) {
                        onEndListener.onEnd();
                    }
                }
        );
    }

    void showMissingPermissionsDialog() {
        showAlertDialog(
                R.string.glia_dialog_permission_error_title,
                R.string.glia_dialog_permission_error_message,
                v -> {
                    dismissAlertDialog();
                    if (callController != null) {
                        callController.unexpectedErrorDialogDismissed();
                    }
                    if (onEndListener != null) {
                        onEndListener.onEnd();
                    }
                }
        );
    }

    private void showOverlayPermissionsDialog() {
        showOptionsDialog(resources.getString(R.string.glia_dialog_overlay_permissions_title),
                resources.getString(R.string.glia_dialog_overlay_permissions_message),
                resources.getString(R.string.glia_dialog_overlay_permissions_ok),
                resources.getString(R.string.glia_dialog_overlay_permissions_no),
                v -> {
                    dismissAlertDialog();
                    if (callController != null) {
                        callController.overlayPermissionsDialogDismissed();
                    }
                    Intent overlayIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + this.getContext().getPackageName()));
                    overlayIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    this.getContext().startActivity(overlayIntent);
                },
                v -> {
                    dismissAlertDialog();
                    if (callController != null) {
                        callController.overlayPermissionsDialogDismissed();
                    }
                },
                dialog -> {
                    dialog.dismiss();
                    if (callController != null) {
                        callController.overlayPermissionsDialogDismissed();
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
        Dependencies.getControllerFactory().destroyControllers();
    }

    private void showOperatorVideo(MediaState operatorMediaState) {
        releaseOperatorVideoStream();
        if (operatorMediaState != null && operatorMediaState.getVideo() != null) {
            Logger.d(TAG, "Starting video operator");
            operatorVideoView = operatorMediaState.getVideo().createVideoView(Utils.getActivity(this.getContext()));
            operatorVideoContainer.removeAllViews();
            operatorVideoContainer.addView(operatorVideoView);
            operatorVideoContainer.invalidate();
        }
    }

    private void releaseOperatorVideoStream() {
        if (operatorVideoView != null) {
            operatorVideoView.release();
            operatorVideoView = null;
        }
    }

    private void hideOperatorVideo() {
        operatorVideoContainer.removeAllViews();
        operatorVideoContainer.invalidate();
        releaseOperatorVideoStream();
    }

    private void hideVisitorVideo() {
        floatingVisitorVideoContainer.hide();
    }

    public void onUserInteraction() {
        if (callController != null) {
            callController.onUserInteraction();
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
        void onBackClicked();
    }

    public interface OnEndListener {
        void onEnd();
    }

    public interface OnMinimizeListener {
        void onMinimize();
    }

    public interface OnNavigateToChatListener {
        void call();
    }

    public interface OnNavigateToSurveyListener {
        void onSurvey(@NonNull Survey survey);
    }

    public interface OnTitleUpdatedListener {
        void onTitleUpdated(String title);
    }

    @Deprecated
    public boolean shouldShowMediaEngagementView() {
        return shouldShowMediaEngagementView(false);
    }

    public boolean shouldShowMediaEngagementView(boolean isUpgradeToCall) {
        if (callController != null) {
            return callController.shouldShowMediaEngagementView(isUpgradeToCall);
        }
        return false;
    }
}
