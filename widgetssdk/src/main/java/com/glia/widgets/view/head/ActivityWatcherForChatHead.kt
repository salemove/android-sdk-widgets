package com.glia.widgets.view.head

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Pair
import androidx.core.view.contains
import com.glia.widgets.R
import com.glia.widgets.base.BaseActivityStackWatcher
import com.glia.widgets.call.CallActivity
import com.glia.widgets.call.CallConfiguration
import com.glia.widgets.callvisualizer.EndScreenSharingActivity
import com.glia.widgets.callvisualizer.EndScreenSharingView
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.chat.ChatView
import com.glia.widgets.di.Dependencies
import com.glia.widgets.filepreview.ui.FilePreviewView
import com.glia.widgets.helper.DialogHolderView
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.WeakReferenceDelegate
import com.glia.widgets.helper.hasChildOfType
import com.glia.widgets.messagecenter.MessageCenterView
import com.glia.widgets.view.head.controller.ActivityWatcherForChatHeadContract

@SuppressLint("CheckResult")
internal class ActivityWatcherForChatHead(
    val controller: ActivityWatcherForChatHeadContract.Controller
) : BaseActivityStackWatcher(), ActivityWatcherForChatHeadContract.Watcher {

    init {
        topActivityObserver.subscribe(
            { resumedActivity = it },
            { error -> Logger.e(TAG, "Observable monitoring top activity FAILED", error) }
        )
        controller.setWatcher(this)
    }

    /**
     * Returns last activity that called [Activity.onResume], but didn't call [Activity.onPause] yet
     * @return Currently resumed activity.
     */

    private var resumedActivity: Activity? by WeakReferenceDelegate()
    private var chatHeadLayout: ChatHeadLayout? by WeakReferenceDelegate()
    private var screenOrientation: Int? = null
    private var chatHeadViewPosition: Pair<Int, Int>? = null

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        controller.onActivityResumed()
        if (isSameOrientation(activity)) restoreBubblePosition()
    }

    override fun onActivityPaused(activity: Activity) {
        super.onActivityPaused(activity)
        screenOrientation = activity.resources.configuration.orientation
        controller.onActivityPaused()
        if (activity === resumedActivity) {
            // MOB-3516: Android emulator with API 29 calls onActivityPaused() AFTER onActivityResumed().
            // activity === resumedActivity prevents hiding a bubble when onActivityPaused() is called for
            // an activity that is not resumed at the moment
            removeChatHeadLayoutIfPresent()
        }
    }

    private fun createChatHeadLayout(activity: Activity) {
        val chatHeadLayout = ChatHeadLayout(activity)
        chatHeadLayout.layoutParams = ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        chatHeadLayout.setNavigationCallback(
            object : ChatHeadLayout.NavigationCallback {
                override fun onNavigateToEndScreenSharing() {
                    navigateToEndScreenSharing(resumedActivity)
                }

                override fun onNavigateToChat() {
                    navigateToChat(resumedActivity)
                }

                override fun onNavigateToCall() {
                    navigateToCall(resumedActivity)
                    if (controller.isFromCallScreen()) {
                        // Finish ChatActivity if bubble is tapped from ChatActivity
                        controller.resetFromCallScreen()
                        resumedActivity?.finish()
                    }
                }
            }
        )
        chatHeadLayout.visibility = View.VISIBLE
        this.chatHeadLayout = chatHeadLayout
    }

    override fun addChatHeadLayoutIfAbsent() {
        val activity = resumedActivity ?: return
        val viewName = fetchGliaOrRootView()?.javaClass?.simpleName
        if (hasGliaView(activity) || !controller.shouldShowBubble(viewName)) return
        val viewGroup = (fetchGliaOrRootView() as? ViewGroup) ?: return
        if (viewGroup.hasChildOfType(ChatHeadLayout::class.java)) return

        createChatHeadLayout(activity)
        try {
            Logger.d(TAG, "Adding application-only bubble")
            activity.runOnUiThread {
                chatHeadLayout?.let {
                    if (!viewGroup.contains(it)) {
                        Logger.i(TAG, "Bubble: show application-only bubble")
                        viewGroup.addView(it)
                    } else {
                        Logger.e(TAG, "Duplicate bubble adding detected")
                    }
                }
            }
        } catch (e: IllegalStateException) {
            Log.d(TAG, "Cannot add bubble: $e")
        }
    }

    override fun removeChatHeadLayoutIfPresent() {
        Logger.d(TAG, "Bubble: remove application-only bubble")
        saveBubblePosition()
        (fetchGliaOrRootView() as? ViewGroup)?.let { gliaOrRootView ->
            gliaOrRootView.post {
                gliaOrRootView.removeView(chatHeadLayout)
                chatHeadLayout = null
            }
        }
    }

    private fun saveBubblePosition() {
        chatHeadLayout?.getPosition()?.let {
            if (it.first == null || it.second == null) return
            chatHeadViewPosition = Pair(it.first, it.second)
        }
    }

    private fun isSameOrientation(activity: Activity) =
        screenOrientation == activity.resources.configuration.orientation

    private fun restoreBubblePosition() {
        chatHeadViewPosition?.let {
            chatHeadLayout?.setPosition(it.first.toFloat(), it.second.toFloat())
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
        return resumedActivity?.let {
            return it.findViewById(R.id.call_view)
                ?: it.findViewById<FilePreviewView>(R.id.file_preview_view)
                ?: it.findViewById<ChatView>(R.id.chat_view)
                ?: it.findViewById<EndScreenSharingView>(R.id.screen_sharing_screen_view)
                ?: it.findViewById<MessageCenterView>(R.id.message_center_view)
                ?: it.findViewById<DialogHolderView>(R.id.dialog_holder_activity_view_id)
                ?: it.findViewById(android.R.id.content)
                ?: it.window.decorView.findViewById(android.R.id.content)
        }
    }

    private fun getDefaultCallConfiguration(): CallConfiguration = Dependencies.sdkConfigurationManager
        .buildEngagementConfiguration()
        .let(::CallConfiguration)

    private fun navigateToChat(activity: Activity?) {
        activity?.let {
            val intent = ChatActivity.getIntent(
                it,
                null, // No need to set contextId because engagement is already ongoing
                emptyList() // No need to set queueId because engagement is already ongoing
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
                getDefaultCallConfiguration().copy(isUpgradeToCall = true)
            )
            it.startActivity(intent)
        }
    }
}
