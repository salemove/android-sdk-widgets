package com.glia.widgets.messagecenter

import android.os.Bundle
import com.glia.widgets.R
import com.glia.widgets.base.FadeTransitionActivity
import com.glia.widgets.base.GliaActivity
import com.glia.widgets.base.GliaFragmentContract
import com.glia.widgets.chat.Intention
import com.glia.widgets.locale.LocaleString

/**
 * This activity hosts [MessageCenterFragment] and serves as an entry point for secure messaging.
 *
 * **Architecture:** This Activity is a thin wrapper that hosts the Fragment. All UI logic
 * is implemented in [MessageCenterFragment] and [MessageCenterView]. This Activity handles Intent-based
 * launches for backwards compatibility.
 *
 * Main features:
 * - Returns an error if the visitor is not authenticated or the specified queue does not support secure messaging.
 * - Allows sending asynchronous messages.
 * - Offers the option to access chat history.
 *
 * Before this activity is launched, make sure that Glia Widgets SDK is set up correctly.
 *
 * @see MessageCenterFragment
 * @see MessageCenterView
 */
internal class MessageCenterActivity : GliaActivity<MessageCenterView>, FadeTransitionActivity(), GliaFragmentContract.Host {
    private var messageCenterFragment: MessageCenterFragment? = null

    override val gliaView: MessageCenterView
        get() = messageCenterFragment?.gliaView as? MessageCenterView ?: error("Fragment not initialized")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.message_center_activity_host)

        if (savedInstanceState == null) {
            messageCenterFragment = MessageCenterFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, messageCenterFragment!!)
                .commit()
        } else {
            messageCenterFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? MessageCenterFragment
        }
    }

    override fun setHostTitle(locale: LocaleString?) {
        setTitle(locale)
    }

    override fun finish() = super.finish()
}
