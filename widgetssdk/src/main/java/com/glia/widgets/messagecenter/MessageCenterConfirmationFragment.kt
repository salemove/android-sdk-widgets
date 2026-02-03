package com.glia.widgets.messagecenter

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.glia.telemetry_lib.ButtonNames
import com.glia.telemetry_lib.GliaLogger
import com.glia.widgets.HostActivity
import com.glia.widgets.R
import com.glia.widgets.base.BaseFragment
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.logScConfirmationScreenButtonClicked

/**
 * Fragment for MessageCenter confirmation screen with MVI architecture.
 *
 * Displayed as a full-screen fragment within HostActivity after a message is successfully sent.
 * Shows a confirmation message and allows navigation to messages.
 */
internal class MessageCenterConfirmationFragment :
    BaseFragment<MessageCenterConfirmationUiState, MessageCenterConfirmationEffect, MessageCenterConfirmationViewModel>(
        R.layout.fragment_message_center_confirmation
    ) {

    override val viewModel: MessageCenterConfirmationViewModel by viewModels { Dependencies.viewModelFactory }
    private val localeProvider by lazy { Dependencies.localeProvider }

    private var confirmationScreenView: MessageCenterConfirmationView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        confirmationScreenView = view.findViewById(R.id.message_center_confirmation_view)

        super.onViewCreated(view, savedInstanceState)
    }

    override fun setupViews() {
        // Setup AppBar through the custom view
        confirmationScreenView?.onCloseClickListener = View.OnClickListener {
            viewModel.processIntent(MessageCenterConfirmationIntent.CloseClicked)
            GliaLogger.logScConfirmationScreenButtonClicked(ButtonNames.CLOSE)
        }

        // Setup ConfirmationScreenView listener
        confirmationScreenView?.confirmationView?.setOnCheckMessagesButtonClickListener {
            viewModel.processIntent(MessageCenterConfirmationIntent.CheckMessagesClicked)
        }
    }

    override fun handleState(state: MessageCenterConfirmationUiState) {
        // Confirmation screen has no dynamic state to render
    }

    override fun handleEffect(effect: MessageCenterConfirmationEffect) {
        when (effect) {
            MessageCenterConfirmationEffect.NavigateToMessaging -> {
                Dependencies.activityLauncher.launchChat(
                    requireContext(),
                    com.glia.widgets.chat.Intention.SC_CHAT
                )
                (activity as? HostActivity)?.finishAndRemoveTask()
            }

            MessageCenterConfirmationEffect.ReturnToLiveChat -> {
                Dependencies.activityLauncher.launchChat(
                    requireContext(),
                    com.glia.widgets.chat.Intention.RETURN_TO_CHAT
                )
                (activity as? HostActivity)?.finishAndRemoveTask()
            }

            MessageCenterConfirmationEffect.Finish -> {
                (activity as? HostActivity)?.finishAndRemoveTask()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        confirmationScreenView = null
    }
}