package com.glia.widgets.view.head

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.contains
import com.glia.widgets.GliaWidgets
import com.glia.widgets.base.BaseActivityStackWatcher
import com.glia.widgets.base.GliaActivity
import com.glia.widgets.chat.Intention
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.WeakReferenceDelegate
import com.glia.widgets.helper.hasChildOfType
import com.glia.widgets.helper.rootView
import com.glia.widgets.internal.chathead.BubbleContextEvent
import com.glia.widgets.internal.chathead.BubbleRenderTarget
import com.glia.widgets.internal.chathead.BubbleUiModel
import com.glia.widgets.internal.chathead.ChatBubbleContract
import com.glia.widgets.internal.chathead.VisibleScreen
import com.glia.widgets.internal.chathead.toVisibleScreen
import com.glia.widgets.launcher.ActivityLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * The only place that reports screen lifecycle to the bubble controller, and the host of the in-app
 * bubble. It attaches [ChatHeadLayout] while the controller says the bubble renders in the app.
 */
@SuppressLint("CheckResult")
internal class ActivityWatcherForChatHead(
    private val controller: ChatBubbleContract.Controller,
    private val activityLauncher: ActivityLauncher
) : BaseActivityStackWatcher() {

    /**
     * Returns last activity that called `Activity.onResume`, but didn't call `Activity.onPause` yet
     * @return Currently resumed activity.
     */

    private var resumedActivity: Activity? by WeakReferenceDelegate()
    private var chatHeadLayout: ChatHeadLayout? by WeakReferenceDelegate()

    /*
     * Declared after the fields above on purpose: collecting starts by replaying the current state,
     * which reads chatHeadLayout - and property delegates are only initialized in declaration order.
     */
    init {
        topActivityObserver.subscribe(
            { resumedActivity = it },
            { error -> Logger.e(TAG, "Observable monitoring top activity FAILED", error) }
        )
        // Process-lifetime scope: the watcher is registered for the whole application lifecycle
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).launch {
            controller.uiState.collect(::onBubbleState)
        }
    }

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        reportVisibleScreen()
        // Also attempted unconditionally, not just from the state collector: navigating between two
        // screens of the same kind leaves the model untouched, and StateFlow does not re-emit an
        // unchanged value, so the newly resumed screen would never get a bubble.
        addChatHeadLayoutIfAbsent()
    }

    override fun onActivityPaused(activity: Activity) {
        super.onActivityPaused(activity)
        if (activity === resumedActivity) {
            // MOB-3516: Android emulator with API 29 calls onActivityPaused() AFTER onActivityResumed().
            // activity === resumedActivity prevents hiding a bubble when onActivityPaused() is called for
            // an activity that is not resumed at the moment
            removeChatHeadLayoutIfPresent()
        }
        reportVisibleScreen()
    }

    /**
     * Reports the screen of whichever activity is on top of the resumed stack now. `super` has
     * already updated [resumedActivity], so this is correct for both resumes and pauses.
     */
    private fun reportVisibleScreen() {
        controller.onContextEvent(BubbleContextEvent.ScreenChanged(resumedActivity?.toVisibleScreen()))
    }

    private fun onBubbleState(state: BubbleUiModel) {
        if (state.target == BubbleRenderTarget.APPLICATION) {
            addChatHeadLayoutIfAbsent()
        } else if (chatHeadLayout != null) {
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

    private fun addChatHeadLayoutIfAbsent() {
        if (controller.uiState.value.target != BubbleRenderTarget.APPLICATION) return
        val activity = resumedActivity ?: return
        // The watcher is registered before the SDK is initialized, so there may be nothing to show yet
        if (!GliaWidgets.isInitialized()) return
        if (isCallOrImagePreviewScreen(activity)) return
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

    private fun removeChatHeadLayoutIfPresent() {
        Logger.d(TAG, "Bubble: remove application-only bubble")

        // Position needs no saving here: [ChatHeadLayout] hands it to the controller on every drag,
        // and maps it back into its bounds when the replacement layout is measured.
        chatHeadLayout?.apply {
            chatHeadLayout = null
            post { removeSelf() }
        }
    }

    private fun isCallOrImagePreviewScreen(activity: Activity): Boolean =
        activity.toVisibleScreen().let { it == VisibleScreen.CALL || it == VisibleScreen.IMAGE_PREVIEW }

    /**
     * The `ViewGroup` the in-app bubble is attached to. Glia screens expose their own view so that the
     * bubble lands inside it; every other activity contributes its root view.
     */
    internal fun fetchGliaOrRootView(): View? {
        val currentActivity = resumedActivity ?: return null

        return when (currentActivity) {
            is GliaActivity<*> -> currentActivity.gliaView
            else -> currentActivity.rootView
        }
    }

    private fun navigateToChat(activity: Activity?) {
        activity?.also { activityLauncher.launchChat(it, Intention.RETURN_TO_CHAT) }
    }

    private fun navigateToCall(activity: Activity?) {
        activity?.also { activityLauncher.launchCall(it, null, false) }
    }
}
