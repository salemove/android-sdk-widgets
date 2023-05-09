package com.glia.widgets.view.head

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Pair
import com.glia.widgets.GliaWidgets
import com.glia.widgets.R
import com.glia.widgets.base.BaseActivityWatcher
import com.glia.widgets.call.CallActivity
import com.glia.widgets.call.Configuration
import com.glia.widgets.callvisualizer.EndScreenSharingActivity
import com.glia.widgets.callvisualizer.EndScreenSharingView
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.chat.ChatView
import com.glia.widgets.di.Dependencies
import com.glia.widgets.filepreview.ui.FilePreviewView
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.Utils
import com.glia.widgets.helper.hasChildOfType
import com.glia.widgets.messagecenter.MessageCenterView
import com.glia.widgets.view.head.controller.ActivityWatcherForChatHeadContract
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
    private var chatHeadLayout: WeakReference<ChatHeadLayout> = WeakReference(null)
    private var screenOrientation: Int? = null
    private var chatHeadViewPosition: Pair<Int, Int>? = null

    override fun onActivityResumed(activity: Activity) {
        resumedActivity = WeakReference(activity)
        controller.onActivityResumed()
        if (isSameOrientation(activity)) restoreBubblePosition()

    }

    override fun onActivityPaused(activity: Activity) {
        screenOrientation = activity.resources.configuration.orientation
        controller.onActivityPaused()
        removeChatHeadLayoutIfPresent()
        resumedActivity.clear()
    }

    private fun createChatHeadLayout(activity: Activity) {
        val chatHeadLayout = ChatHeadLayout(activity.baseContext)
        chatHeadLayout.layoutParams = ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        chatHeadLayout.setNavigationCallback(
            object : ChatHeadLayout.NavigationCallback {
                override fun onNavigateToEndScreenSharing() {
                    navigateToEndScreenSharing(resumedActivity.get())
                }

                override fun onNavigateToChat() {
                    navigateToChat(resumedActivity.get())
                }

                override fun onNavigateToCall() {
                    navigateToCall(resumedActivity.get())
                    if (controller.isFromCallScreen()) {
                        // Finish ChatActivity if bubble is tapped from ChatActivity
                        controller.resetFromCallScreen()
                        resumedActivity.get()?.finish()
                    }
                }
            }
        )
        chatHeadLayout.visibility = View.VISIBLE
        this.chatHeadLayout = WeakReference(chatHeadLayout)
    }

    override fun addChatHeadLayoutIfAbsent() {
        val activity = resumedActivity.get() ?: return
        val viewName = fetchGliaOrRootView()?.javaClass?.simpleName
        if (hasGliaView(activity) || !controller.shouldShowBubble(viewName)) return
        val viewGroup = (fetchGliaOrRootView() as? ViewGroup) ?: return
        if (viewGroup.hasChildOfType(ChatHeadLayout::class.java)) return

        createChatHeadLayout(activity)
        try {
            Logger.d(TAG, "Adding application-only bubble")
            activity.runOnUiThread { viewGroup.addView(chatHeadLayout.get()) }
        } catch (e: IllegalStateException) {
            Log.d(TAG, "Cannot add bubble: $e")
        }
    }

    override fun removeChatHeadLayoutIfPresent() {
        Logger.d(TAG, "Removing application-only bubble")
        saveBubblePosition()
        (fetchGliaOrRootView() as? ViewGroup)?.removeView(chatHeadLayout.get())
        chatHeadLayout.clear()
    }

    private fun saveBubblePosition() {
        chatHeadLayout.get()?.getPosition()?.let {
            if (it.first == null || it.second == null) return
            chatHeadViewPosition = Pair(it.first, it.second)
        }
    }


    private fun isSameOrientation(activity: Activity) =
        screenOrientation == activity.resources.configuration.orientation

    private fun restoreBubblePosition() {
        chatHeadViewPosition?.let {
            chatHeadLayout.get()?.setPosition(it.first.toFloat(), it.second.toFloat())
        }
    }

    private fun hasGliaView(activity: Activity?): Boolean {
        var gliaView: View? = null
        activity?.let {
            gliaView = it.findViewById(R.id.call_view)
                ?: it.findViewById<FilePreviewView>(R.id.file_preview_view)
                        ?: it.findViewById<EndScreenSharingView>(R.id.screen_sharing_screen_view)
        }
        return gliaView != null
    }

    override fun fetchGliaOrRootView(): View? {
        return resumedActivity.get()?.let {
            return it.findViewById(R.id.call_view)
                ?: it.findViewById<FilePreviewView>(R.id.file_preview_view)
                ?: it.findViewById<ChatView>(R.id.chat_view)
                ?: it.findViewById<EndScreenSharingView>(R.id.screen_sharing_screen_view)
                ?: it.findViewById<MessageCenterView>(R.id.message_center_view)
                ?: it.findViewById(android.R.id.content)
                ?: it.window.decorView.findViewById(android.R.id.content)
        }
    }

    override fun openCallActivity() {
        resumedActivity.get()?.let {
            val intent = CallActivity.getIntent(
                it,
                getConfigurationBuilder().setMediaType(Utils.toMediaType(GliaWidgets.MEDIA_TYPE_VIDEO))
                    .setIsUpgradeToCall(true)
                    .build()
            )
            it.startActivity(intent)
        }
    }

    private fun getConfigurationBuilder(): Configuration.Builder {
        val configuration = Dependencies.getSdkConfigurationManager().createWidgetsConfiguration()
        return Configuration.Builder().setWidgetsConfiguration(configuration)
    }

    private fun navigateToChat(activity: Activity?) {
        activity?.let {
            val intent = ChatActivity.getIntent(
                it,
                null, // No need to set contextId because engagement is already ongoing
                null  // No need to set queueId because engagement is already ongoing
            )
            it.startActivity(intent)
        }
    }

    private fun navigateToEndScreenSharing(activity: Activity?) {
        val intent = Intent(activity, EndScreenSharingActivity::class.java)
        // No need to set contextId because engagement is already ongoing
        activity?.startActivity(intent)
    }

    private fun navigateToCall(activity: Activity?) {
        activity?.let {
            val intent = CallActivity.getIntent(
                it,
                getConfigurationBuilder().setMediaType(Utils.toMediaType(GliaWidgets.MEDIA_TYPE_VIDEO))
                    .setIsUpgradeToCall(true)
                    .build()
            )
            it.startActivity(intent)
        }
    }
}
