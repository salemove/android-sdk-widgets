package com.glia.widgets.push.notifications

import android.content.Intent
import android.os.Bundle
import com.glia.widgets.base.FadeTransitionActivity

internal class PushClickHandlerActivity : FadeTransitionActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // About these flags and overall why this activity has separate affinity etc. please refer to the official documentation:
        // https://developer.android.com/develop/ui/views/notifications/navigation#ExtendedNotification
        packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

            //TODO Storing intermediate data for launching SC transcript screen goes here

            startActivity(this)
        }

        finish()
    }

}
