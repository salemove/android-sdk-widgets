package com.glia.widgets.view.unifiedui.theme

import android.graphics.Typeface
import androidx.annotation.DrawableRes
import com.glia.widgets.view.unifiedui.theme.alert.AlertTheme

internal data class AlertDialogConfiguration(
    val theme: AlertTheme,
    val properties: Properties,
    val icons: Icons
)

internal data class Icons(
    @DrawableRes val iconLeaveQueue: Int?,
    @DrawableRes val iconScreenSharingDialog: Int?,
)

internal data class Properties(
    val typeface: Typeface?,
    val whiteLabel: Boolean?
)
