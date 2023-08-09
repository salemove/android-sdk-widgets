package com.glia.widgets;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.glia.androidsdk.GliaConfig;
import com.glia.androidsdk.SiteApiKey;
import com.glia.androidsdk.omnibrowse.Omnibrowse;
import com.glia.widgets.callvisualizer.controller.CallVisualizerController;
import com.glia.widgets.di.ControllerFactory;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.di.GliaCore;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class GliaWidgetsTest {
    @ClassRule
    public static final TestRule rule = new InstantTaskExecutorRule();

    private GliaCore gliaCore;
    private ControllerFactory controllerFactory;

    @Before
    public void setUp() {
        gliaCore = mock(GliaCore.class);
        Dependencies.setGlia(gliaCore);

        controllerFactory = mock(ControllerFactory.class);
        Dependencies.setControllerFactory(controllerFactory);
    }

    @Test
    public void onAppCreate_setApplicationToGliaCore_whenCalled() {
        Application application = mock(Application.class);
        Resources res = mock(Resources.class);
        when(application.getBaseContext()).thenReturn(application);
        when(application.getResources()).thenReturn(res);
        when(res.getString(anyInt())).thenReturn("test");
        when(res.getResourceName(anyInt())).thenReturn("test/test2");
        when(res.getString(anyInt(), any())).thenReturn("value");
        when(res.getString(anyInt(), eq(null))).thenReturn("value");
        NotificationManager notificationManager = mock(NotificationManager.class);
        when(application.getSystemService(NotificationManager.class)).thenReturn(notificationManager);
        GliaWidgets.onAppCreate(application);
        verify(gliaCore).onAppCreate(application);
    }

    @Test
    public void init_setConfigToGliaCore_whenCalled() {
        SiteApiKey siteApiKey = new SiteApiKey("SiteApiId", "SiteApiSecret");
        String siteId = "SiteId";
        String region = "Region";
        Context context = mock(Context.class);
        GliaWidgetsConfig widgetsConfig = new GliaWidgetsConfig.Builder()
                .setSiteApiKey(siteApiKey)
                .setSiteId(siteId)
                .setRegion(region)
                .setContext(context)
                .build();
        Omnibrowse callVisualizer = mock(Omnibrowse.class);
        when(gliaCore.getCallVisualizer()).thenReturn(callVisualizer);
        CallVisualizerController callVisualizerController = mock(CallVisualizerController.class);
        when(controllerFactory.getCallVisualizerController()).thenReturn(callVisualizerController);

        GliaWidgets.init(widgetsConfig);

        ArgumentCaptor<GliaConfig> captor = ArgumentCaptor.forClass(GliaConfig.class);
        verify(gliaCore).init(captor.capture());
        GliaConfig gliaConfig = captor.getValue();
        assertEquals(siteApiKey, gliaConfig.getSiteApiKey());
        assertEquals(siteId, gliaConfig.getSiteId());
        assertEquals(region, gliaConfig.getRegion());
        assertEquals(context, gliaConfig.getContext());
    }
}
