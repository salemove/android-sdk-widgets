package com.glia.widgets.view.head

import android.app.Activity
import android.view.View
import com.glia.widgets.GliaWidgets
import com.glia.widgets.R
import com.glia.widgets.base.BaseActivityWatcher
import com.glia.widgets.call.CallActivity
import com.glia.widgets.call.Configuration
import com.glia.widgets.callvisualizer.EndScreenSharingView
import com.glia.widgets.chat.ChatView
import com.glia.widgets.di.Dependencies
import com.glia.widgets.filepreview.ui.FilePreviewView
import com.glia.widgets.helper.Utils
import com.glia.widgets.view.head.controller.ActivityWatcherForChatHeadContract
import com.glia.widgets.view.unifiedui.extensions.wrapWithMaterialThemeOverlay
import java.lang.ref.WeakReference

internal class ActivityWatcherForChatHead(
    val controller: ActivityWatcherForChatHeadContract.Controller
) : BaseActivityWatcher(), ActivityWatcherForChatHeadContract.Watcher {

    init {
        controller.setWatcher(this)
    }

    /**
     * Returns last activity that called [Activity.onResume], but didn't call [Activity.onPause] yet
     * @return Currently resumed activity.
     */
    private var resumedActivity: WeakReference<Activity?> = WeakReference(null)


    override fun onActivityResumed(activity: Activity) {
        resumedActivity = WeakReference(activity)
        controller.onActivityResumed(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        controller.onActivityPaused()
        resumedActivity.clear()
    }

    override fun fetchGliaOrRootView(): View? {
        return resumedActivity.get()?.let {
            return it.findViewById(R.id.call_view)
                ?: it.findViewById<FilePreviewView>(R.id.file_preview_view)
                ?: it.findViewById<ChatView>(R.id.chat_view)
                ?: it.findViewById<EndScreenSharingView>(R.id.screen_sharing_screen_view)
                ?: it.findViewById(android.R.id.content)
                ?: it.window.decorView.findViewById(android.R.id.content)
        }
    }

    override fun openCallActivity() {
        resumedActivity.get()?.let {
            val contextWithStyle = it.wrapWithMaterialThemeOverlay()
            val intent = CallActivity.getIntent(contextWithStyle,
                getConfigurationBuilder().setMediaType(Utils.toMediaType(GliaWidgets.MEDIA_TYPE_VIDEO))
                    .setIsUpgradeToCall(true)
                    .build())
            contextWithStyle.startActivity(intent)
        }
    }

    private fun getConfigurationBuilder(): Configuration.Builder {
        val configuration = Dependencies.getSdkConfigurationManager()?.createWidgetsConfiguration()
        return Configuration.Builder().setWidgetsConfiguration(configuration)
    }
}
