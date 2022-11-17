package com.glia.widgets.chat.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.glia.widgets.chat.ChatViewCallback;
import com.glia.widgets.chat.domain.CustomCardAdapterTypeUseCase;
import com.glia.widgets.chat.domain.CustomCardInteractableUseCase;
import com.glia.widgets.chat.domain.CustomCardShouldShowUseCase;
import com.glia.widgets.chat.domain.CustomCardTypeUseCase;
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
import com.glia.widgets.core.dialog.DialogController;
import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase;
import com.glia.widgets.core.engagement.domain.GetEngagementStateFlowableUseCase;
import com.glia.widgets.core.engagement.domain.GliaEndEngagementUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementEndUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementUseCase;
import com.glia.widgets.core.fileupload.domain.AddFileAttachmentsObserverUseCase;
import com.glia.widgets.core.fileupload.domain.AddFileToAttachmentAndUploadUseCase;
import com.glia.widgets.core.fileupload.domain.GetFileAttachmentsUseCase;
import com.glia.widgets.core.fileupload.domain.RemoveFileAttachmentObserverUseCase;
import com.glia.widgets.core.fileupload.domain.RemoveFileAttachmentUseCase;
import com.glia.widgets.core.fileupload.domain.SupportedFileCountCheckUseCase;
import com.glia.widgets.core.mediaupgradeoffer.MediaUpgradeOfferRepository;
import com.glia.widgets.core.notification.domain.RemoveCallNotificationUseCase;
import com.glia.widgets.core.notification.domain.ShowAudioCallNotificationUseCase;
import com.glia.widgets.core.notification.domain.ShowVideoCallNotificationUseCase;
import com.glia.widgets.core.operator.domain.AddOperatorMediaStateListenerUseCase;
import com.glia.widgets.core.queue.domain.GliaCancelQueueTicketUseCase;
import com.glia.widgets.core.queue.domain.GliaQueueForChatEngagementUseCase;
import com.glia.widgets.core.queue.domain.QueueTicketStateChangeToUnstaffedUseCase;
import com.glia.widgets.core.survey.domain.GliaSurveyUseCase;
import com.glia.widgets.filepreview.domain.usecase.DownloadFileUseCase;
import com.glia.widgets.helper.TimeCounter;
import com.glia.widgets.view.MessagesNotSeenHandler;
import com.glia.widgets.view.MinimizeHandler;

import org.junit.Test;

public class ChatControllerTest {

    @Test
    public void onDestroy_unregistersListeners_whenCalled() {
        MediaUpgradeOfferRepository mediaUpgradeOfferRepository = mock(MediaUpgradeOfferRepository.class);
        TimeCounter callTimer = mock(TimeCounter.class);
        MinimizeHandler minimizeHandler = mock(MinimizeHandler.class);
        GliaOnEngagementUseCase gliaOnEngagementUseCase = mock(GliaOnEngagementUseCase.class);
        GliaOnEngagementEndUseCase gliaOnEngagementEndUseCase = mock(GliaOnEngagementEndUseCase.class);
        GliaOnMessageUseCase gliaOnMessageUseCase = mock(GliaOnMessageUseCase.class);
        GliaOnOperatorTypingUseCase gliaOnOperatorTypingUseCase = mock(GliaOnOperatorTypingUseCase.class);
        QueueTicketStateChangeToUnstaffedUseCase ticketStateChangeToUnstaffedUseCase = mock(QueueTicketStateChangeToUnstaffedUseCase.class);
        ChatController chatController = new ChatController(
                mediaUpgradeOfferRepository,
                callTimer,
                mock(ChatViewCallback.class),
                minimizeHandler,
                mock(DialogController.class),
                mock(MessagesNotSeenHandler.class),
                mock(ShowAudioCallNotificationUseCase.class),
                mock(ShowVideoCallNotificationUseCase.class),
                mock(RemoveCallNotificationUseCase.class),
                mock(GliaLoadHistoryUseCase.class),
                mock(GliaQueueForChatEngagementUseCase.class),
                gliaOnEngagementUseCase,
                gliaOnEngagementEndUseCase,
                gliaOnMessageUseCase,
                gliaOnOperatorTypingUseCase,
                mock(GliaSendMessagePreviewUseCase.class),
                mock(GliaSendMessageUseCase.class),
                mock(AddOperatorMediaStateListenerUseCase.class),
                mock(GliaCancelQueueTicketUseCase.class),
                mock(GliaEndEngagementUseCase.class),
                mock(AddFileToAttachmentAndUploadUseCase.class),
                mock(AddFileAttachmentsObserverUseCase.class),
                mock(RemoveFileAttachmentObserverUseCase.class),
                mock(GetFileAttachmentsUseCase.class),
                mock(RemoveFileAttachmentUseCase.class),
                mock(SupportedFileCountCheckUseCase.class),
                mock(IsShowSendButtonUseCase.class),
                mock(IsShowOverlayPermissionRequestDialogUseCase.class),
                mock(DownloadFileUseCase.class),
                mock(IsEnableChatEditTextUseCase.class),
                mock(SiteInfoUseCase.class),
                mock(GliaSurveyUseCase.class),
                mock(GetEngagementStateFlowableUseCase.class),
                mock(IsFromCallScreenUseCase.class),
                mock(UpdateFromCallScreenUseCase.class),
                mock(CustomCardAdapterTypeUseCase.class),
                mock(CustomCardTypeUseCase.class),
                mock(CustomCardInteractableUseCase.class),
                mock(CustomCardShouldShowUseCase.class),
                ticketStateChangeToUnstaffedUseCase);

        chatController.onDestroy(false);

        verify(mediaUpgradeOfferRepository, times(1)).stopAll();
        verify(callTimer, times(1)).clear();
        verify(minimizeHandler, times(1)).clear();
        verify(gliaOnEngagementUseCase, times(1)).unregisterListener(any());
        verify(gliaOnEngagementEndUseCase, times(1)).unregisterListener(any());
        verify(gliaOnMessageUseCase, times(1)).unregisterListener();
        verify(gliaOnOperatorTypingUseCase, times(1)).unregisterListener();
        verify(ticketStateChangeToUnstaffedUseCase, times(1)).unregisterListener();
    }
}
