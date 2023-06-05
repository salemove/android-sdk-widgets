@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal fun BaseText(colorTheme: ColorTheme?): TextTheme? =
    composeIfAtLeastOneNotNull(colorTheme) {
        TextTheme(textColor = colorTheme)
    }

/**
 * Default theme for `baseDarkColor` text
 */
internal fun BaseDarkColorTextTheme(pallet: ColorPallet): TextTheme? =
    BaseText(pallet.baseDarkColorTheme)

/**
 * Default theme for `baseLightColor` text
 */
internal fun BaseLightColorTextTheme(pallet: ColorPallet): TextTheme? =
    BaseText(pallet.baseLightColorTheme)

/**
 * Default theme for `baseNormalColor` text
 */
internal fun BaseNormalColorTextTheme(pallet: ColorPallet): TextTheme? =
    BaseText(pallet.baseNormalColorTheme)

/**
 * Default theme for `basePrimaryColor` text
 */
internal fun BasePrimaryColorTextTheme(pallet: ColorPallet): TextTheme? =
    BaseText(pallet.primaryColorTheme)

/**
 * Default theme for `baseNegativeColor` text
 */
internal fun BaseNegativeColorTextTheme(pallet: ColorPallet): TextTheme? =
    BaseText(pallet.systemNegativeColorTheme)
