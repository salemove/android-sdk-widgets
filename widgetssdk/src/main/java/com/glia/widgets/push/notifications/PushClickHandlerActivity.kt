package com.glia.widgets.push.notifications

import android.os.Bundle
import com.glia.widgets.base.FadeTransitionActivity
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.ExtraKeys

internal class PushClickHandlerActivity : FadeTransitionActivity() {
    private val pushClickHandlerController by lazy { Dependencies.controllerFactory.pushClickHandlerController }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appLauncherIntent = packageManager.getLaunchIntentForPackage(packageName)

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
