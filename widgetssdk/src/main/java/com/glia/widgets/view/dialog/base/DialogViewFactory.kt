package com.glia.widgets.view.dialog.base

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.glia.widgets.UiTheme
import com.glia.widgets.view.dialog.alert.AlertDialogViewInflater
import com.glia.widgets.view.dialog.operatorendedengagement.OperatorEndedEngagementDialogViewInflater
import com.glia.widgets.view.dialog.option.OptionDialogViewInflater
import com.glia.widgets.view.dialog.option.ReversedOptionDialogViewInflater
import com.glia.widgets.view.dialog.option.VerticalOptionDialogViewInflater
import com.glia.widgets.view.dialog.option.VerticalReversedOptionDialogViewInflater
import com.glia.widgets.view.dialog.screensharing.ScreenSharingDialogViewInflater
import com.glia.widgets.view.dialog.screensharing.VerticalScreenSharingDialogViewInflater
import com.glia.widgets.view.dialog.update.UpgradeDialogViewInflater
import com.glia.widgets.view.dialog.update.VerticalUpgradeDialogViewInflater
import com.glia.widgets.view.unifiedui.deepMerge
import com.glia.widgets.view.unifiedui.theme.AlertThemeWrapper
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme

internal class DialogViewFactory(context: Context, uiTheme: UiTheme, unifiedTheme: UnifiedTheme?) {
    private val themeWrapper: AlertThemeWrapper = uiTheme.alertTheme(context).run {
        copy(theme = this.theme.deepMerge(unifiedTheme?.alertTheme)!!)
    }

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private val isVerticalAxis: Boolean = themeWrapper.theme.isVerticalAxis ?: false

    fun createView(type: DialogType): View = getInflater(type).view

    private fun getInflater(type: DialogType): DialogViewInflater<*, *> = when {
        type is DialogType.Option && isVerticalAxis -> VerticalOptionDialogViewInflater(layoutInflater, themeWrapper, type.payload)
        type is DialogType.Option -> OptionDialogViewInflater(layoutInflater, themeWrapper, type.payload)
        type is DialogType.ReversedOption && isVerticalAxis -> VerticalReversedOptionDialogViewInflater(layoutInflater, themeWrapper, type.payload)
        type is DialogType.ReversedOption -> ReversedOptionDialogViewInflater(layoutInflater, themeWrapper, type.payload)
        type is DialogType.Upgrade && isVerticalAxis -> VerticalUpgradeDialogViewInflater(layoutInflater, themeWrapper, type.payload)
        type is DialogType.Upgrade -> UpgradeDialogViewInflater(layoutInflater, themeWrapper, type.payload)
        type is DialogType.ScreenSharing && isVerticalAxis -> VerticalScreenSharingDialogViewInflater(layoutInflater, themeWrapper, type.payload)
        type is DialogType.ScreenSharing -> ScreenSharingDialogViewInflater(layoutInflater, themeWrapper, type.payload)
        type is DialogType.OperatorEndedEngagement -> OperatorEndedEngagementDialogViewInflater(layoutInflater, themeWrapper, type.payload)
        type is DialogType.AlertDialog -> AlertDialogViewInflater(layoutInflater, themeWrapper, type.payload)
        else -> throw UnsupportedOperationException("Dialog of unsupported -> $type type was requested")
    }

}
