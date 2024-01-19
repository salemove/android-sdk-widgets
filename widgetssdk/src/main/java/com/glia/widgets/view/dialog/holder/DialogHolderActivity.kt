package com.glia.widgets.view.dialog.holder

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

internal class DialogHolderActivity : AppCompatActivity() {

    override fun finish() {
        overridePendingTransition(0, 0)
        super.finish()
    }

    companion object {
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, DialogHolderActivity::class.java))
            activity.overridePendingTransition(0, android.R.anim.fade_out)
        }
    }

}
