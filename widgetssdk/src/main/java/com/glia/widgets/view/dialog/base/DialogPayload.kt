package com.glia.widgets.view.dialog.base

import android.view.View
import androidx.annotation.DrawableRes
import com.glia.widgets.internal.dialog.model.Link
import com.glia.widgets.locale.LocaleString

internal sealed interface DialogPayload {
    val title: LocaleString

    data class Option(
        override val title: LocaleString,
        val message: LocaleString,
        val positiveButtonText: LocaleString,
        val negativeButtonText: LocaleString,
        val poweredByText: LocaleString,
        val positiveButtonClickListener: View.OnClickListener,
        val negativeButtonClickListener: View.OnClickListener,
    ) : DialogPayload

    data class Confirmation(
        override val title: LocaleString,
        val message: LocaleString,
        val positiveButtonText: LocaleString,
        val negativeButtonText: LocaleString,
        val poweredByText: LocaleString,
        val positiveButtonClickListener: View.OnClickListener,
        val negativeButtonClickListener: View.OnClickListener,
        val link1: Link,
        val link2: Link,
        val link1ClickListener: View.OnClickListener,
        val link2ClickListener: View.OnClickListener,
    ) : DialogPayload

    data class Upgrade(
        override val title: LocaleString,
        val positiveButtonText: LocaleString,
        val negativeButtonText: LocaleString,
        val poweredByText: LocaleString,
        @DrawableRes
        val iconRes: Int,
        val positiveButtonClickListener: View.OnClickListener,
        val negativeButtonClickListener: View.OnClickListener,
    ) : DialogPayload

    data class OperatorEndedEngagement(
        override val title: LocaleString,
        val message: LocaleString,
        val buttonText: LocaleString,
        val buttonClickListener: View.OnClickListener,
    ) : DialogPayload

    data class AlertDialog(
        override val title: LocaleString,
        val message: LocaleString,
        val buttonVisible: Boolean = false,
        val buttonDescription: LocaleString? = null,
        val buttonClickListener: View.OnClickListener? = null
    ) : DialogPayload
}
