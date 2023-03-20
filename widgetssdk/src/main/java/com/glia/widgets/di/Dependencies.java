package com.glia.widgets.di;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.Lifecycle;

import com.glia.widgets.GliaWidgetsConfig;
import com.glia.widgets.core.callvisualizer.CallVisualizerManager;
import com.glia.widgets.core.chathead.ChatHeadManager;
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager;
import com.glia.widgets.core.dialog.PermissionDialogManager;
import com.glia.widgets.core.notification.device.INotificationManager;
import com.glia.widgets.core.notification.device.NotificationManager;
import com.glia.widgets.core.permissions.PermissionManager;
import com.glia.widgets.filepreview.data.source.local.DownloadsFolderDataSource;
import com.glia.widgets.callvisualizer.ActivityWatcherForCallVisualizer;
import com.glia.widgets.helper.ApplicationLifecycleManager;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.ResourceProvider;
import com.glia.widgets.helper.rx.GliaWidgetsSchedulers;
import com.glia.widgets.view.head.controller.ServiceChatHeadController;
import com.glia.widgets.view.unifiedui.theme.UnifiedThemeManager;

public class Dependencies {

    private final static String TAG = "Dependencies";

    private static ControllerFactory controllerFactory;
    private static INotificationManager notificationManager;
    private static CallVisualizerManager callVisualizerManager;
    private static GliaSdkConfigurationManager sdkConfigurationManager =
            new GliaSdkConfigurationManager();
    private static UseCaseFactory useCaseFactory;
    private static GliaCore gliaCore = new GliaCoreImpl();
    private static ResourceProvider resourceProvider;
    private static final UnifiedThemeManager UNIFIED_THEME_MANAGER = new UnifiedThemeManager();

    public static void onAppCreate(Application application) {
        notificationManager = new NotificationManager(application);
        DownloadsFolderDataSource downloadsFolderDataSource = new DownloadsFolderDataSource(application);
        RepositoryFactory repositoryFactory = new RepositoryFactory(
                gliaCore,
                downloadsFolderDataSource
        );
        useCaseFactory = new UseCaseFactory(
                repositoryFactory,
                new PermissionManager(application),
                new PermissionDialogManager(application),
                notificationManager,
                sdkConfigurationManager,
                new ChatHeadManager(application),
                new GliaWidgetsSchedulers()
        );

        controllerFactory = new ControllerFactory(
                repositoryFactory,
                useCaseFactory,
                sdkConfigurationManager
        );
        initApplicationLifecycleObserver(
                new ApplicationLifecycleManager(),
                controllerFactory.getChatHeadController()
        );
        ActivityWatcherForCallVisualizer activityWatcherForCallVisualizer =
                new ActivityWatcherForCallVisualizer(
                        getControllerFactory().getDialogController(),
                        getControllerFactory().getActivityWatcherController()
                );
        application.registerActivityLifecycleCallbacks(activityWatcherForCallVisualizer);
        resourceProvider = new ResourceProvider(application.getBaseContext());
        callVisualizerManager = new CallVisualizerManager(
                useCaseFactory.getVisitorCodeViewBuilderUseCase(),
                repositoryFactory.getCallVisualizerRepository(),
                repositoryFactory.getGliaEngagementRepository()
        );
    }

    public static UseCaseFactory getUseCaseFactory() {
        return useCaseFactory;
    }

    public static INotificationManager getNotificationManager() {
        return notificationManager;
    }

    public static GliaSdkConfigurationManager getSdkConfigurationManager() {
        return sdkConfigurationManager;
    }

    @NonNull
    public static UnifiedThemeManager getGliaThemeManager() {
        return UNIFIED_THEME_MANAGER;
    }

    public static void init(GliaWidgetsConfig gliaWidgetsConfig) {
        controllerFactory.init();
        sdkConfigurationManager.setScreenSharingMode(gliaWidgetsConfig.getScreenSharingMode());
        sdkConfigurationManager.setUseOverlay(gliaWidgetsConfig.isUseOverlay());
        sdkConfigurationManager.setCompanyName(gliaWidgetsConfig.getCompanyName());
        sdkConfigurationManager.setUiTheme(gliaWidgetsConfig.getUiTheme());
    }

    public static ControllerFactory getControllerFactory() {
        Logger.d(TAG, "getControllerFactory");
        return controllerFactory;
    }

    public static GliaCore glia() {
        return gliaCore;
    }

    public static CallVisualizerManager getCallVisualizerManager() {
        return callVisualizerManager;
    }

    @VisibleForTesting
    public static void setGlia(GliaCore gliaCore) {
        Dependencies.gliaCore = gliaCore;
    }

    public static ResourceProvider getResourceProvider() {
        return resourceProvider;
    }

    @VisibleForTesting
    public static void setControllerFactory(ControllerFactory controllerFactory) {
        Dependencies.controllerFactory = controllerFactory;
    }

    @VisibleForTesting
    public static void setNotificationManager(INotificationManager notificationManager) {
        Dependencies.notificationManager = notificationManager;
    }

    @VisibleForTesting
    public static void setSdkConfigurationManager(GliaSdkConfigurationManager sdkConfigurationManager) {
        Dependencies.sdkConfigurationManager = sdkConfigurationManager;
    }

    @VisibleForTesting
    public static void setResourceProvider(ResourceProvider resourceProvider) {
        Dependencies.resourceProvider = resourceProvider;
    }

    private static void initApplicationLifecycleObserver(
            ApplicationLifecycleManager lifecycleManager,
            ServiceChatHeadController chatBubbleController
    ) {
        lifecycleManager.addObserver((source, event) -> {
            if (event == Lifecycle.Event.ON_STOP) {
                chatBubbleController.onApplicationStop();
            } else if (event == Lifecycle.Event.ON_DESTROY) {
                chatBubbleController.onDestroy();
            }
        });
    }
}
