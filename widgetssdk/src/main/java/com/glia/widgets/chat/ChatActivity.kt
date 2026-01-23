package com.glia.widgets.chat

import android.os.Bundle
import com.glia.widgets.R
import com.glia.widgets.base.FadeTransitionActivity
import com.glia.widgets.base.GliaActivity
import com.glia.widgets.base.GliaFragmentContract
import com.glia.widgets.helper.ExtraKeys
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.getEnumExtra
import com.glia.widgets.locale.LocaleString

/**
 * This activity hosts [ChatFragment] and serves as an entry point for chat engagements.
 *
 * **Architecture:** This Activity is a thin wrapper that hosts the Fragment. All UI logic
 * is implemented in [ChatFragment] and [ChatView]. This Activity handles Intent-based
 * launches for backwards compatibility.
 *
 * Main features:
 * - Shows chat history to authenticated visitors before enqueuing for new engagements.
 * - Requests required permissions and enqueues for new chat engagements if no ongoing engagements are found.
 * - Provides controls for managing ongoing engagements.
 * - Enables message exchange between the visitor and the operator during ongoing engagements.
 * - Allows the operator to upgrade engagements.
 *
 * Before this activity is launched, make sure that Glia Widgets SDK is set up correctly.
 *
 * @see ChatFragment
 * @see ChatView
 */
internal class ChatActivity : GliaActivity<ChatView>, FadeTransitionActivity(), GliaFragmentContract.Host {
    private var chatFragment: ChatFragment? = null

    override val gliaView: ChatView
        get() = chatFragment?.gliaView as? ChatView ?: error("Fragment not initialized")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.i(TAG, "Create Chat screen")
        setContentView(R.layout.chat_activity_host)

        if (savedInstanceState == null) {
            val intention = intent.getEnumExtra<Intention>(ExtraKeys.OPEN_CHAT_INTENTION)
            checkNotNull(intention) { "Intention must be provided" }

            chatFragment = ChatFragment.newInstance(intention)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, chatFragment!!)
                .commit()
        } else {
            chatFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? ChatFragment
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.i(TAG, "Destroy Chat screen")
    }

    override fun setHostTitle(locale: LocaleString?) {
        setTitle(locale)
    }

    override fun finish() = super.finish()
}
