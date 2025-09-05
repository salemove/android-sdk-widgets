package com.glia.widgets.chat

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import com.glia.widgets.R
import com.glia.widgets.base.FadeTransitionActivity
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.ExtraKeys
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.getEnumExtra
import com.glia.widgets.launcher.ActivityLauncher
import kotlin.properties.Delegates

/**
 * This activity is used to handle chat engagements.
 *
 *
 * Main features:
 * - Shows chat history to authenticated visitors before enqueuing for new engagements.
 * - Requests required permissions and enqueues for new chat engagements if no ongoing engagements are found.
 * - Provides controls for managing ongoing engagements.
 * - Enables message exchange between the visitor and the operator during ongoing engagements.
 * - Allows the operator to upgrade engagements.
 *
 *
 * Before this activity is launched, make sure that Glia Widgets SDK is set up correctly.
 */
internal class ChatActivity : FadeTransitionActivity() {
    private val activityLauncher: ActivityLauncher by lazy { Dependencies.activityLauncher }

    private var chatView: ChatView by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        this.enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Logger.i(TAG, "Create Chat screen")
        setContentView(R.layout.chat_activity)
        chatView = findViewById(R.id.chat_view)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                chatView.onBackPressed()
            }
        })

        chatView.setOnTitleUpdatedListener(this::setTitle)

        if (!chatView.shouldShow()) {
            finishAfterTransition()
            return
        }

        chatView.setOnBackClickedListener(::finishAfterTransition)
        chatView.setOnBackToCallListener(::backToCallScreen)

        // In case the engagement ends, Activity is removed from the device's Recents menu
        // to avoid app users to accidentally start queueing for another call when they resume
        // the app from the Recents menu and the app's backstack was empty.
        chatView.setOnEndListener(::finishAfterTransition)

        chatView.setOnMinimizeListener(::finishAfterTransition)

        val intention = intent.getEnumExtra<Intention>(ExtraKeys.OPEN_CHAT_INTENTION)

        check(intention != null) { "Intention must be provided" }

        if (savedInstanceState == null) {
            chatView.startChat(intention)
        } else {
            chatView.restoreChat()
        }
    }

    override fun onResume() {
        chatView.onResume()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        chatView.onPause()
    }

    override fun onDestroy() {
        chatView.onDestroyView()
        Logger.i(TAG, "Destroy Chat screen")
        super.onDestroy()
    }

    private fun backToCallScreen() {
        activityLauncher.launchCall(this, null, false)
        finishAfterTransition()
    }
}
