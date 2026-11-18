package com.glia.integratorcompat

import android.app.Activity
import com.glia.widgets.AuthorizationMethod
import com.glia.widgets.GliaWidgets
import com.glia.widgets.GliaWidgetsConfig
import com.glia.widgets.Region

// The app is only built, never run, so the configuration values are placeholders
object KotlinEntry {
    fun startChat(activity: Activity) {
        val config = GliaWidgetsConfig.Builder()
            .setAuthorizationMethod(AuthorizationMethod.UserApiKey("api-key-id", "api-key-secret"))
            .setSiteId("site-id")
            .setRegion(Region.US)
            .setContext(activity.applicationContext)
            .build()

        GliaWidgets.init(
            config,
            { GliaWidgets.getEngagementLauncher(listOf("queue-id")).startChat(activity) },
            { }
        )
    }
}
