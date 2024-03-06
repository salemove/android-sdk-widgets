package com.glia.widgets.helper

import android.app.Activity
import android.content.Intent
import com.glia.widgets.base.FadeTransitionActivity

/**
 * Glia internal class.
 *
 * The manifest merger will automatically add this activity to the integrator's manifest file during compilation.
 *
 * This is a helper activity used to display Glia dialogs inside the integrator's app when the current activity
 * has no Material Design (`AppCompatActivity`) support required for Glia dialogs and to request permissions in case
 * the current activity does not support ActivityResultLauncher requests.
 */
internal class DialogHolderActivity : FadeTransitionActivity() {

    companion object {
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, DialogHolderActivity::class.java))
        }
    }

}
