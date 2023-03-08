package com.glia.exampleapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.glia.androidsdk.SiteApiKey;
import com.glia.widgets.GliaWidgetsConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *Helper class to obtain Glia Config params from deep-link or preferences.
 *
 *for setup with secret link must be
 *      glia://widgets/secret?site_id={site_id}&api_key_secret={api_key_secret}&api_key_id={api_key_id}&queue_id={queue_id}&visitor_context_asset_id={visitor_context_asset_id}
 *      where all query params are mandatory except visitor_context_asset_id
 */
public class GliaWidgetsConfigManager {
    private static final String SECRET_KEY = "secret";

    private static final String SITE_ID_KEY = "site_id";
    private static final String API_KEY_SECRET_KEY = "api_key_secret";
    private static final String API_KEY_ID_KEY = "api_key_id";
    private static final String QUEUE_ID_KEY = "queue_id";
    public static final String VISITOR_CONTEXT_ASSET_ID_KEY = "visitor_context_asset_id";

    private static final String REGION_BETA = "beta";
    private static final String REGION_ACCEPTANCE = "acceptance";

    @NonNull
    public static GliaWidgetsConfig obtainConfigFromDeepLink(@NonNull Uri data, @NonNull Context applicationContext) {
        saveQueueIdToPrefs(data, applicationContext);
        saveVisitorContextAssetIdIfPresent(data, applicationContext);
        saveSiteIdToPrefs(data, applicationContext);

//        We're not checking availability for every query param separately because this is for acceptance tests and we assume that link would be correct
        if (SECRET_KEY.equals(data.getLastPathSegment())) {
            saveSiteApiKeyAuthToPrefs(data, applicationContext);
            return obtainConfigFromSecretDeepLink(data, applicationContext);
        } else {
            throw new RuntimeException("deep link must start with \"glia://widgets/secret\"");
        }
    }

    private static void saveVisitorContextAssetIdIfPresent(Uri data, Context applicationContext) {
        String visitorContextAssetId = data.getQueryParameter(VISITOR_CONTEXT_ASSET_ID_KEY);

        if (visitorContextAssetId == null) return;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);

        sharedPreferences.edit().putString(applicationContext.getString(R.string.pref_context_asset_id), visitorContextAssetId).apply();
    }

    private static void saveQueueIdToPrefs(@NonNull Uri data, @NonNull Context applicationContext) {
        String queueId = data.getQueryParameter(QUEUE_ID_KEY);

        if (queueId == null) return;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);

        sharedPreferences.edit().putString(applicationContext.getString(R.string.pref_queue_id), queueId).apply();
    }

    private static void saveSiteIdToPrefs(@NonNull Uri data, @NonNull Context applicationContext) {
        String siteId = data.getQueryParameter(SITE_ID_KEY);

        if (siteId == null) return;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);

        sharedPreferences.edit().putString(applicationContext.getString(R.string.pref_site_id), siteId).apply();
    }

    private static void saveSiteApiKeyAuthToPrefs(@NonNull Uri data, @NonNull Context applicationContext) {
        String apiKeyId = data.getQueryParameter(API_KEY_ID_KEY);
        String apiKeySecret = data.getQueryParameter(API_KEY_SECRET_KEY);

        if (apiKeyId == null || apiKeySecret == null) return;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);

        sharedPreferences.edit()
                .putString(applicationContext.getString(R.string.pref_api_key_id), apiKeyId)
                .putString(applicationContext.getString(R.string.pref_api_key_secret), apiKeySecret)
                .apply();
    }

    @NonNull
    private static GliaWidgetsConfig obtainConfigFromSecretDeepLink(@NonNull Uri data, @NonNull Context applicationContext) {
        return new GliaWidgetsConfig.Builder()
                .setSiteApiKey(new SiteApiKey(data.getQueryParameter(API_KEY_ID_KEY), data.getQueryParameter(API_KEY_SECRET_KEY)))
                .setSiteId(data.getQueryParameter(SITE_ID_KEY))
                .setRegion(REGION_ACCEPTANCE)
                .setContext(applicationContext)
                .build();
    }

    @NonNull
    public static GliaWidgetsConfig createDefaultConfig(@NonNull Context applicationContext) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        return configWithSiteApiKeyAuth(sharedPreferences, applicationContext);
    }

    @NonNull
    private static GliaWidgetsConfig configWithSiteApiKeyAuth(@NonNull SharedPreferences preferences, @NonNull Context applicationContext) {
        String apiKeyId = preferences.getString(applicationContext.getString(R.string.pref_api_key_id), applicationContext.getString(R.string.glia_api_key_id));
        String apiKeySecret = preferences.getString(applicationContext.getString(R.string.pref_api_key_secret), applicationContext.getString(R.string.glia_api_key_secret));
        String siteId = preferences.getString(applicationContext.getString(R.string.pref_site_id), applicationContext.getString(R.string.site_id));


        return new GliaWidgetsConfig.Builder()
                .setSiteApiKey(new SiteApiKey(apiKeyId, apiKeySecret))
                .setSiteId(siteId)
                .setRegion(REGION_BETA)
                .setContext(applicationContext)
                .setUiJsonRemoteConfig(getRawResource(applicationContext, R.raw.global_colors)) // TODO MOB-1919
                .build();
    }

    // for testing the remote theme configurations JSON
    private static String getRawResource(Context context, int resource) {
        String res = null;
        InputStream is = context.getResources().openRawResource(resource);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = new byte[1];
        try {
            while ( is.read(b) != -1 ) {
                baos.write(b);
            }
            res = baos.toString();
            is.close();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

}
