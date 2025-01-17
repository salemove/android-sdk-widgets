package com.glia.widgets.view.dialog.base

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.glia.widgets.UiTheme
import com.glia.widgets.view.dialog.alert.AlertDialogViewInflater
import com.glia.widgets.view.dialog.confirmation.ConfirmationDialogViewInflater
import com.glia.widgets.view.dialog.confirmation.VerticalConfirmationDialogViewInflater
import com.glia.widgets.view.dialog.operatorendedengagement.OperatorEndedEngagementDialogViewInflater
import com.glia.widgets.view.dialog.option.OptionDialogViewInflater
import com.glia.widgets.view.dialog.option.OptionWithNegativeNeutralDialogViewInflater
import com.glia.widgets.view.dialog.option.ReversedOptionDialogViewInflater
import com.glia.widgets.view.dialog.option.VerticalOptionDialogViewInflater
import com.glia.widgets.view.dialog.option.VerticalOptionWithNegativeNeutralDialogViewInflater
import com.glia.widgets.view.dialog.option.VerticalReversedOptionDialogViewInflater
import com.glia.widgets.view.dialog.screensharing.ScreenSharingDialogViewInflater
import com.glia.widgets.view.dialog.screensharing.VerticalScreenSharingDialogViewInflater
import com.glia.widgets.view.dialog.update.UpgradeDialogViewInflater
import com.glia.widgets.view.dialog.update.VerticalUpgradeDialogViewInflater
import com.glia.widgets.view.unifiedui.nullSafeMerge
import com.glia.widgets.view.unifiedui.theme.AlertDialogConfiguration
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme

internal class DialogViewFactory(context: Context, uiTheme: UiTheme, unifiedTheme: UnifiedTheme?) {
    private val configuration: AlertDialogConfiguration = uiTheme.alertTheme(context).run {
        copy(theme = theme nullSafeMerge unifiedTheme?.alertTheme)
    }

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private val isVerticalAxis: Boolean = configuration.theme.isVerticalAxis ?: false

    fun createView(type: DialogType): View = getInflater(type).view

    private fun getInflater(type: DialogType): DialogViewInflater<*, *> = when {
        type is DialogType.Option && isVerticalAxis -> VerticalOptionDialogViewInflater(layoutInflater, configuration, type.payload)
        type is DialogType.Option -> OptionDialogViewInflater(layoutInflater, configuration, type.payload)
        type is DialogType.ReversedOption && isVerticalAxis -> VerticalReversedOptionDialogViewInflater(layoutInflater, configuration, type.payload)
        type is DialogType.ReversedOption -> ReversedOptionDialogViewInflater(layoutInflater, configuration, type.payload)
        type is DialogType.OptionWithNegativeNeutral && isVerticalAxis -> VerticalOptionWithNegativeNeutralDialogViewInflater(layoutInflater, configuration, type.payload)
        type is DialogType.OptionWithNegativeNeutral -> OptionWithNegativeNeutralDialogViewInflater(layoutInflater, configuration, type.payload)
        type is DialogType.Confirmation && isVerticalAxis -> VerticalConfirmationDialogViewInflater(layoutInflater, configuration, type.payload)
        type is DialogType.Confirmation -> ConfirmationDialogViewInflater(layoutInflater, configuration, type.payload)
        type is DialogType.Upgrade && isVerticalAxis -> VerticalUpgradeDialogViewInflater(layoutInflater, configuration, type.payload)
        type is DialogType.Upgrade -> UpgradeDialogViewInflater(layoutInflater, configuration, type.payload)
        type is DialogType.ScreenSharing && isVerticalAxis -> VerticalScreenSharingDialogViewInflater(layoutInflater, configuration, type.payload)
        type is DialogType.ScreenSharing -> ScreenSharingDialogViewInflater(layoutInflater, configuration, type.payload)
        type is DialogType.OperatorEndedEngagement -> OperatorEndedEngagementDialogViewInflater(layoutInflater, configuration, type.payload)
        type is DialogType.AlertDialog -> AlertDialogViewInflater(layoutInflater, configuration, type.payload)
        else -> throw UnsupportedOperationException("Dialog of unsupported -> $type type was requested")
    }

}
