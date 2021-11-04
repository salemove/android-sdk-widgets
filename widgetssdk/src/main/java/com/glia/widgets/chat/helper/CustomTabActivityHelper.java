package com.glia.widgets.chat.helper;

import android.app.Activity;
import android.net.Uri;
import android.webkit.URLUtil;

import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsServiceConnection;

public class CustomTabActivityHelper implements ServiceConnectionCallback {

    private CustomTabsClient client;
    private CustomTabsServiceConnection connection;

    public static void openCustomTab(Activity activity, String url, int primaryColor, int secondaryColor) {
        String packageName = CustomTabsHelper.getPackageNameToUse(activity);

        CustomTabColorSchemeParams defaultColors = new CustomTabColorSchemeParams.Builder()
                .setToolbarColor(primaryColor)
                .setSecondaryToolbarColor(secondaryColor)
                .build();

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setDefaultColorSchemeParams(defaultColors);
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.setPackage(packageName);

        String finalUrl;

        if (URLUtil.isHttpsUrl(url) || URLUtil.isHttpUrl(url)) {
            finalUrl = url;
        } else {
            finalUrl = "https://" + url;
        }

        customTabsIntent.launchUrl(activity, Uri.parse(finalUrl));
    }

    public static boolean hasSupportedBrowser(Activity activity) {
        return CustomTabsHelper.getPackageNameToUse(activity) != null;
    }

    public void bindCustomTabsService(Activity activity) {
        if (client != null) return;

        String packageName = CustomTabsHelper.getPackageNameToUse(activity);
        if (packageName == null) return;

        connection = new ServiceConnection(this);
        CustomTabsClient.bindCustomTabsService(activity, packageName, connection);
    }

    public void unbindCustomTabsService(Activity activity) {
        if (connection == null) return;
        activity.unbindService(connection);
        client = null;
        connection = null;
    }

    @Override
    public void onServiceConnected(CustomTabsClient client) {
        this.client = client;
        client.warmup(0L);
    }

    @Override
    public void onServiceDisconnected() {
        client = null;
    }
}
