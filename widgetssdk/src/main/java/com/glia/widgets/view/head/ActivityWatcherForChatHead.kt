package com.glia.widgets.view.head

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.PointF
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.contains
import com.glia.widgets.R
import com.glia.widgets.base.BaseActivityStackWatcher
import com.glia.widgets.chat.ChatView
import com.glia.widgets.chat.Intention
import com.glia.widgets.filepreview.ui.ImagePreviewView
import com.glia.widgets.helper.DialogHolderView
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.WeakReferenceDelegate
import com.glia.widgets.helper.hasChildOfType
import com.glia.widgets.launcher.ActivityLauncher
import com.glia.widgets.messagecenter.MessageCenterView
import com.glia.widgets.view.head.controller.ActivityWatcherForChatHeadContract

@SuppressLint("CheckResult")
internal class ActivityWatcherForChatHead(
    val controller: ActivityWatcherForChatHeadContract.Controller,
    private val activityLauncher: ActivityLauncher
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
    private var chatHeadViewPosition: PointF? = null

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
        chatHeadLayout.layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        chatHeadLayout.setNavigationCallback(
            object : ChatHeadLayout.NavigationCallback {
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

        chatHeadLayout?.apply {
            chatHeadLayout = null
            post { removeSelf() }
        }
    }

    private fun saveBubblePosition() {
        chatHeadLayout?.position?.let {
            chatHeadViewPosition = it
        }
    }

    private fun isSameOrientation(activity: Activity) =
        screenOrientation == activity.resources.configuration.orientation

    private fun restoreBubblePosition() {
        chatHeadViewPosition?.let {
            chatHeadLayout?.setPosition(it.x, it.y)
        }
    }

    private fun hasGliaView(activity: Activity?): Boolean {
        var gliaView: View? = null
        activity?.let {
            gliaView = it.findViewById(R.id.call_view)
                ?: it.findViewById<ImagePreviewView>(R.id.preview_view)
        }
        return gliaView != null
    }

    override fun fetchGliaOrRootView(): View? {
        return resumedActivity?.let {
            return it.findViewById(R.id.call_view)
                ?: it.findViewById<ImagePreviewView>(R.id.preview_view)
                ?: it.findViewById<ChatView>(R.id.chat_view)
                ?: it.findViewById<MessageCenterView>(R.id.message_center_view)
                ?: it.findViewById<DialogHolderView>(R.id.dialog_holder_activity_view_id)
                ?: it.findViewById(android.R.id.content)
                ?: it.window.decorView.findViewById(android.R.id.content)
        }
    }

    private fun navigateToChat(activity: Activity?) {
        activity?.also { activityLauncher.launchChat(it, Intention.RETURN_TO_CHAT) }
    }

    private fun navigateToCall(activity: Activity?) {
        activity?.also { activityLauncher.launchCall(it, null, false) }
    }
}
