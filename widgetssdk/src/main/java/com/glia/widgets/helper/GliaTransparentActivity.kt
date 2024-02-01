package com.glia.widgets.helper

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

/**
 * Glia internal class.
 *
 * Will be automatically added to integrator Manifest by Manifest merger during compilation.
 *
 * This is a helper activity that should be invisible to visitor.
 * Used to request permissions and display dialogs when integrator's current activity is not compatible with AppCompatActivity
 */
internal class GliaTransparentActivity : AppCompatActivity() {

    override fun finish() {
        // Used to disable activity transition animation
//        overridePendingTransition(0, 0)
        println("GliaTransparentActivity#finish")
        super.finish()
    }

    companion object {
        /**
         * Use this method to start this Activity to prevent unnecessary animation
         */
        @JvmStatic
        fun start(activity: Activity) {
            val intent = Intent(activity, GliaTransparentActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            activity.startActivity(intent)
            println("GliaTransparentActivity#started")
            // Used to disable activity transition animation
//            activity.overridePendingTransition(0, android.R.anim.fade_out)
        }
    }
}
