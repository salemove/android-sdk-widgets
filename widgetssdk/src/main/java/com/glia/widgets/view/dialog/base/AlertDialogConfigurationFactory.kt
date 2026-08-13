@file:JvmName("AlertDialogConfigurationFactory")

package com.glia.widgets.view.dialog.base

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.glia.widgets.R
import com.glia.widgets.helper.gliaAttrBoolean
import com.glia.widgets.helper.gliaAttrColor
import com.glia.widgets.helper.gliaAttrResourceId
import com.glia.widgets.view.unifiedui.nullSafeMerge
import com.glia.widgets.view.unifiedui.theme.AlertDialogConfiguration
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.Icons
import com.glia.widgets.view.unifiedui.theme.Properties
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.defaulttheme.AlertTheme

/**
 * Builds the dialog styling from the receiver's composed theme, then layers the JSON Unified theme
 * on top so remote configuration keeps winning over everything the XML mechanism can express.
 *
 * The receiver is always a Glia activity - integrator activities are proxied through
 * `DialogHolderActivity` - so its theme already carries the `glia*` values.
 */
internal fun Context.alertDialogConfiguration(unifiedTheme: UnifiedTheme?): AlertDialogConfiguration {
    val alertTheme = AlertTheme(gliaColorPallet()).copy(
        isVerticalAxis = gliaAttrBoolean(R.attr.gliaAlertDialogButtonUseVerticalAlignment)
    )
    val theme = UnifiedTheme(alertTheme = alertTheme, isWhiteLabel = gliaAttrBoolean(R.attr.whiteLabel))

    return AlertDialogConfiguration(
        theme = theme nullSafeMerge unifiedTheme,
        properties = Properties(typeface = gliaThemeTypeface()),
        icons = Icons(iconLeaveQueue = gliaAttrResourceId(R.attr.gliaIconLeaveQueue))
    )
}

/**
 * `neutralColorTheme` has no `glia*` attribute behind it and `secondaryColorTheme` has no default at
 * all - both are reproduced exactly as the legacy projection had them.
 */
private fun Context.gliaColorPallet(): ColorPallet = ColorPallet(
    darkColorTheme = ColorTheme(gliaAttrColor(R.attr.gliaBaseDarkColor, R.color.glia_dark_color)),
    lightColorTheme = ColorTheme(gliaAttrColor(R.attr.gliaBaseLightColor, R.color.glia_light_color)),
    neutralColorTheme = ColorTheme(ContextCompat.getColor(this, R.color.glia_neutral_color)),
    normalColorTheme = ColorTheme(gliaAttrColor(R.attr.gliaBaseNormalColor, R.color.glia_normal_color)),
    shadeColorTheme = ColorTheme(gliaAttrColor(R.attr.gliaBaseShadeColor, R.color.glia_shade_color)),
    primaryColorTheme = ColorTheme(gliaAttrColor(R.attr.gliaBrandPrimaryColor, R.color.glia_primary_color)),
    secondaryColorTheme = null,
    negativeColorTheme = ColorTheme(gliaAttrColor(R.attr.gliaSystemNegativeColor, R.color.glia_negative_color))
)

/**
 * `fontFamily` only yields a typeface when it points at a font *resource*; a family name such as
 * `sans-serif` resolves to no resource id and leaves the dialogs on their layout typefaces.
 */
private fun Context.gliaThemeTypeface(): Typeface? =
    gliaAttrResourceId(androidx.appcompat.R.attr.fontFamily)?.let { ResourcesCompat.getFont(this, it) }
