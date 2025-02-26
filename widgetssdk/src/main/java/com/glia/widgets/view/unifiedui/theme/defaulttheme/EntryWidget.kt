@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.entrywidget.EntryWidgetTheme
import com.glia.widgets.view.unifiedui.theme.entrywidget.MediaTypeItemTheme
import com.glia.widgets.view.unifiedui.theme.entrywidget.MediaTypeItemsTheme

internal fun EntryWidgetTheme(pallet: ColorPallet?): EntryWidgetTheme? = pallet?.run {
    EntryWidgetTheme(
        background = LayerTheme(lightColorTheme),
        mediaTypeItems = DefaultMediaTypeItemsTheme(this),
        errorTitle = TextTheme(
            textColor = darkColorTheme
        ),
        errorMessage = TextTheme(
            textColor = shadeColorTheme
        ),
        errorButton = LinkDefaultButtonTheme(this)
    )
}

internal fun DefaultMediaTypeItemsTheme(pallet: ColorPallet?): MediaTypeItemsTheme? =
    pallet?.run {
        MediaTypeItemsTheme(
            mediaTypeItem = DefaultMediaItemTypeTheme(this),
            dividerColor = shadeColorTheme
        )
    }

internal fun DefaultMediaItemTypeTheme(pallet: ColorPallet?): MediaTypeItemTheme? =
    pallet?.run {
        MediaTypeItemTheme(
            iconColor = primaryColorTheme,
            title = TextTheme(
                textColor = darkColorTheme
            ),
            message = TextTheme(
                textColor = normalColorTheme
            ),
            loadingTintColor = shadeColorTheme,
            badge = BadgeTheme(this)
        )
    }
