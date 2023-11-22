package com.glia.widgets.view.dialog.holder

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

internal class DialogHolderActivity : AppCompatActivity() {

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    companion object {
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, DialogHolderActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
        }
    }

}
