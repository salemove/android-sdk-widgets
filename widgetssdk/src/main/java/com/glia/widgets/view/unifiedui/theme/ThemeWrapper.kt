package com.glia.widgets.view.unifiedui.theme

import android.graphics.Typeface
import androidx.annotation.DrawableRes

internal data class AlertDialogConfiguration(
    val theme: UnifiedTheme,
    val properties: Properties,
    val icons: Icons
)

internal data class Icons(
    @DrawableRes val iconLeaveQueue: Int?,
    @DrawableRes val iconScreenSharingDialog: Int?,
)

internal data class Properties(
    val typeface: Typeface?
)
