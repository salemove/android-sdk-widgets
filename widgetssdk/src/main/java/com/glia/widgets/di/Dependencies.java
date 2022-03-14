package com.glia.widgets.di;

import android.app.Application;

import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.Lifecycle;

import com.glia.widgets.core.chathead.ChatHeadManager;
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager;
import com.glia.widgets.core.dialog.PermissionDialogManager;
import com.glia.widgets.core.notification.device.INotificationManager;
import com.glia.widgets.core.notification.device.NotificationManager;
import com.glia.widgets.core.permissions.PermissionManager;
import com.glia.widgets.filepreview.data.source.local.DownloadsFolderDataSource;
import com.glia.widgets.helper.ApplicationLifecycleManager;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.view.head.controller.ServiceChatHeadController;

public class Dependencies {

    private final static String TAG = "Dependencies";
    private static ControllerFactory controllerFactory;
    private static INotificationManager notificationManager;
    private static GliaSdkConfigurationManager sdkConfigurationManager =
            new GliaSdkConfigurationManager();
    private static UseCaseFactory useCaseFactory;
    private static GliaCore gliaCore = new GliaCoreImpl();

    public static void onAppCreate(Application application) {
        notificationManager = new NotificationManager(application);
        DownloadsFolderDataSource downloadsFolderDataSource = new DownloadsFolderDataSource(application);
        RepositoryFactory repositoryFactory = new RepositoryFactory(gliaCore, downloadsFolderDataSource);
        useCaseFactory = new UseCaseFactory(
                repositoryFactory,
                new PermissionManager(application),
                new PermissionDialogManager(application),
                notificationManager,
                sdkConfigurationManager,
                new ChatHeadManager(application)
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

    public static void init() {
        controllerFactory.init();
    }

    public static ControllerFactory getControllerFactory() {
        Logger.d(TAG, "getControllerFactory");
        return controllerFactory;
    }

    public static GliaCore glia() {
        return gliaCore;
    }

    @VisibleForTesting
    public static void setGlia(GliaCore gliaCore) {
        Dependencies.gliaCore = gliaCore;
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
