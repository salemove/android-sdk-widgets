@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.callvisulaizer.CallVisualizerTheme
import com.glia.widgets.view.unifiedui.theme.callvisulaizer.EndScreenSharingTheme
import com.glia.widgets.view.unifiedui.theme.callvisulaizer.VisitorCodeTheme

/**
 * Default theme for Call Visualizer flow
 */
internal fun CallVisualizerTheme(pallet: ColorPallet): CallVisualizerTheme =
    CallVisualizerTheme(
        endScreenSharingTheme = EndScreenSharingTheme(pallet),
        visitorCodeTheme = VisitorCodeTheme(pallet)
    )

/**
 * Default theme for EndScreenSharing screen
 */
internal fun EndScreenSharingTheme(pallet: ColorPallet): EndScreenSharingTheme =
    EndScreenSharingTheme(
        header = PrimaryColorHeaderTheme(pallet),
        endButton = NegativeDefaultButtonTheme(pallet),
        label = BaseDarkColorTextTheme(pallet),
        background = LayerTheme(fill = pallet.lightColorTheme)
    )

/**
 * Default theme for Visitor Code dialog
 */
internal fun VisitorCodeTheme(pallet: ColorPallet): VisitorCodeTheme =
    VisitorCodeTheme(
        numberSlotText = BaseDarkColorTextTheme(pallet),
        numberSlotBackground = LayerTheme(fill = pallet.lightColorTheme, stroke = pallet.shadeColorTheme?.primaryColor),
        closeButtonColor = pallet.normalColorTheme,
        refreshButton = PositiveDefaultButtonTheme(pallet),
        background = LayerTheme(fill = pallet.lightColorTheme),
        title = BaseDarkColorTextTheme(pallet),
        loadingProgressBar = pallet.primaryColorTheme
    )
