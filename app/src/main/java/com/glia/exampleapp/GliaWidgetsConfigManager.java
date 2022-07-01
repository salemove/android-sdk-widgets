package com.glia.exampleapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.glia.androidsdk.SiteApiKey;
import com.glia.exampleapp.auth.AuthorizationType;
import com.glia.widgets.GliaWidgetsConfig;

/**
 *Helper class to obtain Glia Config params from deep-link or preferences.
 *
 *for setup with app_token link must be -
 *      glia://widgets/token?site_id={site_id}&app_token={app_token}&queue_id={queue_id}&visitor_context_asset_id={visitor_context_asset_id}
 *      where all query params are mandatory except visitor_context_asset_id
 *
 *for setup with secret link must be
 *      glia://widgets/secret?site_id={site_id}&api_key_secret={api_key_secret}&api_key_id={api_key_id}&queue_id={queue_id}&visitor_context_asset_id={visitor_context_asset_id}
 *      where all query params are mandatory except visitor_context_asset_id
 */
public class GliaWidgetsConfigManager {
    private static final String TOKEN_KEY = "token";
    private static final String SECRET_KEY = "secret";

    private static final String SITE_ID_KEY = "site_id";
    private static final String APP_TOKEN_KEY = "app_token";
    private static final String API_KEY_SECRET_KEY = "api_key_secret";
    private static final String API_KEY_ID_KEY = "api_key_id";
    private static final String QUEUE_ID_KEY = "queue_id";

    private static final String REGION_BETA = "beta";
    private static final String REGION_ACCEPTANCE = "acceptance";

    public static final String VISITOR_CONTEXT_ASSET_ID_KEY = "visitor_context_asset_id";

    @NonNull
    public static GliaWidgetsConfig obtainConfig(@NonNull Intent intent, @NonNull Context applicationContext) {
        if (intent.getData() == null) return createDefaultConfig(applicationContext);

        return obtainConfigFromDeepLink(intent.getData(), applicationContext);
    }

    @NonNull
    public static GliaWidgetsConfig obtainConfigFromDeepLink(@NonNull Uri data, @NonNull Context applicationContext) {
        saveQueueIdToPrefs(data, applicationContext);
        saveVisitorContextAssetIdIfPresent(data, applicationContext);
        saveSiteIdToPrefs(data, applicationContext);

//        We're not checking availability for every query param separately because this is for acceptance tests and we assume that link would be correct
        if (SECRET_KEY.equals(data.getLastPathSegment())) {
            saveSiteApiKeyAuthToPrefs(data, applicationContext);
            return obtainConfigFromSecretDeepLink(data, applicationContext);
        } else if (TOKEN_KEY.equals(data.getLastPathSegment())) {
            saveAppTokenAuthToPrefs(data, applicationContext);
            return obtainConfigFromAppTokenDeepLink(data, applicationContext);
        } else {
            throw new RuntimeException("deep link must start with \"glia://widgets/app_token\" or \"glia://widgets/secret\"");
        }
    }

    private static void saveVisitorContextAssetIdIfPresent(Uri data, Context applicationContext) {
        String visitorContextAssetId = data.getQueryParameter(VISITOR_CONTEXT_ASSET_ID_KEY);

        if (visitorContextAssetId == null) return;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);

        sharedPreferences.edit().putString(VISITOR_CONTEXT_ASSET_ID_KEY, visitorContextAssetId).apply();
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

    private static void saveAppTokenAuthToPrefs(@NonNull Uri data, @NonNull Context applicationContext) {
        String appToken = data.getQueryParameter(APP_TOKEN_KEY);

        if (appToken == null) return;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);

        sharedPreferences.edit()
                .putString(applicationContext.getString(R.string.pref_app_token), appToken)
                .putInt(applicationContext.getString(R.string.pref_authorization_type), AuthorizationType.APP_TOKEN)
                .apply();
    }

    private static void saveSiteApiKeyAuthToPrefs(@NonNull Uri data, @NonNull Context applicationContext) {
        String apiKeyId = data.getQueryParameter(API_KEY_ID_KEY);
        String apiKeySecret = data.getQueryParameter(API_KEY_SECRET_KEY);

        if (apiKeyId == null || apiKeySecret == null) return;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);

        sharedPreferences.edit()
                .putString(applicationContext.getString(R.string.pref_api_key_id), apiKeyId)
                .putString(applicationContext.getString(R.string.pref_api_key_secret), apiKeySecret)
                .putInt(applicationContext.getString(R.string.pref_authorization_type), AuthorizationType.SITE_API_KEY)
                .apply();
    }

    @NonNull
    @SuppressWarnings("deprecation")
    private static GliaWidgetsConfig obtainConfigFromAppTokenDeepLink(@NonNull Uri data, @NonNull Context applicationContext) {
        return new GliaWidgetsConfig.Builder()
                .setAppToken(data.getQueryParameter(APP_TOKEN_KEY))
                .setSiteId(data.getQueryParameter(SITE_ID_KEY))
                .setRegion(REGION_ACCEPTANCE)
                .setContext(applicationContext)
                .build();
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
        int authorizationType = sharedPreferences.getInt(applicationContext.getString(R.string.pref_authorization_type), AuthorizationType.DEFAULT);

        if (authorizationType == AuthorizationType.SITE_API_KEY)
            return configWithSiteApiKeyAuth(sharedPreferences, applicationContext);
        else
            return configWithAppTokenAuth(sharedPreferences, applicationContext);
    }

    @NonNull
    @SuppressWarnings("deprecation")
    private static GliaWidgetsConfig configWithAppTokenAuth(@NonNull SharedPreferences preferences, @NonNull Context applicationContext) {
        String appToken = preferences.getString(applicationContext.getString(R.string.pref_app_token), applicationContext.getString(R.string.app_token));
        String siteId = preferences.getString(applicationContext.getString(R.string.pref_site_id), applicationContext.getString(R.string.site_id));
        return new GliaWidgetsConfig.Builder()
                .setAppToken(appToken)
                .setSiteId(siteId)
                .setRegion(REGION_BETA)
                .setContext(applicationContext)
                .build();
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
                .build();
    }

}
