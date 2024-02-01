package com.glia.widgets.view.dialog.holder

import android.app.Activity
import android.content.Intent
import com.glia.widgets.base.FadeTransitionActivity

internal class DialogHolderActivity : FadeTransitionActivity() {

    companion object {
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, DialogHolderActivity::class.java))
        }
    }

}
