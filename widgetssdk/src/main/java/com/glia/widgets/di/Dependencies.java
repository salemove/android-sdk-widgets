package com.glia.widgets.di;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.VisibleForTesting;

import com.glia.widgets.Constants;
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager;
import com.glia.widgets.filepreview.data.source.local.DownloadsFolderDataSource;
import com.glia.widgets.view.head.ChatHeadService;
import com.glia.widgets.view.head.ChatHeadsController;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.core.dialog.PermissionDialogManager;
import com.glia.widgets.core.notification.device.INotificationManager;
import com.glia.widgets.core.notification.device.NotificationManager;
import com.glia.widgets.core.permissions.PermissionManager;

import java.util.ArrayList;
import java.util.List;

public class Dependencies {

    private final static String TAG = "Dependencies";
    private static ControllerFactory controllerFactory;
    private static final List<String> activitiesInBackstack = new ArrayList<>();
    private static INotificationManager notificationManager;
    private static GliaSdkConfigurationManager sdkConfigurationManager;
    private static UseCaseFactory useCaseFactory;
    private static GliaCore gliaCore = new GliaCoreImpl();

    public static void onAppCreate(Application application) {
        notificationManager = new NotificationManager(application);
        sdkConfigurationManager = new GliaSdkConfigurationManager();
        DownloadsFolderDataSource downloadsFolderDataSource = new DownloadsFolderDataSource(application);
        RepositoryFactory repositoryFactory = new RepositoryFactory(downloadsFolderDataSource);
        useCaseFactory = new UseCaseFactory(
                repositoryFactory,
                new PermissionManager(application),
                new PermissionDialogManager(application),
                notificationManager,
                sdkConfigurationManager
        );

        controllerFactory = new ControllerFactory(repositoryFactory, useCaseFactory);
        controllerFactory.getChatHeadsController().addChatHeadServiceListener(
                new ChatHeadsController.ChatHeadServiceListener() {
                    @Override
                    public void startService() {
                        application.startService(new Intent(application, ChatHeadService.class));
                    }

                    @Override
                    public void stopService() {
                        application.stopService(new Intent(application, ChatHeadService.class));
                    }
                });
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

    /**
     * Needed because CallActivity uses translucent window. And this flag messes with any background
     * activity lifecycles. Used to know what activities are in the current backstack.
     *
     * @param activity 1 of either {@link com.glia.widgets.Constants#CALL_ACTIVITY}
     *                 or {@link com.glia.widgets.Constants#CHAT_ACTIVITY}
     */
    public static void addActivityToBackStack(String activity) {
        Logger.d(TAG, "addActivityToBackStack");
        if (activity.equals(Constants.CALL_ACTIVITY) || activity.equals(Constants.CHAT_ACTIVITY) || activity.equals(Constants.IMAGE_PREVIEW_ACTIVITY)) {
            if (!activitiesInBackstack.contains(activity)) {
                activitiesInBackstack.add(activity);
            }
        }
    }

    public static void removeActivityFromBackStack(String activity) {
        Logger.d(TAG, "removeActivityFromBackStack");
        activitiesInBackstack.remove(activity);
    }

    public static boolean isInBackstack(String activity) {
        Logger.d(TAG, "isInBackstack");
        return activitiesInBackstack.contains(activity);
    }

    public static GliaCore glia() {
        return gliaCore;
    }

    @VisibleForTesting
    public static void setGlia(GliaCore gliaCore) {
        Dependencies.gliaCore = gliaCore;
    }
}
