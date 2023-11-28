package com.glia.widgets.di;

import android.app.Application;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;

import com.glia.widgets.GliaWidgetsConfig;
import com.glia.widgets.StringProvider;
import com.glia.widgets.StringProviderImpl;
import com.glia.widgets.callvisualizer.ActivityWatcherForCallVisualizer;
import com.glia.widgets.core.audio.AudioControlManager;
import com.glia.widgets.core.audio.domain.OnAudioStartedUseCase;
import com.glia.widgets.core.callvisualizer.CallVisualizerManager;
import com.glia.widgets.core.chathead.ChatHeadManager;
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager;
import com.glia.widgets.core.dialog.PermissionDialogManager;
import com.glia.widgets.core.notification.device.INotificationManager;
import com.glia.widgets.core.notification.device.NotificationManager;
import com.glia.widgets.core.permissions.PermissionManager;
import com.glia.widgets.filepreview.data.source.local.DownloadsFolderDataSource;
import com.glia.widgets.helper.ApplicationLifecycleManager;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.ResourceProvider;
import com.glia.widgets.helper.rx.GliaWidgetsSchedulers;
import com.glia.widgets.helper.rx.Schedulers;
import com.glia.widgets.permissions.ActivityWatcherForPermissionsRequest;
import com.glia.widgets.view.head.ActivityWatcherForChatHead;
import com.glia.widgets.view.head.controller.ServiceChatHeadController;
import com.glia.widgets.view.snackbar.ActivityWatcherForLiveObservation;
import com.glia.widgets.view.unifiedui.theme.UnifiedThemeManager;

public class Dependencies {

    private final static String TAG = "Dependencies";
    private static final UnifiedThemeManager UNIFIED_THEME_MANAGER = new UnifiedThemeManager();
    private static ControllerFactory controllerFactory;
    private static INotificationManager notificationManager;
    private static CallVisualizerManager callVisualizerManager;
    private static GliaSdkConfigurationManager sdkConfigurationManager =
            new GliaSdkConfigurationManager();
    private static UseCaseFactory useCaseFactory;
    private static ManagerFactory managerFactory;
    private static GliaCore gliaCore = new GliaCoreImpl();
    private static ResourceProvider resourceProvider;
    private static StringProvider stringProvider;
    private static Schedulers schedulers;

    public static void onAppCreate(Application application) {
        schedulers = new GliaWidgetsSchedulers();
        resourceProvider = new ResourceProvider(application.getBaseContext());
        stringProvider = new StringProviderImpl(resourceProvider);
        notificationManager = new NotificationManager(application, stringProvider);
        DownloadsFolderDataSource downloadsFolderDataSource = new DownloadsFolderDataSource(application);
        RepositoryFactory repositoryFactory = new RepositoryFactory(gliaCore, downloadsFolderDataSource);

        PermissionManager permissionManager = new PermissionManager(
                application,
                ContextCompat::checkSelfPermission,
                repositoryFactory.getPermissionsRequestRepository(),
                Build.VERSION.SDK_INT
        );
        AudioControlManager audioControlManager = new AudioControlManager(application);
        useCaseFactory = new UseCaseFactory(
                repositoryFactory,
                permissionManager,
                new PermissionDialogManager(application),
                notificationManager,
                sdkConfigurationManager,
                new ChatHeadManager(application),
                audioControlManager,
                schedulers,
                stringProvider,
                gliaCore
        );
        initAudioControlManager(audioControlManager, useCaseFactory.createOnAudioStartedUseCase());

        managerFactory = new ManagerFactory(useCaseFactory);

        controllerFactory = new ControllerFactory(repositoryFactory, useCaseFactory, sdkConfigurationManager, managerFactory);
        initApplicationLifecycleObserver(new ApplicationLifecycleManager(), controllerFactory.getChatHeadController());

        ActivityWatcherForCallVisualizer activityWatcherForCallVisualizer =
            new ActivityWatcherForCallVisualizer(
                getControllerFactory().getDialogController(),
                getControllerFactory().getActivityWatcherForCallVisualizerController()
            );
        application.registerActivityLifecycleCallbacks(activityWatcherForCallVisualizer);

        ActivityWatcherForChatHead activityWatcherForChatHead =
            new ActivityWatcherForChatHead(
                getControllerFactory().getActivityWatcherForChatHeadController());
        application.registerActivityLifecycleCallbacks(activityWatcherForChatHead);

        ActivityWatcherForLiveObservation activityWatcherForLiveObservation = new ActivityWatcherForLiveObservation(
            stringProvider,
            getGliaThemeManager(),
            controllerFactory.getActivityWatcherForLiveObservationController()
        );
        application.registerActivityLifecycleCallbacks(activityWatcherForLiveObservation);

        ActivityWatcherForPermissionsRequest activityWatcherForPermissionsRequest =
            new ActivityWatcherForPermissionsRequest(
                getControllerFactory().getPermissionsController()
            );
        application.registerActivityLifecycleCallbacks(activityWatcherForPermissionsRequest);

        callVisualizerManager = new CallVisualizerManager(
            useCaseFactory.getVisitorCodeViewBuilderUseCase(),
            repositoryFactory.getCallVisualizerRepository(),
            repositoryFactory.getGliaEngagementRepository()
        );
    }

    @VisibleForTesting
    public static void setSchedulers(Schedulers schedulers) {
        Dependencies.schedulers = schedulers;
    }

    public static Schedulers getSchedulers() {
        return schedulers;
    }

    @VisibleForTesting
    public static void setStringProvider(StringProvider sp) {
        stringProvider = sp;
    }

    public static StringProvider getStringProvider() {
        return stringProvider;
    }

    public static UseCaseFactory getUseCaseFactory() {
        return useCaseFactory;
    }

    @NonNull
    public static GliaSdkConfigurationManager getSdkConfigurationManager() {
        return sdkConfigurationManager;
    }

    @VisibleForTesting
    public static void setSdkConfigurationManager(@NonNull GliaSdkConfigurationManager sdkConfigurationManager) {
        Dependencies.sdkConfigurationManager = sdkConfigurationManager;
    }

    @NonNull
    public static UnifiedThemeManager getGliaThemeManager() {
        return UNIFIED_THEME_MANAGER;
    }

    public static void init(GliaWidgetsConfig gliaWidgetsConfig) {
        controllerFactory.init();
        sdkConfigurationManager.setScreenSharingMode(gliaWidgetsConfig.screenSharingMode);
        sdkConfigurationManager.setUseOverlay(gliaWidgetsConfig.isUseOverlay());
        sdkConfigurationManager.setCompanyName(gliaWidgetsConfig.companyName);
        sdkConfigurationManager.setUiTheme(gliaWidgetsConfig.uiTheme);
    }

    public static ControllerFactory getControllerFactory() {
        Logger.d(TAG, "getControllerFactory");
        return controllerFactory;
    }

    @VisibleForTesting
    public static void setControllerFactory(ControllerFactory controllerFactory) {
        Dependencies.controllerFactory = controllerFactory;
    }

    @VisibleForTesting
    public static void setUseCaseFactory(UseCaseFactory useCaseFactory) {
        Dependencies.useCaseFactory = useCaseFactory;
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

    private static void initAudioControlManager(
            AudioControlManager audioControlManager,
            OnAudioStartedUseCase onAudioStartedUseCase
    ) {
        audioControlManager.init(onAudioStartedUseCase);
    }
}
