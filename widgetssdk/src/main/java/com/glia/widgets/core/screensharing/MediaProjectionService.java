package com.glia.widgets.core.screensharing;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Glia;
import com.glia.androidsdk.omnibrowse.Omnibrowse;
import com.glia.androidsdk.omnibrowse.OmnibrowseEngagement;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.core.notification.NotificationFactory;

import java.util.function.Consumer;

/**
 * Apps targeting SDK version 29 or later require for screen-sharing a running foreground service.
 * This service requires declaration in the Manifest file.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class MediaProjectionService extends Service {
    private static final String TAG = "MediaProjectionService";
    private static final int SERVICE_ID = 123;

    public interface Actions {
        String START = "EngagementMonitoringService:Start";
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setupAsForegroundService();
    }

    public void setupAsForegroundService() {
        // Register this service as a foreground service.
        startForeground(SERVICE_ID, NotificationFactory.createScreenSharingNotification(this));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Actions.START.equals(intent.getAction())) {
            setupListeners();
        }

        return Service.START_STICKY;
    }

    private final Consumer<OmnicoreEngagement> omnicoreEngagementStartListener = (engagement) ->
            engagement.on(Engagement.Events.END, (Runnable) this::stopSelf);

    private final Consumer<OmnibrowseEngagement> omnibrowseEngagementStartListener = (engagement) ->
            engagement.on(Engagement.Events.END, (Runnable) this::stopSelf);

    public void setupListeners() {
        Glia.on(Glia.Events.ENGAGEMENT, omnicoreEngagementStartListener);
        Glia.omnibrowse.on(Omnibrowse.Events.ENGAGEMENT, omnibrowseEngagementStartListener);
    }

    public void removeListeners() {
        Glia.off(Glia.Events.ENGAGEMENT, omnicoreEngagementStartListener);
        Glia.omnibrowse.off(Omnibrowse.Events.ENGAGEMENT, omnibrowseEngagementStartListener);
    }

    @Override
    public void onDestroy() {
        removeListeners();
        super.onDestroy();
    }
}
