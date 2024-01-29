package com.glia.widgets.view.dialog.holder

import android.app.Activity
import android.content.Intent
import com.glia.widgets.base.FadeTransitionActivity

/**
 * Glia internal class.
 *
 * It will be automatically added to the integrator's manifest file by the manifest merger during compilation.
 *
 * This is a helper activity used to display Glia dialogs inside the integrator's app when the current activity
 * has no Material Design (`AppCompatActivity`) support required for Glia dialogs.
 */
internal class DialogHolderActivity : FadeTransitionActivity() {

    companion object {
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, DialogHolderActivity::class.java))
        }
    }

}
