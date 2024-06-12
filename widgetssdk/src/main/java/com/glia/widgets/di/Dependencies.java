package com.glia.widgets.di;

import android.app.Application;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;

import com.glia.androidsdk.Glia;
import com.glia.widgets.GliaWidgetsConfig;
import com.glia.widgets.StringProvider;
import com.glia.widgets.StringProviderImpl;
import com.glia.widgets.callvisualizer.CallVisualizerActivityWatcher;
import com.glia.widgets.core.audio.AudioControlManager;
import com.glia.widgets.core.audio.domain.OnAudioStartedUseCase;
import com.glia.widgets.core.authentication.AuthenticationManager;
import com.glia.widgets.core.callvisualizer.CallVisualizerManager;
import com.glia.widgets.core.chathead.ChatHeadManager;
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager;
import com.glia.widgets.core.dialog.PermissionDialogManager;
import com.glia.widgets.core.notification.device.INotificationManager;
import com.glia.widgets.core.notification.device.NotificationManager;
import com.glia.widgets.core.permissions.PermissionManager;
import com.glia.widgets.engagement.completion.EngagementCompletionActivityWatcher;
import com.glia.widgets.filepreview.data.source.local.DownloadsFolderDataSource;
import com.glia.widgets.helper.ApplicationLifecycleManager;
import com.glia.widgets.helper.GliaActivityManagerImpl;
import com.glia.widgets.helper.IntentConfigurationHelper;
import com.glia.widgets.helper.IntentConfigurationHelperImpl;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.ResourceProvider;
import com.glia.widgets.helper.rx.GliaWidgetsSchedulers;
import com.glia.widgets.helper.rx.Schedulers;
import com.glia.widgets.operator.OperatorRequestActivityWatcher;
import com.glia.widgets.permissions.ActivityWatcherForPermissionsRequest;
import com.glia.widgets.view.head.ActivityWatcherForChatHead;
import com.glia.widgets.view.head.ChatHeadContract;
import com.glia.widgets.view.snackbar.ActivityWatcherForLiveObservation;
import com.glia.widgets.view.unifiedui.theme.UnifiedThemeManager;

/**
 * @hide
 */
public class Dependencies {
    private final static String TAG = "Dependencies";
    private static final UnifiedThemeManager UNIFIED_THEME_MANAGER = new UnifiedThemeManager();
    private static final AuthenticationManagerProvider authenticationManagerProvider = new AuthenticationManagerProvider();
    private static ControllerFactory controllerFactory;
    private static INotificationManager notificationManager;
    private static CallVisualizerManager callVisualizerManager;
    private static UseCaseFactory useCaseFactory;
    private static ManagerFactory managerFactory;
    private static GliaCore gliaCore = new GliaCoreImpl();
    private static ResourceProvider resourceProvider;
    private static StringProvider stringProvider;
    private static Schedulers schedulers;
    private static GliaSdkConfigurationManager sdkConfigurationManager = new GliaSdkConfigurationManager();
    private static IntentConfigurationHelper intentConfigurationHelper = new IntentConfigurationHelperImpl();
    private static RepositoryFactory repositoryFactory;

    public static void onAppCreate(Application application) {
        schedulers = new GliaWidgetsSchedulers();
        resourceProvider = new ResourceProvider(application.getBaseContext());
        stringProvider = new StringProviderImpl(resourceProvider);
        notificationManager = new NotificationManager(application);
        DownloadsFolderDataSource downloadsFolderDataSource = new DownloadsFolderDataSource(application);
        repositoryFactory = new RepositoryFactory(gliaCore, downloadsFolderDataSource);

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
            authenticationManagerProvider,
            schedulers,
            stringProvider,
            gliaCore,
            application
        );
        initAudioControlManager(audioControlManager, useCaseFactory.createOnAudioStartedUseCase());

        managerFactory = new ManagerFactory(useCaseFactory);

        controllerFactory = new ControllerFactory(repositoryFactory, useCaseFactory, sdkConfigurationManager, managerFactory);
        initApplicationLifecycleObserver(new ApplicationLifecycleManager(), controllerFactory.getChatHeadController());

        CallVisualizerActivityWatcher callVisualizerActivityWatcher = new CallVisualizerActivityWatcher(
            getControllerFactory().getCallVisualizerController(),
            new GliaActivityManagerImpl()
        );

        application.registerActivityLifecycleCallbacks(callVisualizerActivityWatcher);

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
            controllerFactory.getCallVisualizerController()
        );

        EngagementCompletionActivityWatcher engagementCompletionActivityWatcher = new EngagementCompletionActivityWatcher(
            controllerFactory.getEndEngagementController(),
            new GliaActivityManagerImpl()
        );
        application.registerActivityLifecycleCallbacks(engagementCompletionActivityWatcher);

        OperatorRequestActivityWatcher operatorRequestActivityWatcher = new OperatorRequestActivityWatcher(
            controllerFactory.getOperatorRequestController(),
            intentConfigurationHelper,
            new GliaActivityManagerImpl()
        );
        application.registerActivityLifecycleCallbacks(operatorRequestActivityWatcher);
    }

    public static Schedulers getSchedulers() {
        return schedulers;
    }

    @VisibleForTesting
    public static void setSchedulers(Schedulers schedulers) {
        Dependencies.schedulers = schedulers;
    }

    public static StringProvider getStringProvider() {
        return stringProvider;
    }

    @VisibleForTesting
    public static void setStringProvider(StringProvider sp) {
        stringProvider = sp;
    }

    public static UseCaseFactory getUseCaseFactory() {
        return useCaseFactory;
    }

    @VisibleForTesting
    public static void setUseCaseFactory(UseCaseFactory useCaseFactory) {
        Dependencies.useCaseFactory = useCaseFactory;
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
        repositoryFactory.getEngagementRepository().initialize();
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

    public static RepositoryFactory getRepositoryFactory() {
        return repositoryFactory;
    }

    @VisibleForTesting
    public static void setRepositoryFactory(RepositoryFactory repositoryFactory) {
        Dependencies.repositoryFactory = repositoryFactory;
    }

    public static IntentConfigurationHelper getIntentConfigurationHelper() {
        return intentConfigurationHelper;
    }

    public static void setIntentConfigurationHelper(IntentConfigurationHelper intentConfigurationHelper) {
        Dependencies.intentConfigurationHelper = intentConfigurationHelper;
    }

    public static void setAuthenticationManager(@NonNull AuthenticationManager authenticationManager) {
        authenticationManagerProvider.setAuthenticationManager(authenticationManager);
    }

    private static void initApplicationLifecycleObserver(
        ApplicationLifecycleManager lifecycleManager,
        ChatHeadContract.Controller chatBubbleController
    ) {
        lifecycleManager.addObserver((source, event) -> {
            if (event == Lifecycle.Event.ON_STOP) {
                chatBubbleController.onApplicationStop();
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && Glia.isInitialized()) {
                    notificationManager.startNotificationRemovalService();
                }
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

    public static void destroyControllers() {
        getControllerFactory().destroyControllers();
    }

    public static void destroyControllersAndResetEngagementData() {
        destroyControllers();
        getRepositoryFactory().getEngagementRepository().reset();
    }

    public static void destroyControllersAndResetQueueing() {
        getControllerFactory().destroyControllersForAuthentication();
        getRepositoryFactory().getEngagementRepository().resetQueueing();
    }

    static class AuthenticationManagerProvider {
        @Nullable
        private AuthenticationManager authenticationManager;

        @Nullable
        public AuthenticationManager getAuthenticationManager() {
            return authenticationManager;
        }

        public void setAuthenticationManager(@NonNull AuthenticationManager authenticationManager) {
            this.authenticationManager = authenticationManager;
        }
    }
}
