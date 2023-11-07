package com.glia.widgets.view.dialog.base

import android.view.View
import androidx.annotation.DrawableRes

internal sealed interface DialogPayload {
    val title: String

    data class Option(
        override val title: String,
        val message: String,
        val positiveButtonText: String,
        val negativeButtonText: String,
        val poweredByText: String,
        val positiveButtonClickListener: View.OnClickListener,
        val negativeButtonClickListener: View.OnClickListener,
    ) : DialogPayload

    data class ScreenSharing(
        override val title: String,
        val message: String,
        val positiveButtonText: String,
        val negativeButtonText: String,
        val poweredByText: String,
        val positiveButtonClickListener: View.OnClickListener,
        val negativeButtonClickListener: View.OnClickListener,
    ) : DialogPayload

    data class Upgrade(
        override val title: String,
        val positiveButtonText: String,
        val negativeButtonText: String,
        val poweredByText: String,
        @DrawableRes
        val iconRes: Int,
        val positiveButtonClickListener: View.OnClickListener,
        val negativeButtonClickListener: View.OnClickListener,
    ) : DialogPayload

    data class OperatorEndedEngagement(
        override val title: String,
        val message: String,
        val buttonText: String,
        val buttonClickListener: View.OnClickListener,
    ) : DialogPayload

    data class AlertDialog(
        override val title: String,
        val message: String,
        val buttonVisible: Boolean = false,
        val buttonDescription: String? = null,
        val buttonClickListener: View.OnClickListener? = null
    ) : DialogPayload
}
