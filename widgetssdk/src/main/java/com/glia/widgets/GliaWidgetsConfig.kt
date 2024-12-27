package com.glia.widgets

import android.content.Context
import com.glia.androidsdk.SiteApiKey
import com.glia.androidsdk.screensharing.ScreenSharing
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

/**
 * Configurations used to initialize Glia SDK
 *
 * @see GliaWidgetsConfig.Builder
 */
class GliaWidgetsConfig private constructor(builder: Builder) {
    @JvmField
    val siteId: String?

    @JvmField
    val siteApiKey: SiteApiKey?

    @JvmField
    val context: Context?

    @JvmField
    val region: String?

    @JvmField
    val baseDomain: String?
    val requestCode: Int

    @JvmField
    val uiJsonRemoteConfig: String?

    @JvmField
    val companyName: String?

    @JvmField
    val screenSharingMode: ScreenSharing.Mode?

    @JvmField
    val enableBubbleOutsideApp: Boolean?

    @JvmField
    val enableBubbleInsideApp: Boolean?

    @JvmField
    val uiTheme: UiTheme?

    @JvmField
    val manualLocaleOverride: String?

    init {
        siteApiKey = builder.siteApiKey
        siteId = builder.siteId
        context = builder.context
        region = builder.region
        baseDomain = builder.baseDomain
        requestCode = builder.requestCode
        uiJsonRemoteConfig = builder.uiJsonRemoteConfig
        companyName = builder.companyName
        screenSharingMode = builder.screenSharingMode
        enableBubbleOutsideApp = builder.enableBubbleOutsideApp
        enableBubbleInsideApp = builder.enableBubbleInsideApp
        uiTheme = builder.uiTheme
        manualLocaleOverride = builder.manualLocaleOverride
    }

    /**
     * Defines regions that can be applied to [GliaWidgetsConfig].
     */
    object Regions {
        var US = "us"
        var EU = "eu"
    }

    /**
     * Glia configuration builder.
     *
     *
     * Use it to build [GliaWidgetsConfig] and initialize [Glia SDK][GliaWidgets.init].
     * To get your SDK configuration contact your success manager
     *
     *
     *
     * Required information is:
     *
     *  * Site Api Key Id
     *  * Site Api Key Secret
     *  * Site ID
     *  * Region
     *  * Context
     *
     *
     *
     *
     * For CallVisualizer implementation **companyName** is also required information
     *
     *
     *
     * **Usage example:**
     * <pre>
     * `
     * GliaBuildConfig gliaBuildConfig = new GliaBuildConfig.Builder(
     * .setSiteApiKey(new SiteApiKey(SITE_API_KEY_ID, SITE_API_KEY_SECRET))
     * .setSiteId("SITE_ID")
     * .setRegion(Regions.US)
     * .setContext(getApplicationContext())
     * .setCompanyName("Company Name")
     * .build();
    ` *
    </pre> *
     *
     */
    class Builder {
        var siteId: String? = null
            private set
        var siteApiKey: SiteApiKey? = null
            private set
        var context: Context? = null
            private set
        var region: String? = null
            private set
        var baseDomain: String? = null
            private set
        var requestCode = 45554442
            private set
        var uiJsonRemoteConfig: String? = null
            private set
        var companyName: String? = null
            private set
        var screenSharingMode: ScreenSharing.Mode? = null
            private set
        var enableBubbleOutsideApp: Boolean? = null
            private set
        var enableBubbleInsideApp: Boolean? = null
            private set
        var uiTheme: UiTheme? = null
            private set
        var manualLocaleOverride: String? = null
            private set

        /**
         * @param siteId - your site ID
         * @return Builder instance
         */
        fun setSiteId(siteId: String?): Builder {
            this.siteId = siteId
            return this
        }

        /**
         * @param context - your application context
         * @return Builder instance
         */
        fun setContext(context: Context?): Builder {
            this.context = context
            return this
        }

        /**
         * @param region Region in which the site is created.
         * One of [Regions].
         * @return Builder instance
         */
        fun setRegion(region: String?): Builder {
            this.region = region
            return this
        }

        /**
         * @hidden
         * @param baseDomain Base domain to be used.
         * @return Builder instance
         */
        fun setBaseDomain(baseDomain: String?): Builder {
            this.baseDomain = baseDomain
            return this
        }

        fun setSiteApiKey(siteApiKey: SiteApiKey?): Builder {
            this.siteApiKey = siteApiKey
            return this
        }

        fun setRequestCode(requestCode: Int): Builder {
            this.requestCode = requestCode
            return this
        }

        fun setUiJsonRemoteConfig(uiJsonRemoteConfig: String?): Builder {
            Logger.i(TAG, "Setting Unified UI Config")
            this.uiJsonRemoteConfig = uiJsonRemoteConfig
            return this
        }

        /**
         * @param companyName - your company name
         * @return Builder instance
         */
        fun setCompanyName(companyName: String?): Builder {
            this.companyName = companyName
            return this
        }

        /**
         * @param screenSharingMode - Screen sharing mode, either UNBOUND(default) or APP_BOUND
         * @return Builder instance
         */
        fun setScreenSharingMode(screenSharingMode: ScreenSharing.Mode?): Builder {
            this.screenSharingMode = screenSharingMode
            return this
        }

        /**
         * @param useOverlay - is it allowed to overlay the application
         * @return Builder instance
         * @deprecated Use [GliaWidgetsConfig.enableBubbleOutsideApp] and [GliaWidgetsConfig.enableBubbleInsideApp]
         */
        @Deprecated("Please use GliaWidgetsConfig.enableBubbleOutsideApp and GliaWidgetsConfig.enableBubbleInsideApp")
        fun setUseOverlay(useOverlay: Boolean): Builder {
            Logger.logDeprecatedMethodUse(TAG, "GliaWidgetsConfig.setUseOverlay()")
            this.enableBubbleOutsideApp = useOverlay
            this.enableBubbleInsideApp = useOverlay
            return this
        }

        /**
         * @param enableBubbleOutsideApp - is bubble enabled outside the app
         * @return Builder instance
         */
        fun enableBubbleOutsideApp(enableBubbleOutsideApp: Boolean): Builder {
            Logger.i(TAG, "Bubble: enable outside app $enableBubbleOutsideApp")
            this.enableBubbleOutsideApp = enableBubbleOutsideApp
            return this
        }

        /**
         * @param enableBubbleInsideApp - is bubble enabled inside the app
         * @return Builder instance
         */
        fun enableBubbleInsideApp(enableBubbleInsideApp: Boolean): Builder {
            Logger.i(TAG, "Bubble: enable inside app $enableBubbleInsideApp")
            this.enableBubbleInsideApp = enableBubbleInsideApp
            return this
        }

        /**
         * @param uiTheme - uiTheme resource for UI configuration
         * @return Builder instance
         */
        @Deprecated(
            "While UiTheme can still be used for UI customization, we strongly encourage adopting remote configurations(GliaWidgetsConfig.Builder.setUiJsonRemoteConfig). " +
                "The remote configurations approach is more versatile and better suited for future development."
        )
        fun setUiTheme(uiTheme: UiTheme?): Builder {
            this.uiTheme = uiTheme
            return this
        }

        /**
         * @param manualLocaleOverride - manual locale override if you wish not to use the default locale
         * @return Builder instance
         */
        fun setManualLocaleOverride(manualLocaleOverride: String?): Builder {
            this.manualLocaleOverride = manualLocaleOverride
            return this
        }

        /**
         * Builds the final configurations
         *
         * @return Glia SDK configurations
         */
        fun build(): GliaWidgetsConfig {
            return GliaWidgetsConfig(this)
        }
    }
}
