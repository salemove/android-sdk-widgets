package com.glia.widgets;

import android.content.Context;

/**
 * Configurations used to initialize Glia SDK
 *
 * @see GliaWidgetsConfig.Builder
 */
public class GliaWidgetsConfig {
    private final String appToken;
    private final String siteId;
    private final Context context;
    private final String region;
    private final int requestCode;

    /**
     * @deprecated Deprecated since SDK version 1.6.5. Please use {@link GliaWidgetsConfig#GliaWidgetsConfig(String, String, Context, String, int)} instead.
     */
    @Deprecated
    public GliaWidgetsConfig(String appToken, String apiToken, String siteId, Context context, String region, int requestCode) {
        this(appToken, siteId, context, region, requestCode);
    }

    public GliaWidgetsConfig(String appToken, String siteId, Context context, String region, int requestCode) {
        this.appToken = appToken;
        this.siteId = siteId;
        this.context = context;
        this.region = region;
        this.requestCode = requestCode;
    }

    /**
     * Defines regions that can be applied to {@link GliaWidgetsConfig}.
     */
    public static class Regions {
        public static String US = "us";
        public static String EU = "eu";
    }

    public String getSiteId() {
        return siteId;
    }

    public String getAppToken() {
        return appToken;
    }

    public Context getContext() {
        return context;
    }

    public String getRegion() {
        return region;
    }

    /**
     * @deprecated API token is no longer needed for SDK to function correctly.
     * Deprecated since SDK version 1.6.5
     */
    @Deprecated
    public String getApiToken() {
        return null;
    }

    public int getRequestCode() {
        return requestCode;
    }

    /**
     * Glia configuration builder.
     *
     * <p>Use it to build {@link GliaWidgetsConfig} and initialize {@link GliaWidgets#init(GliaWidgetsConfig) Glia SDK}.
     * To get your SDK configuration contact your success manager</p>
     *
     * <p>
     * Required information is:
     * <ul>
     * <li>APP token</li>
     * <li>Site ID</li>
     * <li>Region</li>
     * <li>Context</li>
     * </ul>
     * </p>
     * <p>
     * <b>Usage example:</b>
     * <pre>
     * <code>
     * GliaBuildConfig gliaBuildConfig = new GliaBuildConfig.Builder(
     *   .setAppToken("APP_TOKEN")
     *   .setSiteId("SITE_ID")
     *   .setRegion(Regions.US)
     *   .setContext(getApplicationContext())
     *   .build();
     * </code>
     * </pre>
     */
    public static class Builder {
        String appToken;
        String siteId;
        Context context;
        String region;
        int requestCode = 45554442;

        /**
         * @param appToken - your APP token
         * @return Builder instance
         */
        public Builder setAppToken(String appToken) {
            this.appToken = appToken;
            return this;
        }

        /**
         * @deprecated API token is no longer needed for SDK to function correctly.
         * Deprecated since SDK version 1.6.5
         */
        @Deprecated
        public Builder setApiToken(String apiToken) {
            return this;
        }

        /**
         * @param siteId - your site ID
         * @return Builder instance
         */
        public Builder setSiteId(String siteId) {
            this.siteId = siteId;
            return this;
        }

        /**
         * @param context - your application context
         * @return Builder instance
         */
        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        /**
         * @param region Region in which the site is created.
         *               One of {@link Regions}.
         * @return Builder instance
         */
        public Builder setRegion(String region) {
            this.region = region;
            return this;
        }

        /**
         * Builds the final configurations
         *
         * @return Glia SDK configurations
         */
        public GliaWidgetsConfig build() {
            return new GliaWidgetsConfig(appToken, siteId, context, region, requestCode);
        }
    }
}
