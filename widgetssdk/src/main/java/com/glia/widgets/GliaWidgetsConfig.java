package com.glia.widgets;

import android.content.Context;

import androidx.annotation.Nullable;

import com.glia.androidsdk.SiteApiKey;
import com.glia.androidsdk.screensharing.ScreenSharing;

import org.jetbrains.annotations.NotNull;

/**
 * Configurations used to initialize Glia SDK
 *
 * @see GliaWidgetsConfig.Builder
 */
public class GliaWidgetsConfig {

    public static final boolean DEFAULT_USE_OVERLAY = true;
    public static final ScreenSharing.Mode DEFAULT_SCREEN_SHARING_MODE = ScreenSharing.Mode.UNBOUNDED;

    private final String siteId;
    private final SiteApiKey siteApiKey;
    private final Context context;
    private final String region;
    private final String baseDomain;
    private final int requestCode;
    private final String uiJsonRemoteConfig;
    private final String companyName;
    private final ScreenSharing.Mode screenSharingMode;
    private final boolean useOverlay;
    private final UiTheme uiTheme;
    private final String manualLocaleOverride;

    private GliaWidgetsConfig(Builder builder) {
        this.siteApiKey = builder.siteApiKey;
        this.siteId = builder.siteId;
        this.context = builder.context;
        this.region = builder.region;
        this.baseDomain = builder.baseDomain;
        this.requestCode = builder.requestCode;
        this.uiJsonRemoteConfig = builder.uiJsonRemoteConfig;
        this.companyName = builder.companyName;
        this.screenSharingMode = builder.screenSharingMode != null ? builder.screenSharingMode : DEFAULT_SCREEN_SHARING_MODE;
        this.useOverlay = builder.useOverlay != null ? builder.useOverlay : DEFAULT_USE_OVERLAY;
        this.uiTheme = builder.uiTheme;
        this.manualLocaleOverride = builder.manualLocaleOverride;
    }

    public String getSiteId() {
        return siteId;
    }

    public Context getContext() {
        return context;
    }

    public String getRegion() {
        return region;
    }

    public String getBaseDomain() {
        return baseDomain;
    }

    public SiteApiKey getSiteApiKey() {
        return siteApiKey;
    }

    @Nullable
    public String getUiJsonRemoteConfig() {
        return uiJsonRemoteConfig;
    }

    public String getCompanyName() {
        return companyName;
    }

    public ScreenSharing.Mode getScreenSharingMode() {
        return screenSharingMode;
    }

    public boolean isUseOverlay() {
        return useOverlay;
    }

    public UiTheme getUiTheme() {
        return uiTheme;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public String getManualLocaleOverride() {
        return manualLocaleOverride;
    }

    /**
     * Defines regions that can be applied to {@link GliaWidgetsConfig}.
     */
    public static class Regions {
        public static String US = "us";
        public static String EU = "eu";
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
     * <li>Site Api Key Id</li>
     * <li>Site Api Key Secret</li>
     * <li>Site ID</li>
     * <li>Region</li>
     * <li>Context</li>
     * </ul>
     * </p>
     * <p>
     * For CallVisualizer implementation <b>companyName</b> is also required information
     * </p>
     * <p>
     * <b>Usage example:</b>
     * <pre>
     * <code>
     * GliaBuildConfig gliaBuildConfig = new GliaBuildConfig.Builder(
     *   .setSiteApiKey(new SiteApiKey(SITE_API_KEY_ID, SITE_API_KEY_SECRET))
     *   .setSiteId("SITE_ID")
     *   .setRegion(Regions.US)
     *   .setContext(getApplicationContext())
     *   .setCompanyName("Company Name")
     *   .build();
     * </code>
     * </pre>
     * </p>
     */
    public static class Builder {
        String siteId;
        SiteApiKey siteApiKey;
        Context context;
        String region;
        String baseDomain;
        int requestCode;
        String uiJsonRemoteConfig;
        String companyName;
        ScreenSharing.Mode screenSharingMode;
        Boolean useOverlay;
        UiTheme uiTheme;
        String manualLocaleOverride;

        public Builder() {
            requestCode = 45554442;
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
         * @hidden
         * @param baseDomain Base domain to be used.
         * @return Builder instance
         */
        public Builder setBaseDomain(String baseDomain) {
            this.baseDomain = baseDomain;
            return this;
        }

        public Builder setSiteApiKey(SiteApiKey siteApiKey) {
            this.siteApiKey = siteApiKey;
            return this;
        }

        public Builder setRequestCode(int requestCode) {
            this.requestCode = requestCode;
            return this;
        }

        public Builder setUiJsonRemoteConfig(@Nullable String uiJsonRemoteConfig) {
            this.uiJsonRemoteConfig = uiJsonRemoteConfig;
            return this;
        }

        /**
         * @param companyName - your company name
         * @return Builder instance
         */
        public Builder setCompanyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        /**
         * @param screenSharingMode - Screen sharing mode, either UNBOUND(default) or APP_BOUND
         * @return Builder instance
         */
        public Builder setScreenSharingMode(@Nullable ScreenSharing.Mode screenSharingMode) {
            this.screenSharingMode = screenSharingMode;
            return this;
        }

        /**
         * @param useOverlay - Is it allowed to overlay the application
         * @return Builder instance
         */
        public Builder setUseOverlay(boolean useOverlay) {
            this.useOverlay = useOverlay;
            return this;
        }

        /**
         * @param uiTheme - uiTheme resource for UI configuration
         * @return Builder instance
         */
        public Builder setUiTheme(UiTheme uiTheme) {
            this.uiTheme = uiTheme;
            return this;
        }

        /**
         * @param manualLocaleOverride - manual locale override if you wish not to use the default locale
         * @return Builder instance
         */
        public Builder setManualLocaleOverride(String manualLocaleOverride) {
            this.manualLocaleOverride = manualLocaleOverride;
            return this;
        }

        /**
         * Builds the final configurations
         *
         * @return Glia SDK configurations
         */
        public GliaWidgetsConfig build() {
            return new GliaWidgetsConfig(this);
        }
    }
}
