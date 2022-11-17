package com.glia.widgets.call;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.glia.widgets.call.domain.ToggleVisitorAudioMediaMuteUseCase;
import com.glia.widgets.call.domain.ToggleVisitorVideoUseCase;
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase;
import com.glia.widgets.core.dialog.DialogController;
import com.glia.widgets.core.dialog.domain.IsShowEnableCallNotificationChannelDialogUseCase;
import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase;
import com.glia.widgets.core.engagement.domain.GetEngagementStateFlowableUseCase;
import com.glia.widgets.core.engagement.domain.GliaEndEngagementUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementEndUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementUseCase;
import com.glia.widgets.core.engagement.domain.ShouldShowMediaEngagementViewUseCase;
import com.glia.widgets.core.mediaupgradeoffer.MediaUpgradeOfferRepository;
import com.glia.widgets.core.notification.domain.RemoveCallNotificationUseCase;
import com.glia.widgets.core.notification.domain.ShowAudioCallNotificationUseCase;
import com.glia.widgets.core.notification.domain.ShowVideoCallNotificationUseCase;
import com.glia.widgets.core.operator.domain.AddOperatorMediaStateListenerUseCase;
import com.glia.widgets.core.permissions.domain.HasCallNotificationChannelEnabledUseCase;
import com.glia.widgets.core.queue.domain.GliaCancelQueueTicketUseCase;
import com.glia.widgets.core.queue.domain.GliaQueueForMediaEngagementUseCase;
import com.glia.widgets.core.queue.domain.QueueTicketStateChangeToUnstaffedUseCase;
import com.glia.widgets.core.survey.domain.GliaSurveyUseCase;
import com.glia.widgets.core.visitor.domain.AddVisitorMediaStateListenerUseCase;
import com.glia.widgets.core.visitor.domain.RemoveVisitorMediaStateListenerUseCase;
import com.glia.widgets.helper.TimeCounter;
import com.glia.widgets.view.MessagesNotSeenHandler;
import com.glia.widgets.view.MinimizeHandler;

import org.junit.Test;

public class CallControllerTest {

    @Test
    public void onDestroy_unregistersListeners_whenCalled() {
        MinimizeHandler minimizeHandler = mock(MinimizeHandler.class);
        TimeCounter callTimer = mock(TimeCounter.class);
        MessagesNotSeenHandler messagesNotSeenHandler = mock(MessagesNotSeenHandler.class);
        GliaOnEngagementUseCase gliaOnEngagementUseCase = mock(GliaOnEngagementUseCase.class);
        GliaOnEngagementEndUseCase gliaOnEngagementEndUseCase = mock(GliaOnEngagementEndUseCase.class);
        QueueTicketStateChangeToUnstaffedUseCase ticketStateChangeToUnstaffedUseCase =
                mock(QueueTicketStateChangeToUnstaffedUseCase.class);

        CallController callController = new CallController(
                mock(MediaUpgradeOfferRepository.class),
                callTimer,
                mock(CallViewCallback.class),
                mock(TimeCounter.class),
                mock(TimeCounter.class),
                minimizeHandler,
                mock(DialogController.class),
                messagesNotSeenHandler,
                mock(ShowAudioCallNotificationUseCase.class),
                mock(ShowVideoCallNotificationUseCase.class),
                mock(RemoveCallNotificationUseCase.class),
                mock(GliaQueueForMediaEngagementUseCase.class),
                mock(GliaCancelQueueTicketUseCase.class),
                gliaOnEngagementUseCase,
                mock(AddOperatorMediaStateListenerUseCase.class),
                gliaOnEngagementEndUseCase,
                mock(GliaEndEngagementUseCase.class),
                mock(ShouldShowMediaEngagementViewUseCase.class),
                mock(IsShowOverlayPermissionRequestDialogUseCase.class),
                mock(HasCallNotificationChannelEnabledUseCase.class),
                mock(IsShowEnableCallNotificationChannelDialogUseCase.class),
                mock(GliaSurveyUseCase.class),
                mock(AddVisitorMediaStateListenerUseCase.class),
                mock(RemoveVisitorMediaStateListenerUseCase.class),
                mock(ToggleVisitorAudioMediaMuteUseCase.class),
                mock(ToggleVisitorVideoUseCase.class),
                mock(GetEngagementStateFlowableUseCase.class),
                mock(UpdateFromCallScreenUseCase.class),
                ticketStateChangeToUnstaffedUseCase);

        callController.onDestroy(false);

        verify(ticketStateChangeToUnstaffedUseCase, times(1)).unregisterListener();
        verify(callTimer, times(1)).clear();
        verify(minimizeHandler, times(1)).clear();
        verify(messagesNotSeenHandler, times(1)).removeListener(any());
        verify(gliaOnEngagementUseCase, times(1)).unregisterListener(any());
        verify(gliaOnEngagementEndUseCase, times(1)).unregisterListener(any());
    }
}
