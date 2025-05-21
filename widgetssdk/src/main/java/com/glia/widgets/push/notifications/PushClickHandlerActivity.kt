package com.glia.widgets.push.notifications

import android.content.Intent
import android.os.Bundle
import com.glia.widgets.base.FadeTransitionActivity
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.ExtraKeys

internal class PushClickHandlerActivity : FadeTransitionActivity() {
    private val pushClickHandlerController by lazy { Dependencies.controllerFactory.pushClickHandlerController }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // About these flags and overall why this activity has separate affinity etc. please refer to the official documentation:
        // https://developer.android.com/develop/ui/views/notifications/navigation#ExtendedNotification
        val appLauncherIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // We need launcher intent to run the app's default flow.
        if (appLauncherIntent != null) {
            pushClickHandlerController.handlePushClick(
                intent.getStringExtra(ExtraKeys.PN_QUEUE_ID),
                intent.getStringExtra(ExtraKeys.PN_VISITOR_ID)!!
            )
            startActivity(appLauncherIntent)
        }

        finish()
    }

}
