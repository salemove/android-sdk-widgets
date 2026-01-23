package com.glia.widgets.call

import android.os.Bundle
import com.glia.widgets.R
import com.glia.widgets.base.FadeTransitionActivity
import com.glia.widgets.base.GliaActivity
import com.glia.widgets.base.GliaFragmentContract
import com.glia.widgets.engagement.MediaType
import com.glia.widgets.helper.ExtraKeys
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.getEnumExtra
import com.glia.widgets.locale.LocaleString

/**
 * This activity hosts [CallFragment] and serves as an entry point for call engagements.
 *
 * **Architecture:** This Activity is a thin wrapper that hosts the Fragment. All UI logic
 * is implemented in [CallFragment] and [CallView]. This Activity handles Intent-based
 * launches for backwards compatibility.
 *
 * Main features:
 * - Requests required permissions and enqueues for audio and/or video engagements if no ongoing engagements are found.
 * - Provides video feeds from operator and visitor cameras.
 * - Provides controls for managing ongoing engagements, including video and audio.
 * - Allows switching between chat and call activities.
 *
 * Before this activity is launched, make sure that Glia Widgets SDK is set up correctly.
 *
 * @see CallFragment
 * @see CallView
 */
internal class CallActivity : GliaActivity<CallView>, FadeTransitionActivity(), GliaFragmentContract.Host {
    private var callFragment: CallFragment? = null

    override val gliaView: CallView
        get() = callFragment?.gliaView as? CallView ?: error("Fragment not initialized")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.i(TAG, "Create Call screen")
        setContentView(R.layout.call_activity_host)

        if (savedInstanceState == null) {
            val mediaType = intent.getEnumExtra<MediaType>(ExtraKeys.MEDIA_TYPE)
            val isUpgradeToCall = intent.getBooleanExtra(ExtraKeys.IS_UPGRADE_TO_CALL, false)

            callFragment = CallFragment.newInstance(mediaType, isUpgradeToCall)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, callFragment!!)
                .commit()
        } else {
            callFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? CallFragment
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        callFragment?.onUserInteraction()
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.i(TAG, "Destroy Call screen")
    }

    override fun setHostTitle(locale: LocaleString?) {
        setTitle(locale)
    }

    override fun finish() = super.finish()
}
