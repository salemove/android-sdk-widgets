package com.glia.widgets.core.screensharing.data;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.androidsdk.screensharing.ScreenSharing;
import com.glia.androidsdk.screensharing.ScreenSharingRequest;
import com.glia.widgets.core.screensharing.GliaScreenSharingCallback;
import com.glia.widgets.di.GliaCore;

import org.junit.Test;

import java.util.function.Consumer;

public class GliaScreenSharingRepositoryTest {

    @Test
    @SuppressWarnings("unchecked")
    public void init_callsOnScreenSharingRequest_whenRequested() {
        GliaScreenSharingRepository subjectUnderTest = new GliaScreenSharingRepository(GLIA_CORE);
        GliaScreenSharingCallback screenSharingCallback = mock(GliaScreenSharingCallback.class);
        OmnicoreEngagement engagement = mock(OmnicoreEngagement.class);
        ScreenSharing screenSharing = mock(ScreenSharing.class);
        ScreenSharingRequest request = mock(ScreenSharingRequest.class);
        doAnswer(invocation -> {
            Consumer<OmnicoreEngagement> callback = invocation.getArgument(1);
            callback.accept(engagement);
            return null;
        }).when(GLIA_CORE).on(any(), any());
        when(engagement.getScreenSharing()).thenReturn(screenSharing);
        doAnswer(invocation -> {
            Consumer<ScreenSharingRequest> callback = invocation.getArgument(1);
            callback.accept(request);
            return null;
        }).when(screenSharing).on(eq(ScreenSharing.Events.SCREEN_SHARING_REQUEST), any(Consumer.class));

        subjectUnderTest.init(screenSharingCallback);

        verify(screenSharingCallback).onScreenSharingRequest();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onScreenSharingAccepted_callsOnScreenSharingRequestSuccess_whenNoException() {
        GliaScreenSharingRepository subjectUnderTest = new GliaScreenSharingRepository(GLIA_CORE);
        GliaScreenSharingCallback screenSharingCallback = mock(GliaScreenSharingCallback.class);
        OmnicoreEngagement engagement = mock(OmnicoreEngagement.class);
        ScreenSharing screenSharing = mock(ScreenSharing.class);
        ScreenSharingRequest request = mock(ScreenSharingRequest.class);
        doAnswer(invocation -> {
            Consumer<OmnicoreEngagement> callback = invocation.getArgument(1);
            callback.accept(engagement);
            return null;
        }).when(GLIA_CORE).on(any(), any());
        when(engagement.getScreenSharing()).thenReturn(screenSharing);
        doAnswer(invocation -> {
            Consumer<ScreenSharingRequest> callback = invocation.getArgument(1);
            callback.accept(request);
            return null;
        }).when(screenSharing).on(eq(ScreenSharing.Events.SCREEN_SHARING_REQUEST), any(Consumer.class));
        doAnswer(invocation -> {
            Consumer<GliaException> callback = invocation.getArgument(3);
            callback.accept(null);
            return null;
        }).when(request).accept(any(), any(Activity.class), anyInt(), any());
        subjectUnderTest.init(screenSharingCallback);

        subjectUnderTest.onScreenSharingAccepted(ACTIVITY, ScreenSharing.Mode.UNBOUNDED);

        verify(screenSharingCallback).onScreenSharingRequestSuccess();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onScreenSharingAccepted_callsOnScreenSharingRequestError_whenException() {
        GliaScreenSharingRepository subjectUnderTest = new GliaScreenSharingRepository(GLIA_CORE);
        GliaScreenSharingCallback screenSharingCallback = mock(GliaScreenSharingCallback.class);
        OmnicoreEngagement engagement = mock(OmnicoreEngagement.class);
        ScreenSharing screenSharing = mock(ScreenSharing.class);
        ScreenSharingRequest request = mock(ScreenSharingRequest.class);
        doAnswer(invocation -> {
            Consumer<OmnicoreEngagement> callback = invocation.getArgument(1);
            callback.accept(engagement);
            return null;
        }).when(GLIA_CORE).on(any(), any());
        when(engagement.getScreenSharing()).thenReturn(screenSharing);
        doAnswer(invocation -> {
            Consumer<ScreenSharingRequest> callback = invocation.getArgument(1);
            callback.accept(request);
            return null;
        }).when(screenSharing).on(eq(ScreenSharing.Events.SCREEN_SHARING_REQUEST), any(Consumer.class));
        doAnswer(invocation -> {
            Consumer<GliaException> callback = invocation.getArgument(3);
            callback.accept(mock(GliaException.class));
            return null;
        }).when(request).accept(any(), any(Activity.class), anyInt(), any());
        subjectUnderTest.init(screenSharingCallback);

        subjectUnderTest.onScreenSharingAccepted(ACTIVITY, ScreenSharing.Mode.UNBOUNDED);

        verify(screenSharingCallback).onScreenSharingRequestError(any());
    }

    private static final GliaCore GLIA_CORE = mock(GliaCore.class);
    private static final Activity ACTIVITY = new Activity();
}
