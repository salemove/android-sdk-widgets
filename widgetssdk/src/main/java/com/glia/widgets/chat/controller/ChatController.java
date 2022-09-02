package com.glia.widgets.chat.controller;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.Operator;
import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.androidsdk.chat.Chat;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.chat.FilesAttachment;
import com.glia.androidsdk.chat.MessageAttachment;
import com.glia.androidsdk.chat.SingleChoiceAttachment;
import com.glia.androidsdk.chat.SingleChoiceOption;
import com.glia.androidsdk.chat.VisitorMessage;
import com.glia.androidsdk.comms.MediaDirection;
import com.glia.androidsdk.comms.MediaUpgradeOffer;
import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.androidsdk.engagement.EngagementFile;
import com.glia.androidsdk.engagement.Survey;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.androidsdk.site.SiteInfo;
import com.glia.widgets.Constants;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.chat.ChatView;
import com.glia.widgets.chat.ChatViewCallback;
import com.glia.widgets.chat.adapter.ChatAdapter;
import com.glia.widgets.chat.domain.GliaLoadHistoryUseCase;
import com.glia.widgets.chat.domain.GliaOnMessageUseCase;
import com.glia.widgets.chat.domain.GliaOnOperatorTypingUseCase;
import com.glia.widgets.chat.domain.GliaSendMessagePreviewUseCase;
import com.glia.widgets.chat.domain.GliaSendMessageUseCase;
import com.glia.widgets.chat.domain.IsEnableChatEditTextUseCase;
import com.glia.widgets.chat.domain.IsFromCallScreenUseCase;
import com.glia.widgets.chat.domain.IsShowSendButtonUseCase;
import com.glia.widgets.chat.domain.SiteInfoUseCase;
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase;
import com.glia.widgets.chat.model.ChatInputMode;
import com.glia.widgets.chat.model.ChatState;
import com.glia.widgets.chat.model.history.ChatItem;
import com.glia.widgets.chat.model.history.MediaUpgradeStartedTimerItem;
import com.glia.widgets.chat.model.history.OperatorAttachmentItem;
import com.glia.widgets.chat.model.history.OperatorChatItem;
import com.glia.widgets.chat.model.history.OperatorMessageItem;
import com.glia.widgets.chat.model.history.OperatorStatusItem;
import com.glia.widgets.chat.model.history.VisitorAttachmentItem;
import com.glia.widgets.chat.model.history.VisitorMessageItem;
import com.glia.widgets.core.dialog.DialogController;
import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase;
import com.glia.widgets.core.engagement.domain.GetEngagementStateFlowableUseCase;
import com.glia.widgets.core.engagement.domain.GliaEndEngagementUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementEndUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementUseCase;
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal;
import com.glia.widgets.core.engagement.domain.model.EngagementStateEvent;
import com.glia.widgets.core.engagement.domain.model.EngagementStateEventVisitor;
import com.glia.widgets.core.fileupload.domain.AddFileAttachmentsObserverUseCase;
import com.glia.widgets.core.fileupload.domain.AddFileToAttachmentAndUploadUseCase;
import com.glia.widgets.core.fileupload.domain.GetFileAttachmentsUseCase;
import com.glia.widgets.core.fileupload.domain.RemoveFileAttachmentObserverUseCase;
import com.glia.widgets.core.fileupload.domain.RemoveFileAttachmentUseCase;
import com.glia.widgets.core.fileupload.domain.SupportedFileCountCheckUseCase;
import com.glia.widgets.core.fileupload.model.FileAttachment;
import com.glia.widgets.core.mediaupgradeoffer.MediaUpgradeOfferRepository;
import com.glia.widgets.core.mediaupgradeoffer.MediaUpgradeOfferRepositoryCallback;
import com.glia.widgets.core.notification.domain.RemoveCallNotificationUseCase;
import com.glia.widgets.core.notification.domain.ShowAudioCallNotificationUseCase;
import com.glia.widgets.core.notification.domain.ShowVideoCallNotificationUseCase;
import com.glia.widgets.core.operator.GliaOperatorMediaRepository;
import com.glia.widgets.core.operator.domain.AddOperatorMediaStateListenerUseCase;
import com.glia.widgets.core.queue.domain.GliaCancelQueueTicketUseCase;
import com.glia.widgets.core.queue.domain.GliaQueueForChatEngagementUseCase;
import com.glia.widgets.core.queue.domain.exception.QueueingOngoingException;
import com.glia.widgets.core.survey.OnSurveyListener;
import com.glia.widgets.core.survey.domain.GliaSurveyUseCase;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.filepreview.domain.usecase.DownloadFileUseCase;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.TimeCounter;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.view.MessagesNotSeenHandler;
import com.glia.widgets.view.MinimizeHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ChatController implements
        GliaOnEngagementUseCase.Listener,
        GliaOnEngagementEndUseCase.Listener,
        OnSurveyListener {

    private final static String TAG = ChatController.class.getSimpleName();
    private final static String EMPTY_MESSAGE = "";

    @Nullable
    private ChatViewCallback viewCallback;
    private MediaUpgradeOfferRepositoryCallback mediaUpgradeOfferRepositoryCallback;
    private TimeCounter.FormattedTimerStatusListener timerStatusListener;
    private final MediaUpgradeOfferRepository mediaUpgradeOfferRepository;
    private final TimeCounter callTimer;
    private final MinimizeHandler minimizeHandler;
    private final MessagesNotSeenHandler messagesNotSeenHandler;

    private final CompositeDisposable disposable = new CompositeDisposable();
    private Disposable engagementStateEventDisposable = null;

    private final GliaOperatorMediaRepository.OperatorMediaStateListener operatorMediaStateListener = this::onNewOperatorMediaState;

    private final GliaSendMessageUseCase.Listener sendMessageCallback = new GliaSendMessageUseCase.Listener() {
        @Override
        public void messageSent(VisitorMessage message) {
            onMessageSent(message);
        }

        @Override
        public void onMessageValidated() {
            if (viewCallback != null) {
                viewCallback.clearMessageInput();
            }
            emitViewState(
                    chatState
                            .setLastTypedText(EMPTY_MESSAGE)
                            .setShowSendButton(isShowSendButtonUseCase.execute(EMPTY_MESSAGE))
            );
        }

        @Override
        public void errorOperatorNotOnline(String message) {
            onSendMessageOperatorOffline(message);
        }

        @Override
        public void errorMessageInvalid() {

        }

        @Override
        public void error(GliaException ex) {
            onMessageSendError(ex);
        }
    };

    private final DialogController dialogController;

    private final ShowAudioCallNotificationUseCase showAudioCallNotificationUseCase;
    private final ShowVideoCallNotificationUseCase showVideoCallNotificationUseCase;
    private final RemoveCallNotificationUseCase removeCallNotificationUseCase;
    private final GliaLoadHistoryUseCase loadHistoryUseCase;
    private final GliaOnEngagementUseCase getEngagementUseCase;
    private final GliaOnEngagementEndUseCase engagementEndUseCase;
    private final GliaOnMessageUseCase onMessageUseCase;
    private final GliaOnOperatorTypingUseCase onOperatorTypingUseCase;
    private final GliaSendMessagePreviewUseCase sendMessagePreviewUseCase;
    private final GliaSendMessageUseCase sendMessageUseCase;
    private final AddOperatorMediaStateListenerUseCase addOperatorMediaStateListenerUseCase;
    private final GliaCancelQueueTicketUseCase cancelQueueTicketUseCase;
    private final GliaEndEngagementUseCase endEngagementUseCase;
    private final GliaQueueForChatEngagementUseCase queueForChatEngagementUseCase;
    private final AddFileAttachmentsObserverUseCase addFileAttachmentsObserverUseCase;
    private final RemoveFileAttachmentObserverUseCase removeFileAttachmentObserverUseCase;
    private final AddFileToAttachmentAndUploadUseCase addFileToAttachmentAndUploadUseCase;
    private final GetFileAttachmentsUseCase getFileAttachmentsUseCase;
    private final RemoveFileAttachmentUseCase removeFileAttachmentUseCase;
    private final SupportedFileCountCheckUseCase supportedFileCountCheckUseCase;
    private final IsShowSendButtonUseCase isShowSendButtonUseCase;
    private final IsShowOverlayPermissionRequestDialogUseCase isShowOverlayPermissionRequestDialogUseCase;
    private final DownloadFileUseCase downloadFileUseCase;
    private final IsEnableChatEditTextUseCase isEnableChatEditTextUseCase;
    private final SiteInfoUseCase siteInfoUseCase;
    private final GliaSurveyUseCase surveyUseCase;
    private final GetEngagementStateFlowableUseCase getGliaEngagementStateFlowableUseCase;
    private final IsFromCallScreenUseCase isFromCallScreenUseCase;
    private final UpdateFromCallScreenUseCase updateFromCallScreenUseCase;

    private boolean isVisitorEndEngagement = false;
    private volatile boolean isChatViewPaused = false;

    // TODO pending photoCaptureFileUri - need to move some place better
    private Uri photoCaptureFileUri = null;

    public ChatController(
            MediaUpgradeOfferRepository mediaUpgradeOfferRepository,
            TimeCounter sharedTimer,
            @Nullable ChatViewCallback chatViewCallback,
            MinimizeHandler minimizeHandler,
            DialogController dialogController,
            MessagesNotSeenHandler messagesNotSeenHandler,
            ShowAudioCallNotificationUseCase showAudioCallNotificationUseCase,
            ShowVideoCallNotificationUseCase showVideoCallNotificationUseCase,
            RemoveCallNotificationUseCase removeCallNotificationUseCase,
            GliaLoadHistoryUseCase gliaLoadHistoryUseCase,
            GliaQueueForChatEngagementUseCase queueForChatEngagementUseCase,
            GliaOnEngagementUseCase onEngagementUseCase,
            GliaOnEngagementEndUseCase onEngagementEndUseCase,
            GliaOnMessageUseCase gliaOnMessageUseCase,
            GliaOnOperatorTypingUseCase gliaOnOperatorTypingUseCase,
            GliaSendMessagePreviewUseCase gliaSendMessagePreviewUseCase,
            GliaSendMessageUseCase gliaSendMessageUseCase,
            AddOperatorMediaStateListenerUseCase addOperatorMediaStateListenerUseCase,
            GliaCancelQueueTicketUseCase cancelQueueTicketUseCase,
            GliaEndEngagementUseCase endEngagementUseCase,
            AddFileToAttachmentAndUploadUseCase addFileToAttachmentAndUploadUseCase,
            AddFileAttachmentsObserverUseCase addFileAttachmentsObserverUseCase,
            RemoveFileAttachmentObserverUseCase removeFileAttachmentObserverUseCase,
            GetFileAttachmentsUseCase getFileAttachmentsUseCase,
            RemoveFileAttachmentUseCase removeFileAttachmentUseCase,
            SupportedFileCountCheckUseCase supportedFileCountCheckUseCase,
            IsShowSendButtonUseCase isShowSendButtonUseCase,
            IsShowOverlayPermissionRequestDialogUseCase isShowOverlayPermissionRequestDialogUseCase,
            DownloadFileUseCase downloadFileUseCase,
            IsEnableChatEditTextUseCase isEnableChatEditTextUseCase,
            SiteInfoUseCase siteInfoUseCase,
            GliaSurveyUseCase surveyUseCase,
            GetEngagementStateFlowableUseCase getGliaEngagementStateFlowableUseCase,
            IsFromCallScreenUseCase isFromCallScreenUseCase, UpdateFromCallScreenUseCase updateFromCallScreenUseCase) {
        this.isFromCallScreenUseCase = isFromCallScreenUseCase;
        this.updateFromCallScreenUseCase = updateFromCallScreenUseCase;
        Logger.d(TAG, "constructor");

        // viewCallback is accessed from multiple threads
        // and must be protected from race condition
        synchronized (this) {
            this.viewCallback = chatViewCallback;
        }

        this.chatState = new ChatState.Builder()
                .setHistoryLoaded(false)
                .setOperatorName(null)
                .setCompanyName(null)
                .setQueueId(null)
                .setVisitorContextAssetId(null)
                .setIsVisible(false)
                .setIntegratorChatStarted(false)
                .setChatItems(new ArrayList<>())
                .setLastTypedText(EMPTY_MESSAGE)
                .setChatInputMode(ChatInputMode.ENABLED_NO_ENGAGEMENT)
                .setIsAttachmentButtonNeeded(false)
                .setIsAttachmentAllowed(true)
                .setIsChatInBottom(true)
                .setMessagesNotSeen(0)
                .setPendingNavigationType(null)
                .setUnsentMessages(new ArrayList<>())
                .setIsOperatorTyping(false)
                .createChatState();
        this.mediaUpgradeOfferRepository = mediaUpgradeOfferRepository;
        this.callTimer = sharedTimer;
        this.minimizeHandler = minimizeHandler;
        this.dialogController = dialogController;
        this.messagesNotSeenHandler = messagesNotSeenHandler;

        this.showAudioCallNotificationUseCase = showAudioCallNotificationUseCase;
        this.showVideoCallNotificationUseCase = showVideoCallNotificationUseCase;
        this.removeCallNotificationUseCase = removeCallNotificationUseCase;
        this.loadHistoryUseCase = gliaLoadHistoryUseCase;
        this.getEngagementUseCase = onEngagementUseCase;
        this.engagementEndUseCase = onEngagementEndUseCase;
        this.queueForChatEngagementUseCase = queueForChatEngagementUseCase;
        this.onMessageUseCase = gliaOnMessageUseCase;
        this.onOperatorTypingUseCase = gliaOnOperatorTypingUseCase;
        this.sendMessagePreviewUseCase = gliaSendMessagePreviewUseCase;
        this.sendMessageUseCase = gliaSendMessageUseCase;
        this.addOperatorMediaStateListenerUseCase = addOperatorMediaStateListenerUseCase;
        this.cancelQueueTicketUseCase = cancelQueueTicketUseCase;
        this.endEngagementUseCase = endEngagementUseCase;
        this.addFileAttachmentsObserverUseCase = addFileAttachmentsObserverUseCase;
        this.addFileToAttachmentAndUploadUseCase = addFileToAttachmentAndUploadUseCase;
        this.removeFileAttachmentObserverUseCase = removeFileAttachmentObserverUseCase;
        this.getFileAttachmentsUseCase = getFileAttachmentsUseCase;
        this.removeFileAttachmentUseCase = removeFileAttachmentUseCase;
        this.supportedFileCountCheckUseCase = supportedFileCountCheckUseCase;
        this.isShowSendButtonUseCase = isShowSendButtonUseCase;
        this.isShowOverlayPermissionRequestDialogUseCase = isShowOverlayPermissionRequestDialogUseCase;
        this.downloadFileUseCase = downloadFileUseCase;
        this.isEnableChatEditTextUseCase = isEnableChatEditTextUseCase;
        this.siteInfoUseCase = siteInfoUseCase;
        this.surveyUseCase = surveyUseCase;
        this.getGliaEngagementStateFlowableUseCase = getGliaEngagementStateFlowableUseCase;
    }

    public void setPhotoCaptureFileUri(Uri photoCaptureFileUri) {
        this.photoCaptureFileUri = photoCaptureFileUri;
    }

    public Uri getPhotoCaptureFileUri() {
        return this.photoCaptureFileUri;
    }

    private final Observer fileAttachmentObserver = new Observer() {
        @Override
        public void update(Observable observable, Object o) {
            if (viewCallback != null) {
                viewCallback.emitUploadAttachments(getFileAttachmentsUseCase.execute());
                emitViewState(
                        chatState
                                .setShowSendButton(isShowSendButtonUseCase.execute(chatState.lastTypedText))
                                .setIsAttachmentButtonEnabled(supportedFileCountCheckUseCase.execute())
                );
            }
        }
    };

    private volatile ChatState chatState;

    public void initChat(String companyName,
                         String queueId,
                         String visitorContextAssetId
    ) {
        if (isShowOverlayPermissionRequestDialogUseCase.execute())
            dialogController.showOverlayPermissionsDialog();
        if (chatState.integratorChatStarted || dialogController.isShowingChatEnderDialog()) {
            return;
        }
        emitViewState(chatState.initChat(companyName, queueId, visitorContextAssetId));
        loadChatHistory();
        addFileAttachmentsObserverUseCase.execute(fileAttachmentObserver);
        initMediaUpgradeCallback();
        mediaUpgradeOfferRepository.addCallback(mediaUpgradeOfferRepositoryCallback);
        minimizeHandler.addListener(this::minimizeView);
        createNewTimerCallback();
        callTimer.addFormattedValueListener(timerStatusListener);
        updateAllowFileSendState();
    }

    private void queueForEngagement() {
        Logger.d(TAG, "queueForEngagement");
        disposable.add(
                queueForChatEngagementUseCase
                        .execute(
                                chatState.queueId,
                                chatState.visitorContextAssetId
                        )
                        .subscribe(
                                this::queueForEngagementStarted,
                                this::queueForEngagementError
                        )
        );
    }

    private synchronized void emitViewState(ChatState state) {
        if (setState(state) && viewCallback != null) {
            Logger.d(TAG, "Emit state:\n" + state.toString());
            viewCallback.emitState(chatState);
        }
    }

    private synchronized void emitChatItems(ChatState state) {
        if (setState(state) && viewCallback != null) {
            Logger.d(TAG, "Emit chat items:\n" + state.chatItems.toString() +
                    "\n(State): " + state);
            viewCallback.emitItems(state.chatItems);
            viewCallback.emitUploadAttachments(getFileAttachmentsUseCase.execute());
        }
    }

    public void onDestroy(boolean retain) {
        Logger.d(TAG, "onDestroy, retain:" + retain);
        destroyView();

        // viewCallback is accessed from multiple threads
        // and must be protected from race condition
        synchronized (this) {
            viewCallback = null;
        }

        if (!retain) {
            disposable.dispose();
            mediaUpgradeOfferRepository.stopAll();
            mediaUpgradeOfferRepositoryCallback = null;
            timerStatusListener = null;
            callTimer.clear();
            minimizeHandler.clear();

            getEngagementUseCase.unregisterListener(this);
            engagementEndUseCase.unregisterListener(this);

            onMessageUseCase.unregisterListener();
            onOperatorTypingUseCase.unregisterListener();
            removeFileAttachmentObserverUseCase.execute(fileAttachmentObserver);
        }
    }

    public void onPause() {
        isChatViewPaused = true;
        messagesNotSeenHandler.onChatWentBackground();
        surveyUseCase.unregisterListener(this);
    }

    public void onMessageTextChanged(String message) {
        emitViewState(
                chatState
                        .setLastTypedText(message)
                        .setShowSendButton(isShowSendButtonUseCase.execute(message))
        );
        sendMessagePreview(message);
    }

    public void sendMessage(String message) {
        Logger.d(TAG, "Send MESSAGE: " + message);
        clearMessagePreview();
        sendMessageUseCase.execute(message, sendMessageCallback);
    }

    private void sendMessagePreview(String message) {
        if (chatState.isOperatorOnline()) {
            sendMessagePreviewUseCase.execute(message);
        }
    }

    private void clearMessagePreview() {
        // Empty string has to be sent to clear the message preview.
        sendMessagePreview(EMPTY_MESSAGE);
    }

    private void subscribeToMessages() {
        disposable.add(onMessageUseCase.execute().doOnNext(this::onMessage).subscribe());
    }

    private void onMessage(@NonNull ChatMessageInternal messageInternal) {
        ChatMessage message = messageInternal.getChatMessage();
        boolean isUnsentMessage = !chatState.unsentMessages.isEmpty() && chatState.unsentMessages.get(0).getMessage().equals(message.getContent());
        Logger.d(TAG, "onMessage: " + message.getContent() + ", id: " + message.getId() + ", isUnsentMessage: " + isUnsentMessage);
        if (isUnsentMessage) {
            List<VisitorMessageItem> unsentMessages = new ArrayList<>(chatState.unsentMessages);
            VisitorMessageItem currentMessage = unsentMessages.get(0);
            unsentMessages.remove(currentMessage);

            List<ChatItem> currentChatItems = new ArrayList<>(chatState.chatItems);
            int currentMessageIndex = currentChatItems.indexOf(currentMessage);
            currentChatItems.remove(currentMessage);
            currentChatItems.add(currentMessageIndex, new VisitorMessageItem(message.getId(), false, message.getContent()));

            // emitting state because no need to change recyclerview items here
            emitViewState(chatState
                    .changeItems(currentChatItems)
                    .changeUnsentMessages(unsentMessages)
            );
            if (!chatState.unsentMessages.isEmpty()) {
                sendMessageUseCase.execute(chatState.unsentMessages.get(0).getMessage(), sendMessageCallback);
            }
            return;
        }

        List<ChatItem> items = new ArrayList<>(chatState.chatItems);
        appendMessageItem(items, messageInternal);
        emitChatItems(chatState.changeItems(items));
    }

    private void onMessageSent(VisitorMessage message) {
        if (message != null) {
            Logger.d(TAG, "messageSent: " + message + ", id: " + message.getId());
            List<ChatItem> currentChatItems = new ArrayList<>(chatState.chatItems);
            changeDeliveredIndex(currentChatItems, message);

            // chat input mode has to be set to enabled after message is sent
            if (isEnableChatEditTextUseCase.execute(currentChatItems, chatState.chatInputMode)) {
                emitViewState(chatState.chatInputModeChanged(ChatInputMode.ENABLED));
            }
            emitChatItems(chatState.changeItems(currentChatItems));
        }
    }

    private void onMessageSendError(GliaException ex) {
        if (ex != null) {
            Logger.d(TAG, "messageSent exception");
            error(ex);
        }
    }

    private void onSendMessageOperatorOffline(String message) {
        appendUnsentMessage(message);
        if (!chatState.engagementRequested) {
            queueForEngagement();
        }
    }

    private void appendUnsentMessage(String message) {
        Logger.d(TAG, "appendUnsentMessage: " + message);
        List<VisitorMessageItem> unsentMessages = new ArrayList<>(chatState.unsentMessages);
        VisitorMessageItem unsentItem = new VisitorMessageItem(VisitorMessageItem.UNSENT_MESSAGE_ID, false, message);
        unsentMessages.add(unsentItem);

        List<ChatItem> currentChatItems = new ArrayList<>(chatState.chatItems);
        currentChatItems.add(unsentItem);

        emitViewState(chatState.changeUnsentMessages(unsentMessages));
        emitChatItems(chatState.changeItems(currentChatItems));
    }

    private void onOperatorTyping(boolean isOperatorTyping) {
        emitViewState(chatState.setIsOperatorTyping(isOperatorTyping));
    }

    public void show() {
        Logger.d(TAG, "show");
        if (!chatState.isVisible) {
            emitViewState(chatState.changeVisibility(true));
        }
    }

    public void onBackArrowClicked() {
        Logger.d(TAG, "onBackArrowClicked");
        emitViewState(chatState.changeVisibility(false));
        messagesNotSeenHandler.chatOnBackClicked();
    }

    public void onBackArrowClicked(@Nullable ChatView.OnBackClickedListener onBackClickedListener) {
        if (isFromCallScreenUseCase.isFromCallScreen()) {
            if (viewCallback != null) {
                viewCallback.backToCall();
            }
        } else {
            if (onBackClickedListener!= null) {
                onBackClickedListener.onBackClicked();
            }
        }

        updateFromCallScreenUseCase.updateFromCallScreen(false);
    }

    public void noMoreOperatorsAvailableDismissed() {
        Logger.d(TAG, "noMoreOperatorsAvailableDismissed");
        stop();
        dialogController.dismissCurrentDialog();
    }

    public void unexpectedErrorDialogDismissed() {
        Logger.d(TAG, "unexpectedErrorDialogDismissed");
        stop();
        dialogController.dismissCurrentDialog();
    }

    public void endEngagementDialogYesClicked() {
        Logger.d(TAG, "endEngagementDialogYesClicked");
        isVisitorEndEngagement = true;
        stop();
        dialogController.dismissDialogs();
    }

    public void endEngagementDialogDismissed() {
        Logger.d(TAG, "endEngagementDialogDismissed");
        dialogController.dismissCurrentDialog();
    }

    public void leaveChatClicked() {
        Logger.d(TAG, "leaveChatClicked");
        if (chatState.isOperatorOnline())
            dialogController.showExitChatDialog(chatState.getFormattedOperatorName());
    }

    public void leaveChatQueueClicked() {
        Logger.d(TAG, "leaveChatQueueClicked");
        dialogController.showExitQueueDialog();
    }

    public boolean isChatVisible() {
        return chatState.isVisible;
    }

    // viewCallback is accessed from multiple threads
    // and must be protected from race condition
    public synchronized void setViewCallback(ChatViewCallback chatViewCallback) {
        Logger.d(TAG, "setViewCallback");
        this.viewCallback = chatViewCallback;
        viewCallback.emitState(chatState);
        viewCallback.emitItems(chatState.chatItems);
        viewCallback.emitUploadAttachments(getFileAttachmentsUseCase.execute());

        // always start in bottom
        emitViewState(chatState
                .isInBottomChanged(true)
                .changeVisibility(true)
        );

        viewCallback.scrollToBottomImmediate();

        if (chatState.pendingNavigationType != null) {
            viewCallback.navigateToCall(chatState.pendingNavigationType);
        }
    }

    public void onResume() {
        isChatViewPaused = false;
        Logger.d(TAG, "onResume\n");
        messagesNotSeenHandler.callChatButtonClicked();
        surveyUseCase.registerListener(this);
        subscribeToEngagementStateChange();
    }

    private void subscribeToEngagementStateChange() {
        if (engagementStateEventDisposable != null) engagementStateEventDisposable.dispose();
        engagementStateEventDisposable = getGliaEngagementStateFlowableUseCase
                .execute()
                .subscribe(
                        this::onEngagementStateChanged,
                        throwable -> Logger.e(TAG, throwable.getMessage())
                );
        disposable.add(engagementStateEventDisposable);
    }

    private void onEngagementStateChanged(EngagementStateEvent engagementState) {
        EngagementStateEventVisitor<Operator> visitor = new EngagementStateEventVisitor.OperatorVisitor();
        switch (engagementState.getType()) {
            case ENGAGEMENT_OPERATOR_CHANGED:
                onOperatorChanged(visitor.visit(engagementState));
                break;
            case ENGAGEMENT_OPERATOR_CONNECTED:
                onOperatorConnected(visitor.visit(engagementState));
                break;
            case ENGAGEMENT_TRANSFERRING:
                onTransferring();
                break;
            case ENGAGEMENT_ONGOING:
                onEngagementOngoing(visitor.visit(engagementState));
                break;
            case ENGAGEMENT_ENDED:
                break;
        }
    }

    private void onEngagementOngoing(Operator operator) {
        String name = operator.getName();
        String imageUrl = Utils.getOperatorImageUrl(operator);
        emitViewState(chatState.operatorConnected(name, imageUrl));
    }

    private void onOperatorConnected(Operator operator) {
        String name = operator.getName();
        String imageUrl = Utils.getOperatorImageUrl(operator);
        operatorConnected(name, imageUrl);
    }

    private void onOperatorChanged(Operator operator) {
        String name = operator.getName();
        String imageUrl = Utils.getOperatorImageUrl(operator);
        operatorChanged(name, imageUrl);
    }

    private void onTransferring() {
        List<ChatItem> items = new ArrayList<>(chatState.chatItems);
        if (chatState.operatorStatusItem != null) {
            items.remove(chatState.operatorStatusItem);
        }
        items.add(OperatorStatusItem.TransferringStatusItem());
        emitViewState(chatState.transferring());
        emitChatItems(chatState.changeItems(items));
    }

    public void overlayPermissionsDialogDismissed() {
        Logger.d(TAG, "overlayPermissionsDialogDismissed");
        dialogController.dismissCurrentDialog();
        emitViewState(chatState);
    }

    public void acceptUpgradeOfferClicked(MediaUpgradeOffer offer) {
        Logger.d(TAG, "upgradeToAudioClicked");
        messagesNotSeenHandler.chatUpgradeOfferAccepted();
        mediaUpgradeOfferRepository.acceptOffer(offer, MediaUpgradeOfferRepository.Submitter.CHAT);
        dialogController.dismissCurrentDialog();
    }

    public void declineUpgradeOfferClicked(MediaUpgradeOffer offer) {
        Logger.d(TAG, "closeUpgradeDialogClicked");
        mediaUpgradeOfferRepository.declineOffer(offer, MediaUpgradeOfferRepository.Submitter.CHAT);
        dialogController.dismissCurrentDialog();
    }

    public void navigateToCallSuccess() {
        Logger.d(TAG, "navigateToCallSuccess");
        emitViewState(chatState.setPendingNavigationType(null));
    }

    private synchronized boolean setState(ChatState state) {
        if (this.chatState.equals(state)) return false;
        this.chatState = state;
        return true;
    }

    private void error(String error) {
        Logger.e(TAG, error);
        dialogController.showUnexpectedErrorDialog();
        emitViewState(chatState.stop());
    }

    private void onOperatorMediaStateVideo() {
        Logger.d(TAG, "newOperatorMediaState: video");
        showVideoCallNotificationUseCase.execute();
    }

    private void onOperatorMediaStateAudio() {
        Logger.d(TAG, "newOperatorMediaState: audio");
        showAudioCallNotificationUseCase.execute();
    }

    private void onOperatorMediaStateUnknown() {
        Logger.d(TAG, "newOperatorMediaState: null");
        removeCallNotificationUseCase.execute();
    }

    private void initMediaUpgradeCallback() {
        mediaUpgradeOfferRepositoryCallback = new MediaUpgradeOfferRepositoryCallback() {
            @Override
            public void newOffer(MediaUpgradeOffer offer) {
                if (isChatViewPaused) return;

                if (offer.video == MediaDirection.NONE && offer.audio == MediaDirection.TWO_WAY) {
                    // audio call
                    Logger.d(TAG, "audioUpgradeRequested");
                    if (chatState.isOperatorOnline())
                        dialogController.showUpgradeAudioDialog(offer, chatState.getFormattedOperatorName());
                } else if (offer.video == MediaDirection.TWO_WAY) {
                    // video call
                    Logger.d(TAG, "2 way videoUpgradeRequested");
                    if (chatState.isOperatorOnline())
                        dialogController.showUpgradeVideoDialog2Way(offer, chatState.getFormattedOperatorName());
                } else if (offer.video == MediaDirection.ONE_WAY) {
                    Logger.d(TAG, "1 way videoUpgradeRequested");
                    if (chatState.isOperatorOnline())
                        dialogController.showUpgradeVideoDialog1Way(offer, chatState.getFormattedOperatorName());
                }
            }

            @Override
            public void upgradeOfferChoiceSubmitSuccess(
                    MediaUpgradeOffer offer,
                    MediaUpgradeOfferRepository.Submitter submitter
            ) {
                Logger.d(TAG, "upgradeOfferChoiceSubmitSuccess");
                if (submitter == MediaUpgradeOfferRepository.Submitter.CHAT) {
                    String requestedMediaType;
                    if (offer.video != null && offer.video != MediaDirection.NONE) {
                        requestedMediaType = GliaWidgets.MEDIA_TYPE_VIDEO;
                    } else {
                        requestedMediaType = GliaWidgets.MEDIA_TYPE_AUDIO;
                    }
                    emitViewState(chatState.setPendingNavigationType(requestedMediaType));
                    if (viewCallback != null) {
                        viewCallback.navigateToCall(requestedMediaType);
                        Logger.d(TAG, "navigateToCall");
                    }
                }
            }

            @Override
            public void upgradeOfferChoiceDeclinedSuccess(
                    MediaUpgradeOfferRepository.Submitter submitter
            ) {
                Logger.d(TAG, "upgradeOfferChoiceDeclinedSuccess");
            }
        };
    }

    private void viewInitQueueing() {
        Logger.d(TAG, "viewInitQueueing");
        List<ChatItem> items = new ArrayList<>(chatState.chatItems);
        if (chatState.operatorStatusItem != null) {
            items.remove(chatState.operatorStatusItem);
        }
        OperatorStatusItem operatorStatusItem =
                OperatorStatusItem.QueueingStatusItem(chatState.companyName);
        items.add(operatorStatusItem);
        emitViewState(chatState.queueingStarted(operatorStatusItem));
        emitChatItems(chatState.changeItems(items));
    }

    private void destroyView() {
        if (viewCallback != null) {
            Logger.d(TAG, "destroyingView");
            viewCallback.destroyView();
        }
    }

    private void minimizeView() {
        if (viewCallback != null) viewCallback.minimizeView();
    }

    private void operatorConnected(String operatorName, String profileImgUrl) {
        List<ChatItem> items = new ArrayList<>(chatState.chatItems);
        if (chatState.operatorStatusItem != null) {
            // remove previous operator status item
            int operatorStatusItemIndex = items.indexOf(chatState.operatorStatusItem);
            Logger.d(TAG, "operatorStatusItemIndex: " + operatorStatusItemIndex + ", size: " + items.size());
            items.remove(chatState.operatorStatusItem);
            items.add(
                    operatorStatusItemIndex,
                    OperatorStatusItem.OperatorFoundStatusItem(
                            chatState.companyName,
                            Utils.formatOperatorName(operatorName),
                            profileImgUrl
                    )
            );
        } else {
            items.add(
                    OperatorStatusItem.OperatorFoundStatusItem(
                            chatState.companyName,
                            Utils.formatOperatorName(operatorName),
                            profileImgUrl
                    )
            );
        }
        emitViewState(chatState.operatorConnected(operatorName, profileImgUrl));
        emitChatItems(chatState.changeItems(items));
    }

    private void operatorChanged(String operatorName, String profileImgUrl) {
        List<ChatItem> items = new ArrayList<>(chatState.chatItems);
        OperatorStatusItem operatorStatusItem =
                OperatorStatusItem.OperatorJoinedStatusItem(
                        chatState.companyName,
                        Utils.formatOperatorName(operatorName),
                        profileImgUrl
                );

        items.add(operatorStatusItem);
        emitChatItems(chatState.changeItems(items));
        emitViewState(chatState.operatorConnected(operatorName, profileImgUrl));
    }

    private void stop() {
        Logger.d(TAG, "Stop, engagement ended");
        disposable.add(
                cancelQueueTicketUseCase.execute()
                        .subscribe(
                                this::queueForEngagementStopped,
                                throwable -> Logger.e(TAG, "cancelQueueTicketUseCase error: " + throwable.getMessage())
                        )
        );
        endEngagementUseCase.execute();
        mediaUpgradeOfferRepository.stopAll();
        emitViewState(chatState.stop());
    }

    private void appendHistoryChatItem(List<ChatItem> currentChatItems, ChatMessageInternal chatMessageInternal) {
        ChatMessage message = chatMessageInternal.getChatMessage();
        if (message.getSenderType() == Chat.Participant.VISITOR) {
            appendHistoryMessage(currentChatItems, message);
            addVisitorAttachmentItemsToChatItems(currentChatItems, message);
        } else if (message.getSenderType() == Chat.Participant.OPERATOR) {
            appendOperatorMessage(currentChatItems, chatMessageInternal);
        }
    }

    private void appendHistoryMessage(List<ChatItem> currentChatItems, ChatMessage message) {
        MessageAttachment attachment = message.getAttachment();
        if (attachment instanceof SingleChoiceAttachment &&
                ((SingleChoiceAttachment) attachment).getSelectedOption() != null) {
            Logger.d(TAG, "Not adding singleChoiceAnswer");
            return;
        }

        if (message.getContent() != null && !message.getContent().isEmpty()) {
            currentChatItems.add(new VisitorMessageItem(VisitorMessageItem.HISTORY_ID, false, message.getContent()));
        }
    }

    private void appendMessageItem(List<ChatItem> currentChatItems, ChatMessageInternal messageInternal) {
        ChatMessage message = messageInternal.getChatMessage();
        if (message.getSenderType() == Chat.Participant.VISITOR) {
            appendSentMessage(currentChatItems, message);
            addVisitorAttachmentItemsToChatItems(currentChatItems, message);
        } else if (message.getSenderType() == Chat.Participant.OPERATOR) {
            onOperatorMessageReceived(currentChatItems, messageInternal);
        }
    }

    private void onOperatorMessageReceived(List<ChatItem> currentChatItems, ChatMessageInternal messageInternal) {
        appendOperatorMessage(currentChatItems, messageInternal);
        appendMessagesNotSeen();
        changeChatInputMode(messageInternal.getChatMessage());
    }

    private void changeChatInputMode(ChatMessage message) {
        ChatInputMode newInputMode = getChatInputMode(message);
        if (chatState.chatInputMode != newInputMode) {
            emitViewState(chatState.chatInputModeChanged(newInputMode));
        }
    }

    private ChatInputMode getChatInputMode(ChatMessage message) {
        if (message.getAttachment() instanceof SingleChoiceAttachment) {
            SingleChoiceAttachment attachment = ((SingleChoiceAttachment) message.getAttachment());
            if (attachment.getOptions() != null) {
                return ChatInputMode.SINGLE_CHOICE_CARD;
            }
        }
        return ChatInputMode.ENABLED;
    }

    private void addVisitorAttachmentItemsToChatItems(List<ChatItem> currentChatItems, ChatMessage chatMessage) {
        MessageAttachment attachment = chatMessage.getAttachment();
        if (attachment instanceof FilesAttachment) {
            FilesAttachment filesAttachment = (FilesAttachment) attachment;
            AttachmentFile[] files = filesAttachment.getFiles();
            for (AttachmentFile file : files) {
                int type;
                if (file.getContentType().startsWith("image")) {
                    type = ChatAdapter.VISITOR_IMAGE_VIEW_TYPE;
                } else {
                    type = ChatAdapter.VISITOR_FILE_VIEW_TYPE;
                }
                currentChatItems.add(
                        new VisitorAttachmentItem(chatMessage.getId(), type, file, false, false, false)
                );
            }
        }
    }

    private void appendSentMessage(List<ChatItem> items, ChatMessage message) {
        // do nothing if single choice response
        MessageAttachment attachment = message.getAttachment();
        if (attachment instanceof SingleChoiceAttachment &&
                ((SingleChoiceAttachment) attachment).getSelectedOption() != null) {
            Logger.d(TAG, "Not adding singleChoiceAnswer");
            return;
        }

        if (message.getContent() != null && !message.getContent().isEmpty()) {
            items.add(new VisitorMessageItem(message.getId(), false, message.getContent()));
        }
    }

    private void appendMessagesNotSeen() {
        emitViewState(chatState.messagesNotSeenChanged(
                chatState.isChatInBottom ?
                        0 :
                        chatState.messagesNotSeen + 1));
    }

    private void initGliaEngagementObserving() {
        getEngagementUseCase.execute(this);
        engagementEndUseCase.execute(this);
    }

    private void changeDeliveredIndex(List<ChatItem> currentChatItems, VisitorMessage message) {
        // "Delivered" status only applies to visitor messages
        if (message.getSender() != Chat.Participant.VISITOR) return;
        String messageId = message.getId();
        boolean foundDelivered = false;
        for (int i = currentChatItems.size() - 1; i >= 0; i--) {
            ChatItem currentChatItem = currentChatItems.get(i);
            if (currentChatItem instanceof VisitorMessageItem) {
                VisitorMessageItem item = (VisitorMessageItem) currentChatItem;
                String itemId = item.getId();
                if (itemId.equals(VisitorMessageItem.HISTORY_ID)) {
                    // we reached the history items no point in going searching further
                    break;
                } else if (!foundDelivered && itemId.equals(messageId)) {
                    foundDelivered = true;
                    currentChatItems.set(i, new VisitorMessageItem(itemId, true, item.getMessage()));
                } else if (item.isShowDelivered()) {
                    currentChatItems.set(i, new VisitorMessageItem(itemId, false, item.getMessage()));
                }
            } else if (currentChatItem instanceof VisitorAttachmentItem) {
                VisitorAttachmentItem item = (VisitorAttachmentItem) currentChatItem;
                if (!foundDelivered && item.getId().equals(messageId)) {
                    foundDelivered = true;
                    setDelivered(currentChatItems, i, item, true);
                } else if (item.showDelivered) {
                    setDelivered(currentChatItems, i, item, false);
                }
            }
        }
    }

    private void setDelivered(List<ChatItem> currentChatItems, int i, VisitorAttachmentItem item, boolean delivered) {
        currentChatItems.set(
                i,
                new VisitorAttachmentItem(
                        item.getId(),
                        item.getViewType(),
                        item.attachmentFile,
                        item.isFileExists,
                        item.isDownloading,
                        delivered
                )
        );
    }

    private void appendOperatorMessage(List<ChatItem> currentChatItems, ChatMessageInternal chatMessageInternal) {
        setLastOperatorItemChatHeadVisibility(currentChatItems, isOperatorChanged(currentChatItems, chatMessageInternal));
        appendOperatorMessageItem(currentChatItems, chatMessageInternal);
        appendOperatorAttachmentItems(currentChatItems, chatMessageInternal);
        setLastOperatorItemChatHeadVisibility(currentChatItems, true);
    }

    private boolean isOperatorChanged(List<ChatItem> currentChatItems, ChatMessageInternal chatMessageInternal) {
        if (currentChatItems.isEmpty()) return false;
        ChatItem lastItem = currentChatItems.get(currentChatItems.size() - 1);

        if (lastItem instanceof OperatorChatItem) {
            OperatorChatItem operatorMessageItem = (OperatorChatItem) lastItem;

            return !chatMessageInternal
                    .getOperatorId()
                    .filter(id -> Objects.equals(id, operatorMessageItem.operatorId))
                    .isPresent();
        }

        return false;
    }

    private void setLastOperatorItemChatHeadVisibility(List<ChatItem> currentChatItems, boolean showChatHead) {
        if (!currentChatItems.isEmpty()) {
            ChatItem lastItem = currentChatItems.get(currentChatItems.size() - 1);
            if (lastItem instanceof OperatorMessageItem) {
                OperatorMessageItem lastItemInView = (OperatorMessageItem) lastItem;
                currentChatItems.remove(lastItemInView);
                currentChatItems.add(
                        new OperatorMessageItem(
                                lastItemInView.getId(),
                                lastItemInView.operatorName,
                                lastItemInView.operatorProfileImgUrl,
                                showChatHead,
                                lastItemInView.content,
                                lastItemInView.singleChoiceOptions,
                                lastItemInView.selectedChoiceIndex,
                                lastItemInView.choiceCardImageUrl,
                                lastItemInView.operatorId
                        )
                );
            } else if (lastItem instanceof OperatorAttachmentItem) {
                OperatorAttachmentItem lastItemInView = (OperatorAttachmentItem) lastItem;
                currentChatItems.remove(lastItemInView);
                currentChatItems.add(
                        new OperatorAttachmentItem(
                                lastItemInView.getId(),
                                lastItemInView.getViewType(),
                                showChatHead,
                                lastItemInView.attachmentFile,
                                lastItemInView.operatorProfileImgUrl,
                                false,
                                false,
                                lastItemInView.operatorId
                        )
                );
            }
        }
    }

    private void appendOperatorAttachmentItems(List<ChatItem> currentChatItems, ChatMessageInternal messageInternal) {
        ChatMessage message = messageInternal.getChatMessage();
        MessageAttachment attachment = message.getAttachment();
        if (attachment instanceof FilesAttachment) {
            FilesAttachment filesAttachment = (FilesAttachment) attachment;
            AttachmentFile[] files = filesAttachment.getFiles();

            for (AttachmentFile file : files) {
                int viewType;
                if (file.getContentType().startsWith("image")) {
                    viewType = ChatAdapter.OPERATOR_IMAGE_VIEW_TYPE;
                } else {
                    viewType = ChatAdapter.OPERATOR_FILE_VIEW_TYPE;
                }
                currentChatItems.add(
                        new OperatorAttachmentItem(
                                message.getId(),
                                viewType,
                                false,
                                file,
                                messageInternal.getOperatorImageUrl().orElse(chatState.operatorProfileImgUrl),
                                false,
                                false,
                                messageInternal.getOperatorId().orElse(UUID.randomUUID().toString())
                        )
                );
            }
        }
    }

    private void appendOperatorMessageItem(List<ChatItem> currentChatItems, ChatMessageInternal messageInternal) {
        ChatMessage message = messageInternal.getChatMessage();
        if (!message.getContent().equals(EMPTY_MESSAGE)) {
            MessageAttachment messageAttachment = message.getAttachment();
            currentChatItems.add(
                    new OperatorMessageItem(
                            message.getId(),
                            messageInternal.getOperatorName().orElse(chatState.operatorName),
                            messageInternal.getOperatorImageUrl().orElse(chatState.operatorProfileImgUrl),
                            false,
                            message.getContent(),
                            getSingleChoiceAttachmentOptions(messageAttachment),
                            null,
                            getSingleChoiceAttachmentImgUrl(messageAttachment),
                            messageInternal.getOperatorId().orElse(UUID.randomUUID().toString())
                    )
            );
        }
    }

    private String getSingleChoiceAttachmentImgUrl(MessageAttachment attachment) {
        String imageUrl = null;

        if (attachment instanceof SingleChoiceAttachment) {
            Optional<String> optionalImageUrl = ((SingleChoiceAttachment) attachment).getImageUrl();

            if (optionalImageUrl.isPresent()) {
                imageUrl = optionalImageUrl.get();
            }
        }

        return imageUrl;
    }

    private List<SingleChoiceOption> getSingleChoiceAttachmentOptions(MessageAttachment attachment) {
        List<SingleChoiceOption> singleChoiceOptions = null;

        if (attachment instanceof SingleChoiceAttachment) {
            SingleChoiceAttachment singleChoiceAttachment = (SingleChoiceAttachment) attachment;
            singleChoiceOptions =
                    Arrays.stream(singleChoiceAttachment.getOptions())
                            .collect(Collectors.toList());
        }

        return singleChoiceOptions;
    }

    private void startTimer() {
        Logger.d(TAG, "startTimer");
        callTimer.startNew(Constants.CALL_TIMER_DELAY, Constants.CALL_TIMER_INTERVAL_VALUE);
    }

    private void upgradeMediaItem() {
        Logger.d(TAG, "upgradeMediaItem");
        List<ChatItem> newItems = new ArrayList<>(chatState.chatItems);
        MediaUpgradeStartedTimerItem mediaUpgradeStartedTimerItem =
                new MediaUpgradeStartedTimerItem(MediaUpgradeStartedTimerItem.Type.VIDEO, chatState.mediaUpgradeStartedTimerItem.time);
        newItems.remove(chatState.mediaUpgradeStartedTimerItem);
        newItems.add(mediaUpgradeStartedTimerItem);
        emitChatItems(chatState.changeTimerItem(newItems, mediaUpgradeStartedTimerItem));
    }

    private void createNewTimerCallback() {
        if (timerStatusListener != null) {
            callTimer.removeFormattedValueListener(timerStatusListener);
        }
        timerStatusListener = new TimeCounter.FormattedTimerStatusListener() {
            @Override
            public void onNewFormattedTimerValue(String formatedValue) {
                if (chatState.isMediaUpgradeStarted()) {
                    int index = chatState.chatItems.indexOf(chatState.mediaUpgradeStartedTimerItem);
                    if (index != -1) {
                        List<ChatItem> newItems = new ArrayList<>(chatState.chatItems);
                        MediaUpgradeStartedTimerItem.Type type = chatState.mediaUpgradeStartedTimerItem.type;
                        newItems.remove(index);
                        MediaUpgradeStartedTimerItem mediaUpgradeStartedTimerItem =
                                new MediaUpgradeStartedTimerItem(type, formatedValue);
                        newItems.add(index, mediaUpgradeStartedTimerItem);
                        emitChatItems(chatState.changeTimerItem(newItems, mediaUpgradeStartedTimerItem));
                    }
                }
            }

            @Override
            public void onFormattedTimerCancelled() {
                if (chatState.isMediaUpgradeStarted() &&
                        chatState.chatItems.contains(chatState.mediaUpgradeStartedTimerItem)) {
                    List<ChatItem> newItems = new ArrayList<>(chatState.chatItems);
                    newItems.remove(chatState.mediaUpgradeStartedTimerItem);
                    emitChatItems(chatState.changeTimerItem(newItems, null));
                }
            }
        };
    }

    public void singleChoiceOptionClicked(
            String id,
            int indexInList,
            int optionIndex
    ) {
        Logger.d(TAG, "singleChoiceOptionClicked, id: " + id);
        if (indexInList == RecyclerView.NO_POSITION) {
            return;
        }
        ChatItem item = chatState.chatItems.get(indexInList);
        if (item.getId().equals(id)) {
            OperatorMessageItem choiceCardItem =
                    (OperatorMessageItem) chatState.chatItems.get(indexInList);
            SingleChoiceOption selectedOption =
                    choiceCardItem.singleChoiceOptions.get(optionIndex);
            sendMessageUseCase.execute(selectedOption.asSingleChoiceResponse(), sendMessageCallback);

            OperatorMessageItem choiceCardItemWithSelected =
                    new OperatorMessageItem(
                            id,
                            choiceCardItem.operatorName,
                            choiceCardItem.operatorProfileImgUrl,
                            choiceCardItem.showChatHead,
                            choiceCardItem.content,
                            choiceCardItem.singleChoiceOptions,
                            optionIndex,
                            choiceCardItem.choiceCardImageUrl,
                            choiceCardItem.operatorId
                    );

            List<ChatItem> modifiedItems = new ArrayList<>(chatState.chatItems);
            modifiedItems.remove(indexInList);
            modifiedItems.add(indexInList, choiceCardItemWithSelected);
            emitChatItems(chatState.changeItems(modifiedItems));
        }
    }

    public void onRecyclerviewPositionChanged(boolean isBottom) {
        if (isBottom) {
            Logger.d(TAG, "onRecyclerviewPositionChanged, isBottom = true");
            emitViewState(chatState.isInBottomChanged(true).messagesNotSeenChanged(0));
        } else {
            Logger.d(TAG, "onRecyclerviewPositionChanged, isBottom = false");
            emitViewState(chatState.isInBottomChanged(false));
        }
    }

    public void newMessagesIndicatorClicked() {
        Logger.d(TAG, "newMessagesIndicatorClicked");
        if (viewCallback != null) {
            viewCallback.smoothScrollToBottom();
        }
    }

    private void loadChatHistory() {
        Disposable historyDisposable = loadHistoryUseCase.execute()
                .doOnError(this::error)
                .doOnSuccess(this::historyLoaded)
                .subscribe();

        disposable.add(historyDisposable);
    }

    private void historyLoaded(List<ChatMessageInternal> messages) {
        Logger.d(TAG, "historyLoaded");
        List<ChatItem> items = new ArrayList<>(chatState.chatItems);
        if (messages != null && !messages.isEmpty()) {
            for (ChatMessageInternal message : messages) {
                appendHistoryChatItem(items, message);
            }
            emitChatItems(chatState.historyLoaded(items));
            initGliaEngagementObserving();
        } else {
            initGliaEngagementObserving();
            queueForEngagement();
        }

    }

    private void error(Throwable error) {
        if (error != null) {
            error(error.toString());
        }
    }

    @Override
    public void newEngagementLoaded(OmnicoreEngagement engagement) {
        Logger.d(TAG, "newEngagementLoaded");
        subscribeToMessages();
        onOperatorTypingUseCase.execute(this::onOperatorTyping);
        addOperatorMediaStateListenerUseCase.execute(operatorMediaStateListener);
        mediaUpgradeOfferRepository.startListening();
        if (!chatState.unsentMessages.isEmpty()) {
            sendMessageUseCase.execute(chatState.unsentMessages.get(0).getMessage(), sendMessageCallback);
            Logger.d(TAG, "unsentMessage sent!");
        }
        emitViewState(chatState.engagementStarted());
    }

    @Override
    public void engagementEnded() {
        Logger.d(TAG, "engagementEnded");
        stop();
    }

    @Override
    public void onSurveyLoaded(@Nullable Survey survey) {
        Logger.d(TAG, "newSurveyLoaded");

        if (viewCallback != null && survey != null) {
            viewCallback.navigateToSurvey(survey);
            Dependencies.getControllerFactory().destroyControllers();
        } else if (!isVisitorEndEngagement) {
            dialogController.showEngagementEndedDialog();
        } else {
            Dependencies.getControllerFactory().destroyControllers();
        }
    }

    public void onNewOperatorMediaState(OperatorMediaState operatorMediaState) {
        Logger.d(TAG, "newOperatorMediaState: " + operatorMediaState.toString());

        if (chatState.isAudioCallStarted() && operatorMediaState.getVideo() != null) {
            upgradeMediaItem();
        } else if (!chatState.isMediaUpgradeStarted()) {
            addMediaUpgradeItemToChatItems(operatorMediaState);
            if (!callTimer.isRunning()) {
                startTimer();
            }
        }
        if (operatorMediaState.getVideo() != null) {
            onOperatorMediaStateVideo();
        } else if (operatorMediaState.getAudio() != null) {
            onOperatorMediaStateAudio();
        } else {
            onOperatorMediaStateUnknown();
        }
    }

    private void addMediaUpgradeItemToChatItems(OperatorMediaState operatorMediaState) {
        MediaUpgradeStartedTimerItem.Type type = null;
        if (operatorMediaState.getVideo() == null && operatorMediaState.getAudio() != null) {
            Logger.d(TAG, "starting audio timer");
            type = MediaUpgradeStartedTimerItem.Type.AUDIO;
        } else if (operatorMediaState.getVideo() != null) {
            Logger.d(TAG, "starting video timer");
            type = MediaUpgradeStartedTimerItem.Type.VIDEO;
        }
        List<ChatItem> newItems = new ArrayList<>(chatState.chatItems);
        MediaUpgradeStartedTimerItem mediaUpgradeStartedTimerItem =
                new MediaUpgradeStartedTimerItem(type, Utils.toMmSs(0));
        newItems.add(mediaUpgradeStartedTimerItem);
        emitChatItems(chatState.changeTimerItem(newItems, mediaUpgradeStartedTimerItem));
    }

    public void notificationsDialogDismissed() {
        dialogController.dismissCurrentDialog();
    }

    public void queueForEngagementStarted() {
        Logger.d(TAG, "queueForEngagementStarted");
        viewInitQueueing();
    }

    public void queueForEngagementStopped() {
        Logger.d(TAG, "queueForEngagementStopped");
    }

    public void queueForEngagementError(Throwable exception) {
        if (exception != null) {
            if (exception instanceof GliaException) {
                Logger.e(TAG, exception.toString());
                switch (((GliaException) exception).cause) {
                    case QUEUE_CLOSED:
                    case QUEUE_FULL:
                        dialogController.showNoMoreOperatorsAvailableDialog();
                        break;
                    default:
                        dialogController.showUnexpectedErrorDialog();
                }
                emitViewState(chatState.stop());
            } else if (exception instanceof QueueingOngoingException) {
                queueForEngagementStarted();
            }
        }
    }

    public void onRemoveAttachment(FileAttachment attachment) {
        removeFileAttachmentUseCase.execute(attachment);
    }

    public void onAttachmentReceived(FileAttachment file) {
        addFileToAttachmentAndUploadUseCase
                .execute(file, new AddFileToAttachmentAndUploadUseCase.Listener() {
                    @Override
                    public void onFinished() {
                        Logger.d(TAG, "fileUploadFinished");
                    }

                    @Override
                    public void onStarted() {
                        Logger.d(TAG, "fileUploadStarted");
                    }

                    @Override
                    public void onError(Exception ex) {
                        Logger.e(TAG, "Upload file failed: " + ex.getMessage());
                    }

                    @Override
                    public void onSecurityCheckStarted() {
                        Logger.d(TAG, "fileUploadSecurityCheckStarted");
                    }

                    @Override
                    public void onSecurityCheckFinished(EngagementFile.ScanResult scanResult) {
                        Logger.d(TAG, "fileUploadSecurityCheckFinished result=" + scanResult);
                    }
                });
    }

    public void onFileDownloadClicked(AttachmentFile attachmentFile) {
        disposable.add(
                downloadFileUseCase
                        .execute(attachmentFile)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> fileDownloadSuccess(attachmentFile),
                                error -> fileDownloadError(attachmentFile, error)
                        )
        );
    }

    private void fileDownloadError(AttachmentFile attachmentFile, Throwable error) {
        if (viewCallback != null) viewCallback.fileDownloadError(attachmentFile, error);
    }

    private void fileDownloadSuccess(AttachmentFile attachmentFile) {
        if (viewCallback != null) viewCallback.fileDownloadSuccess(attachmentFile);
    }

    private void updateAllowFileSendState() {
        siteInfoUseCase.execute((siteInfo, e) -> onSiteInfoReceived(siteInfo));
    }

    private void onSiteInfoReceived(@Nullable SiteInfo siteInfo) {
        emitViewState(chatState.allowSendAttachmentStateChanged(siteInfo == null || siteInfo.getAllowedFileSenders().isVisitorAllowed()));
    }
}
