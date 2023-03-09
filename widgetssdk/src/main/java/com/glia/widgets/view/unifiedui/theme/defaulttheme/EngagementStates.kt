@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.exstensions.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.chat.*


/**
 * Default theme for Engagement states
 */
internal fun ChatEngagementStates(
    pallet: ColorPallet
): EngagementStatesTheme? {
    val operatorTheme = OperatorDefaultTheme(pallet)

    return pallet.run {
        composeIfAtLeastOneNotNull(
            operatorTheme,
            primaryColorTheme,
            baseLightColorTheme,
            baseDarkColorTheme
        ) {
            EngagementStatesTheme(
                operator = operatorTheme,
                queue = EngagementState(
                    title = baseDarkColorTheme,
                    description = baseNormalColorTheme
                ),
                connecting = EngagementState(
                    title = baseDarkColorTheme,
                    description = primaryColorTheme
                ),
                connected = EngagementState(
                    title = baseDarkColorTheme,
                    description = primaryColorTheme
                ),
                transferring = EngagementState(
                    title = baseDarkColorTheme,
                    description = baseDarkColorTheme
                )
            )
        }
    }
}

/**
 * Default theme for Engagement state
 */
private fun EngagementState(
    title: ColorTheme? = null,
    description: ColorTheme? = null
): EngagementStateTheme? = composeIfAtLeastOneNotNull(title, description) {
    EngagementStateTheme(
        title = TextTheme(textColor = title),
        description = TextTheme(textColor = description)
    )
}

/**
 * Default theme for Operator
 */
internal fun OperatorDefaultTheme(pallet: ColorPallet): OperatorTheme? {
    val userImage = UserImageDefaultTheme(pallet)

    return pallet.run {
        composeIfAtLeastOneNotNull(userImage, primaryColorTheme, baseLightColorTheme) {
            OperatorTheme(
                image = userImage,
                animationColor = primaryColorTheme,
                onHoldOverlay = OnHoldOverlayTheme(tintColor = baseLightColorTheme)
            )
        }
    }
}