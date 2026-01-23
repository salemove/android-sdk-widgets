package com.glia.widgets.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import com.glia.widgets.R
import com.glia.widgets.base.GliaFragment
import com.glia.widgets.base.GliaFragmentContract
import com.glia.widgets.databinding.ChatFragmentBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.FragmentArgumentKeys
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.getEnumArgument
import com.glia.widgets.launcher.ActivityLauncher
import com.glia.widgets.locale.LocaleString

/**
 * Fragment for displaying chat engagements.
 *
 * Features:
 * - Shows chat history to authenticated visitors before enqueuing for new engagements.
 * - Requests required permissions and enqueues for new chat engagements if no ongoing engagements are found.
 * - Provides controls for managing ongoing engagements.
 * - Enables message exchange between the visitor and the operator during ongoing engagements.
 * - Allows the operator to upgrade engagements.
 *
 * This Fragment is hosted by [ChatActivity] which handles Intent-based launches for backwards compatibility.
 *
 * @see ChatActivity
 * @see ChatView
 */
internal class ChatFragment : GliaFragment() {
    private val activityLauncher: ActivityLauncher by lazy { Dependencies.activityLauncher }

    private var _binding: ChatFragmentBinding? = null
    private val binding get() = _binding!!

    private val chatView: ChatView
        get() = binding.chatView

    private var host: GliaFragmentContract.Host? = null

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            chatView.onBackPressed()
        }
    }

    override val gliaView: View
        get() = chatView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Logger.i(TAG, "Create Chat Fragment")
        _binding = ChatFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        host = activity as? GliaFragmentContract.Host

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        chatView.setOnTitleUpdatedListener { title: LocaleString? ->
            host?.setHostTitle(title)
        }

        if (!chatView.shouldShow()) {
            host?.finish()
            return
        }

        chatView.setOnBackClickedListener {
            host?.finish()
        }

        chatView.setOnBackToCallListener(::backToCallScreen)

        chatView.setOnEndListener {
            host?.finish()
        }

        chatView.setOnMinimizeListener {
            host?.finish()
        }

        val intention = arguments?.getEnumArgument<Intention>(FragmentArgumentKeys.OPEN_CHAT_INTENTION)

        checkNotNull(intention) { "Intention must be provided" }

        if (savedInstanceState == null) {
            chatView.startChat(intention)
        } else {
            chatView.restoreChat()
        }
    }

    override fun onResume() {
        super.onResume()
        chatView.onResume()
    }

    override fun onPause() {
        super.onPause()
        chatView.onPause()
    }

    override fun onDestroyView() {
        chatView.onDestroyView()
        _binding = null
        Logger.i(TAG, "Destroy Chat Fragment")
        super.onDestroyView()
    }

    override fun onDetach() {
        super.onDetach()
        host = null
    }

    private fun backToCallScreen() {
        requireContext().let { context ->
            activityLauncher.launchCall(context, null, false)
            host?.finish()
        }
    }

    companion object {
        /**
         * Create a new instance of ChatFragment with the given intention.
         *
         * @param intention The chat intention (welcome screen, chat history, etc.)
         * @return A new ChatFragment instance
         */
        fun newInstance(intention: Intention): ChatFragment {
            return ChatFragment().apply {
                arguments = Bundle().apply {
                    putInt(FragmentArgumentKeys.OPEN_CHAT_INTENTION, intention.ordinal)
                }
            }
        }
    }
}
