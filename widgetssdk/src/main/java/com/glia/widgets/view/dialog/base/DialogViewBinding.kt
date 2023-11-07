package com.glia.widgets.view.dialog.base

import android.view.View
import android.widget.TextView
import androidx.viewbinding.ViewBinding

internal interface DialogViewBinding<T : ViewBinding> {
    val root: View
    val binding: T

    val titleTv: TextView
}
