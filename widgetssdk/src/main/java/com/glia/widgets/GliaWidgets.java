package com.glia.widgets;

import android.app.Application;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.GliaConfig;
import com.glia.widgets.di.ControllerFactory;
import com.glia.widgets.di.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

public class GliaWidgets {

    public static final String UI_THEME = "ui_theme";
    public static final String COMPANY_NAME = "company_name";
    public static final String QUEUE_ID = "queue_id";
    public static final String CONTEXT_URL = "context_url";
    public static final String OPERATOR_PROFILE_IMG_URL = "operator_profile_img_url";
    public static final String RETURN_DESTINATION = "return_destination";
    public static final String CHAT_ACTIVITY = "chat_activity";
    public static final String CALL_ACTIVITY = "call_activity";
    public static final String IS_ORIGIN_CALL = "is_origin_call";

    private static ControllerFactory controllerFactory;
    private static final List<String> activitiesInBackstack = new ArrayList<>();

    public synchronized static void onAppCreate(Application application) {
        Glia.onAppCreate(application);
    }

    public synchronized static void init(GliaWidgetsConfig gliaWidgetsConfig) {
        GliaConfig gliaConfig = new GliaConfig.Builder()
                .setApiToken(gliaWidgetsConfig.getApiToken())
                .setAppToken(gliaWidgetsConfig.getAppToken())
                .setSiteId(gliaWidgetsConfig.getSiteId())
                .setRegion(gliaWidgetsConfig.getRegion())
                .setContext(gliaWidgetsConfig.getContext())
                .build();

        Glia.init(gliaConfig);
        controllerFactory = new ControllerFactory(getRepositoryFactory());
    }

    private static RepositoryFactory getRepositoryFactory() {
        return new RepositoryFactory();
    }

    public static ControllerFactory getControllerFactory() {
        return controllerFactory;
    }

    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Glia.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Needed because CallActivity uses translucent window. And this flag messes with any background
     * activity lifecycles. Using this to know what activities are in the current backstack.
     *
     * @param activity 1 of either {@link com.glia.widgets.GliaWidgets#CALL_ACTIVITY}
     *                 or {@link com.glia.widgets.GliaWidgets#CHAT_ACTIVITY}
     */
    public static void addActivityToBackStack(String activity) {
        if (activity.equals(CALL_ACTIVITY) || activity.equals(CHAT_ACTIVITY)) {
            if (!activitiesInBackstack.contains(activity)) {
                activitiesInBackstack.add(activity);
            }
        }
    }

    public static void removeActivityFromBackStack(String activity) {
        activitiesInBackstack.remove(activity);
    }

    public static boolean isInBackstack(String activity) {
        return activitiesInBackstack.contains(activity);
    }
}
