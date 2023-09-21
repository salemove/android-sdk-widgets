@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.gva.GvaGalleryCardTheme
import com.glia.widgets.view.unifiedui.theme.gva.GvaPersistentButtonTheme
import com.glia.widgets.view.unifiedui.theme.gva.GvaTheme

/**
 * Default Theme for Gva
 */
internal fun GvaTheme(pallet: ColorPallet): GvaTheme = GvaTheme(
    quickReplyTheme = pallet.primaryColorTheme?.let { OutlinedButtonTheme(it, it) },
    persistentButtonTheme = GvaPersistentButtonTheme(pallet),
    galleryCardTheme = GvaGalleryCardTheme(pallet)
)

private fun GvaPersistentButtonTheme(
    pallet: ColorPallet
): GvaPersistentButtonTheme? = pallet.run {
    composeIfAtLeastOneNotNull(baseNeutralColorTheme, baseDarkColorTheme, baseLightColorTheme) {
        GvaPersistentButtonTheme(
            title = BaseDarkColorTextTheme(this),
            background = LayerTheme(
                fill = baseNeutralColorTheme
            ),
            button = GvaDefaultButtonTheme(this)
        )
    }
}

private fun GvaGalleryCardTheme(
    pallet: ColorPallet
): GvaGalleryCardTheme? = pallet.run {
    composeIfAtLeastOneNotNull(baseNeutralColorTheme, baseDarkColorTheme, baseLightColorTheme) {
        GvaGalleryCardTheme(
            title = BaseDarkColorTextTheme(this),
            subtitle = BaseDarkColorTextTheme(this),
            background = LayerTheme(
                fill = baseNeutralColorTheme
            ),
            button = GvaDefaultButtonTheme(this)
        )
    }
}
