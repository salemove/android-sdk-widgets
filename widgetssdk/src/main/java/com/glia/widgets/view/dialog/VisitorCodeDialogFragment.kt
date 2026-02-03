package com.glia.widgets.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import com.glia.telemetry_lib.DialogNames
import com.glia.telemetry_lib.EventAttribute
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.widgets.HostActivity
import com.glia.widgets.di.Dependencies
import com.glia.widgets.view.VisitorCodeView
import com.google.android.material.dialog.MaterialDialogs

/**
 * DialogFragment wrapper for VisitorCodeView to work with HostActivity.
 *
 * This is a thin wrapper that hosts VisitorCodeView and delegates all logic to
 * the existing VisitorCodeController. The View can still be used standalone by
 * integrators without requiring a Fragment/Activity wrapper.
 *
 * Architecture:
 * - VisitorCodeView remains fully functional standalone
 * - VisitorCodeController remains the source of truth for business logic
 * - This Fragment just provides integration with HostActivity navigation
 */
internal class VisitorCodeDialogFragment : AppCompatDialogFragment() {

    private var visitorCodeView: VisitorCodeView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        visitorCodeView = Dependencies.useCaseFactory.visitorCodeViewBuilderUseCase(
            requireContext(),
            true // isClosable
        )
        return visitorCodeView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Log dialog shown
        GliaLogger.i(LogEvents.DIALOG_SHOWN) {
            put(EventAttribute.DialogName, DialogNames.VISITOR_CODE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        visitorCodeView = null
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)

        // Log dialog closed
        GliaLogger.i(LogEvents.DIALOG_CLOSED) {
            put(EventAttribute.DialogName, DialogNames.VISITOR_CODE)
        }

        // Notify HostActivity to finish if no content remains
        (activity as? HostActivity)?.finishIfEmpty()
    }

    override fun onCancel(dialog: android.content.DialogInterface) {
        super.onCancel(dialog)

        // Notify CallVisualizerController about dismissal
        Dependencies.controllerFactory.callVisualizerController.dismissVisitorCodeDialog()
    }
}
