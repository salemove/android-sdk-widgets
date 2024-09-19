package com.glia.widgets.entrywidget

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.glia.widgets.R
import com.glia.widgets.databinding.EntryWidgetFragmentBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.entrywidget.adapter.EntryWidgetAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * EntryWidgetFragment (bottom sheet) provides a way to display the entry points for the user to start a chat, audio call, video call, or secure messaging.
 */
internal class EntryWidgetFragment : BottomSheetDialogFragment() {

    interface OnDismissListener {
        fun onEntryWidgetDismiss()
    }

    private var onDismissListener: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = EntryWidgetFragmentBinding.inflate(inflater, container, false)

        val entryWidgetsTheme = Dependencies.gliaThemeManager.theme?.entryWidgetTheme
        val entryWidgetAdapter = EntryWidgetAdapter(EntryWidgetContract.ViewType.BOTTOM_SHEET, entryWidgetsTheme)

        EntryWidgetView(
            requireContext(),
            entryWidgetAdapter,
            entryWidgetsTheme
        ).also {
            binding.container.addView(it)
            it.onDismissListener = {
                dismiss()
            }
        }

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.ThemeOverlay_Glia_BottomSheetDialog)
        val dialog = super.onCreateDialog(savedInstanceState)
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (activity as? OnDismissListener)?.let {
            onDismissListener = it::onEntryWidgetDismiss
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        onDismissListener?.invoke()
    }

    fun show(parentFragmentManager: FragmentManager) {
        show(parentFragmentManager, tag)
    }

}
