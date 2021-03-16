package com.glia.widgets.screensharing;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.glia.widgets.notification.NotificationFactory;

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
        startForeground(SERVICE_ID, NotificationFactory.createScreenSharingNotification(this));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
