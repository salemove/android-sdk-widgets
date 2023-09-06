package com.glia.widgets.core.screensharing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import android.app.Activity;

import com.glia.androidsdk.GliaException;
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager;
import com.glia.widgets.core.dialog.DialogController;
import com.glia.widgets.core.notification.domain.RemoveScreenSharingNotificationUseCase;
import com.glia.widgets.core.notification.domain.ShowScreenSharingNotificationUseCase;
import com.glia.widgets.core.permissions.domain.HasScreenSharingNotificationChannelEnabledUseCase;
import com.glia.widgets.core.screensharing.data.GliaScreenSharingRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ScreenSharingControllerTest {

    private GliaScreenSharingRepository gliaScreenSharingRepository;
    private DialogController dialogController;
    private ShowScreenSharingNotificationUseCase showScreenSharingNotificationUseCase;
    private RemoveScreenSharingNotificationUseCase removeScreenSharingNotificationUseCase;
    private HasScreenSharingNotificationChannelEnabledUseCase hasScreenSharingNotificationChannelEnabledUseCase;
    private ScreenSharingController subjectUnderTest;

    @Before
    public void setUp() {
        gliaScreenSharingRepository =
                mock(GliaScreenSharingRepository.class);
        dialogController = mock(DialogController.class);
        showScreenSharingNotificationUseCase =
                mock(ShowScreenSharingNotificationUseCase.class);
        removeScreenSharingNotificationUseCase =
                mock(RemoveScreenSharingNotificationUseCase.class);
        hasScreenSharingNotificationChannelEnabledUseCase =
                mock(HasScreenSharingNotificationChannelEnabledUseCase.class);

        subjectUnderTest = new ScreenSharingController(
                gliaScreenSharingRepository,
                dialogController,
                showScreenSharingNotificationUseCase,
                removeScreenSharingNotificationUseCase,
                hasScreenSharingNotificationChannelEnabledUseCase,
                mock(GliaSdkConfigurationManager.class)
        );
    }

    @Test
    public void onScreenSharingRequest_noInteractions_whenNoViewCallbacksAdded() {
        subjectUnderTest.onScreenSharingRequest();

        verifyNoInteractions(dialogController);
    }

    @Test
    public void onScreenSharingRequest_showsEnableNotificationsDialog_whenNotificationChannelDisabled() {
        when(hasScreenSharingNotificationChannelEnabledUseCase.invoke()).thenReturn(false);
        subjectUnderTest.setViewCallback(mock(ScreenSharingController.ViewCallback.class));

        subjectUnderTest.onScreenSharingRequest();

        verify(dialogController).showEnableScreenSharingNotificationsAndStartSharingDialog();
    }

    @Test
    public void onScreenSharingRequest_showsStartScreenSharingDialog_whenNotificationChannelEnabled() {
        when(hasScreenSharingNotificationChannelEnabledUseCase.invoke()).thenReturn(true);
        subjectUnderTest.setViewCallback(mock(ScreenSharingController.ViewCallback.class));

        subjectUnderTest.onScreenSharingRequest();

        verify(dialogController).showStartScreenSharingDialog();
    }

    @Test
    public void onScreenSharingRequestError_removesNotificationCallsOnScreenSharingRequestError() {
        ScreenSharingController.ViewCallback viewCallback = mock(ScreenSharingController.ViewCallback.class);
        subjectUnderTest.setViewCallback(viewCallback);
        GliaException exception = mock(GliaException.class);

        subjectUnderTest.onScreenSharingRequestError(exception);

        verify(viewCallback).onScreenSharingRequestError(exception);
        verify(removeScreenSharingNotificationUseCase).invoke();
    }

    @Test
    public void onResume_acceptsScreenSharing_whenNotificationChannelEnabled() {
        subjectUnderTest.hasPendingScreenSharingRequest = true;
        when(hasScreenSharingNotificationChannelEnabledUseCase.invoke())
                .thenReturn(true);

        subjectUnderTest.onResume(mock(Activity.class));

        verify(showScreenSharingNotificationUseCase).invoke();
        verify(gliaScreenSharingRepository).onScreenSharingAccepted(any(), any());
    }

    @Test
    public void onResume_showsEnableNotifications_whenNotificationChannelDisabled() {
        subjectUnderTest.hasPendingScreenSharingRequest = true;
        when(hasScreenSharingNotificationChannelEnabledUseCase.invoke())
                .thenReturn(false);

        subjectUnderTest.onResume(mock(Activity.class));

        verify(dialogController).showEnableScreenSharingNotificationsAndStartSharingDialog();
    }

    @Test
    public void onResume_doNothing_whenEnableScreenSharingNotificationsAndStartSharingDialogShown() {
        subjectUnderTest.hasPendingScreenSharingRequest = true;
        when(hasScreenSharingNotificationChannelEnabledUseCase.invoke())
            .thenReturn(false);
        when(dialogController.isEnableScreenSharingNotificationsAndStartSharingDialogShown()).thenReturn(true);

        subjectUnderTest.onResume(mock(Activity.class));

        verify(dialogController, never()).showEnableScreenSharingNotificationsAndStartSharingDialog();
    }

    @Test
    public void onScreenSharingAccepted_acceptsScreenSharing() {
        subjectUnderTest.onScreenSharingAccepted(mock(Activity.class));

        verify(showScreenSharingNotificationUseCase).invoke();
        verify(gliaScreenSharingRepository).onScreenSharingAccepted(any(), any());
    }

    @Test
    public void onScreenSharingDeclined_declinesScreenSharing() {
        subjectUnderTest.onScreenSharingDeclined();

        verify(gliaScreenSharingRepository).onScreenSharingDeclined();
    }

    @Test
    public void onScreenSharingNotificationEndPressed_hidesNotificationEndsScreenSharing() {
        subjectUnderTest.onScreenSharingNotificationEndPressed();

        verify(removeScreenSharingNotificationUseCase).invoke();
        verify(gliaScreenSharingRepository).onEndScreenSharing();
    }
}
