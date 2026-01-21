package com.glia.widgets

import android.content.Context
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
    val authorizationMethod: AuthorizationMethod?

    @JvmField
    val context: Context?

    @JvmField
    val region: Region?

    @JvmField
    val regionString: String?

    @JvmField
    val uiJsonRemoteConfig: String?

    @JvmField
    val companyName: String?

    @JvmField
    val enableBubbleOutsideApp: Boolean?

    @JvmField
    val enableBubbleInsideApp: Boolean?

    @JvmField
    val manualLocaleOverride: String?

    @JvmField
    val suppressPushNotificationsPermissionRequestDuringAuthentication: Boolean?

    init {
        // The Context can be an Activity that has its life cycle.
        // To prevent storing the link to an Activity that can be destroyed, we need to take the Application Context.
        // An Application object lives all the time when the application is active, so it is safe to store it.
        val applicationContext = builder.context?.applicationContext

        authorizationMethod = builder.authorizationMethod
        siteId = builder.siteId
        context = applicationContext
        region = builder.region
        regionString = builder.regionString
        uiJsonRemoteConfig = builder.uiJsonRemoteConfig
        companyName = builder.companyName
        enableBubbleOutsideApp = builder.enableBubbleOutsideApp
        enableBubbleInsideApp = builder.enableBubbleInsideApp
        manualLocaleOverride = builder.manualLocaleOverride
        suppressPushNotificationsPermissionRequestDuringAuthentication = builder.suppressPushNotificationsPermissionRequestDuringAuthentication
    }

    /**
     * Defines regions that can be applied to [GliaWidgetsConfig].
     */
    object Regions {
        const val US = "us"
        const val EU = "eu"
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
        var authorizationMethod: AuthorizationMethod? = null
            private set
        var context: Context? = null
            private set
        var region: Region? = null
            private set
        var regionString: String? = null
            private set
        var baseDomain: String? = null
            private set
        var uiJsonRemoteConfig: String? = null
            private set
        var companyName: String? = null
            private set
        var enableBubbleOutsideApp: Boolean? = null
            private set
        var enableBubbleInsideApp: Boolean? = null
            private set
        var manualLocaleOverride: String? = null
            private set
        var suppressPushNotificationsPermissionRequestDuringAuthentication: Boolean? = null
            private set

        /**
         * @param siteId - your site ID
         * @return Builder instance
         */
        fun setSiteId(siteId: String): Builder {
            this.siteId = siteId
            return this
        }

        /**
         * @param context - your application context
         * @return Builder instance
         */
        fun setContext(context: Context): Builder {
            this.context = context
            return this
        }

        fun setRegion(region: Region): Builder {
            this.region = region
            return this
        }

        /**
         * @param region Region in which the site is created.
         * One of [Regions].
         * @return Builder instance
         * @deprecated Will be removed in version 4.0.0.
         */
        @Deprecated("Use {@link #setRegion(Region)}")
        fun setRegion(region: String): Builder {
            this.regionString = region
            return this
        }

        /**
         * @hidden
         * @param baseDomain Base domain to be used.
         * @return Builder instance
         * @deprecated Will be removed in version 4.0.0.
         */
        @Deprecated("Use {@link #setRegion(Region)}")
        fun setBaseDomain(baseDomain: String): Builder {
            this.baseDomain = baseDomain
            return this
        }

        /**
         * @param siteApiKey - your site API key
         * @return Builder instance
         * @deprecated Will be removed in version 4.0.0.
         */
        @Deprecated(
            "Use {@link #setAuthorizationMethod(AuthorizationMethod.UserApiKey)}",
            ReplaceWith(
                "setAuthorizationMethod(AuthorizationMethod.UserApiKey(id, secret))",
                "com.glia.widgets.AuthorizationMethod"
            )
        )
        fun setSiteApiKey(siteApiKey: com.glia.androidsdk.SiteApiKey): Builder {
            return setSiteApiKey(siteApiKey.toWidgetType())
        }

        /**
         * @param siteApiKey - your site API key
         * @return Builder instance
         * @deprecated Will be removed in version 4.0.0.
         */
        @Deprecated(
            "Use {@link #setAuthorizationMethod(AuthorizationMethod.UserApiKey)}",
            ReplaceWith(
                "setAuthorizationMethod(AuthorizationMethod.UserApiKey(id, secret))",
                "com.glia.widgets.AuthorizationMethod"
            )
        )
        fun setSiteApiKey(siteApiKey: AuthorizationMethod.SiteApiKey): Builder {
            this.authorizationMethod = siteApiKey
            return this
        }

        /**
         * @param authorizationMethod - your API key
         * @return Builder instance
         *
         * Example:
         * ```
         * .setAuthorizationMethod(
         *     AuthorizationMethod.UserApiKey("API_KEY_ID", "API_KEY_SECRET")
         * )
         * ```
         */
        fun setAuthorizationMethod(authorizationMethod: AuthorizationMethod): Builder {
            this.authorizationMethod = authorizationMethod
            return this
        }

        @Deprecated("All the permissions are requested automatically by the SDK when needed.")
        fun setRequestCode(requestCode: Int): Builder {
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
         * @param manualLocaleOverride - manual locale override if you wish not to use the default locale
         * @return Builder instance
         */
        fun setManualLocaleOverride(manualLocaleOverride: String?): Builder {
            this.manualLocaleOverride = manualLocaleOverride
            return this
        }

        /**
         * @param suppressPushNotificationsPermissionRequestDuringAuthentication - suppress push notifications permission request during authentication
         * @return Builder instance
         */
        fun setSuppressPushNotificationsPermissionRequestDuringAuthentication(suppressPushNotificationsPermissionRequestDuringAuthentication: Boolean): Builder {
            this.suppressPushNotificationsPermissionRequestDuringAuthentication = suppressPushNotificationsPermissionRequestDuringAuthentication
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
