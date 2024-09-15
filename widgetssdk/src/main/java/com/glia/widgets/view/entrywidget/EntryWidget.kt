package com.glia.widgets.view.entrywidget

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import com.glia.widgets.Navigator
import com.glia.widgets.R

/**
 * EntryWidget provides a way to display the entry points for the user to start a chat, audio call, video call, or secure messaging.
 */
class EntryWidget(val navigator: Navigator) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_layout, container, false)

        val liveChatButton: Button = view.findViewById(R.id.liveChat)
        val audioButton: Button = view.findViewById(R.id.audio)
        val videoButton: Button = view.findViewById(R.id.video)
        val secureMessagingButton: Button = view.findViewById(R.id.secureMessaging)


        liveChatButton.setOnClickListener {
            // Handle live chat button click
            context?.let { navigator.startChat(it) }
            hide()
        }
        audioButton.setOnClickListener {
            // Handle audio button click
            context?.let { navigator.startAudioCall(it) }
            hide()
        }

        videoButton.setOnClickListener {
            // Handle video button click
            context?.let { navigator.startVideoCall(it) }
            hide()
        }

        secureMessagingButton.setOnClickListener {
            // Handle secure messaging button click
            context?.let { navigator.startSecureConversations(it) }
            hide()
        }

        return view
    }

    fun show(parentFragmentManager: FragmentManager) {
        show(parentFragmentManager, tag)
    }

    fun hide() {
        dismiss()
    }
}
