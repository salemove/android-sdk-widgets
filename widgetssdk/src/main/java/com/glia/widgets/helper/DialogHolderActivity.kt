package com.glia.widgets.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.glia.widgets.R
import com.glia.widgets.base.FadeTransitionActivity
import com.glia.widgets.base.GliaActivity
import com.glia.widgets.view.head.ActivityWatcherForChatHead
import kotlin.properties.Delegates

/**
 * Glia internal class.
 *
 * The manifest merger will automatically add this activity to the integrator's manifest file during compilation.
 *
 * This is a helper activity used to display Glia dialogs inside the integrator's app when the current activity
 * has no Material Design (`AppCompatActivity`) support required for Glia dialogs and to request permissions in case
 * the current activity does not support ActivityResultLauncher requests.
 */
internal class DialogHolderActivity : GliaActivity<DialogHolderView>, FadeTransitionActivity() {

    private var view: DialogHolderView by Delegates.notNull()

    override val gliaView: DialogHolderView
        get() = view

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = DialogHolderView(this)
        setContentView(view)
    }

    companion object {
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, DialogHolderActivity::class.java))
        }
    }

}

/**
 * Glia internal class.
 *
 * This is a view used to make the [DialogHolderActivity] recognizable by the services that draw the chat bubble.
 * @see [ActivityWatcherForChatHead.fetchGliaOrRootView]
 */
internal class DialogHolderView(context: Context) : View(context) {
    init {
        id = R.id.dialog_holder_activity_view_id
    }
}
