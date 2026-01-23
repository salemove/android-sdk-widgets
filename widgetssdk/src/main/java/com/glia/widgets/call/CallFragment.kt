package com.glia.widgets.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.widgets.base.GliaFragment
import com.glia.widgets.base.GliaFragmentContract
import com.glia.widgets.call.CallView.OnNavigateToChatListener
import com.glia.widgets.call.CallView.OnNavigateToWebBrowserListener
import com.glia.widgets.chat.Intention
import com.glia.widgets.databinding.CallFragmentBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.engagement.MediaType
import com.glia.widgets.helper.FragmentArgumentKeys
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.getEnumArgument
import com.glia.widgets.launcher.ActivityLauncher
import com.glia.widgets.locale.LocaleString

/**
 * Fragment for displaying audio and/or video call engagements.
 *
 * Features:
 * - Requests required permissions and enqueues for audio and/or video engagements if no ongoing engagements are found.
 * - Provides video feeds from operator and visitor cameras.
 * - Provides controls for managing ongoing engagements, including video and audio.
 * - Allows switching between chat and call activities.
 *
 * This Fragment is hosted by [CallActivity] which handles Intent-based launches for backwards compatibility.
 *
 * @see CallActivity
 * @see CallView
 */
internal class CallFragment : GliaFragment() {
    private val activityLauncher: ActivityLauncher by lazy { Dependencies.activityLauncher }

    private var _binding: CallFragmentBinding? = null
    private val binding get() = _binding!!

    private val callView: CallView
        get() = binding.callView

    private var host: GliaFragmentContract.Host? = null

    private var onBackClickedListener: CallView.OnBackClickedListener? = null
    private var onNavigateToChatListener: OnNavigateToChatListener? = null
    private var onNavigateToWebBrowserListener: OnNavigateToWebBrowserListener? = null

    override val gliaView: View
        get() = callView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Logger.i(TAG, "Create Call Fragment")
        _binding = CallFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        host = activity as? GliaFragmentContract.Host

        val isUpgradeToCall = arguments?.getBoolean(FragmentArgumentKeys.IS_UPGRADE_TO_CALL, false) ?: false

        if (!callView.shouldShowMediaEngagementView(isUpgradeToCall)) {
            host?.finish()
            return
        }

        callView.setOnTitleUpdatedListener { title: LocaleString? ->
            host?.setHostTitle(title)
        }

        onBackClickedListener = CallView.OnBackClickedListener {
            host?.finish()
        }
        onBackClickedListener?.also(callView::setOnBackClickedListener)

        callView.setOnEndListener {
            host?.finish()
        }

        callView.setOnMinimizeListener {
            host?.finish()
        }

        onNavigateToChatListener = OnNavigateToChatListener {
            navigateToChat()
            host?.finish()
        }
        onNavigateToChatListener?.also(callView::setOnNavigateToChatListener)

        onNavigateToWebBrowserListener = OnNavigateToWebBrowserListener(::navigateToWebBrowser)
        onNavigateToWebBrowserListener?.also(callView::setOnNavigateToWebBrowserListener)

        if (savedInstanceState == null) {
            val mediaType = arguments?.getEnumArgument<MediaType>(FragmentArgumentKeys.MEDIA_TYPE)
            startCall(mediaType, isUpgradeToCall)
        }
    }

    override fun onResume() {
        super.onResume()
        callView.onResume()
        GliaLogger.i(LogEvents.CALL_SCREEN_SHOWN)
    }

    override fun onPause() {
        super.onPause()
        callView.onPause()
        GliaLogger.i(LogEvents.CALL_SCREEN_CLOSED)
    }

    override fun onDestroyView() {
        callView.onDestroy()
        onBackClickedListener = null
        onNavigateToChatListener = null
        onNavigateToWebBrowserListener = null
        _binding = null
        Logger.i(TAG, "Destroy Call Fragment")
        super.onDestroyView()
    }

    override fun onDetach() {
        super.onDetach()
        host = null
    }

    /**
     * Called when the user interacts with the UI.
     * This method should be called by the host Activity's onUserInteraction().
     */
    fun onUserInteraction() {
        callView.onUserInteraction()
    }

    private fun startCall(mediaType: MediaType?, isUpgradeToCall: Boolean) {
        callView.startCall(isUpgradeToCall, mediaType)
    }

    private fun navigateToChat() {
        Logger.d(TAG, "navigateToChat")
        requireContext().let { context ->
            activityLauncher.launchChat(context, Intention.RETURN_TO_CHAT)
        }
    }

    private fun navigateToWebBrowser(title: LocaleString, url: String) {
        requireContext().let { context ->
            activityLauncher.launchWebBrowser(context, title, url)
        }
    }

    companion object {
        /**
         * Create a new instance of CallFragment with the given media type and upgrade flag.
         *
         * @param mediaType The media type (audio or video), or null
         * @param upgradeToCall Whether this is an upgrade from chat to call
         * @return A new CallFragment instance
         */
        fun newInstance(mediaType: MediaType?, upgradeToCall: Boolean): CallFragment {
            return CallFragment().apply {
                arguments = Bundle().apply {
                    putInt(FragmentArgumentKeys.MEDIA_TYPE, mediaType?.ordinal ?: -1)
                    putBoolean(FragmentArgumentKeys.IS_UPGRADE_TO_CALL, upgradeToCall)
                }
            }
        }
    }
}
