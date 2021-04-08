package com.glia.widgets.screensharing;

import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import com.glia.widgets.R;
import com.glia.widgets.notification.NotificationFactory;

import java.util.function.Consumer;

import static com.glia.widgets.notification.NotificationFactory.NOTIFICATION_SCREEN_SHARING_CHANNEL_ID;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MediaProjectionService extends Service {

    private static final String TAG = "MediaProjectionService";

    private static final int SERVICE_ID = 123;

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
        // Foreground services have to display a notification to the user when they are running
        registerNotificationsChannel(NOTIFICATION_SCREEN_SHARING_CHANNEL_ID, getString(R.string.notification_screen_sharing_channel_name));

        // To request that your service run in the foreground, call startForeground().
        // This method takes two parameters: an integer that uniquely identifies the notification and the Notification for the status bar.
        startForeground(SERVICE_ID, NotificationFactory.createScreenSharingNotification(this));
    }

    private void registerNotificationsChannel(String channelId, CharSequence channelName) {
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);

        NotificationManager manager = (NotificationManager) getSystemService(NotificationManager.class);
        manager.createNotificationChannel(notificationChannel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setupListeners();
        return Service.START_STICKY;
    }

    private Consumer<OmnicoreEngagement> omnicoreEngagementStartListener = (engagement) -> {
        engagement.on(Engagement.Events.END, (Runnable) this::stopSelf);
    };
    private Consumer<OmnibrowseEngagement> omnibrowseEngagementStartListener = (engagement) -> {
        engagement.on(Engagement.Events.END, (Runnable) this::stopSelf);
    };

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
