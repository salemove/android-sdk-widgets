@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.chat.*


/**
 * Default theme for Chat Engagement states
 */
internal fun ChatEngagementStatesTheme(
    pallet: ColorPallet
): EngagementStatesTheme? {
    val operatorTheme = OperatorTheme(pallet)

    return pallet.run {
        composeIfAtLeastOneNotNull(
            operatorTheme,
            primaryColorTheme,
            baseLightColorTheme,
            baseDarkColorTheme
        ) {
            EngagementStatesTheme(
                operator = operatorTheme,
                queue = EngagementStateTheme(
                    title = baseDarkColorTheme,
                    description = baseNormalColorTheme
                ),
                connecting = EngagementStateTheme(
                    title = baseDarkColorTheme,
                    description = primaryColorTheme
                ),
                connected = EngagementStateTheme(
                    title = baseDarkColorTheme,
                    description = primaryColorTheme
                ),
                transferring = EngagementStateTheme(
                    title = baseDarkColorTheme,
                    description = baseDarkColorTheme
                )
            )
        }
    }
}

/**
 * Default theme for Call Engagement states
 */
internal fun CallEngagementStatesTheme(
    pallet: ColorPallet
): EngagementStatesTheme? {
    val operatorTheme = OperatorTheme(pallet)

    return pallet.run {
        composeIfAtLeastOneNotNull(operatorTheme, baseLightColorTheme) {
            val engagementState =
                EngagementStateTheme(title = baseLightColorTheme, description = baseLightColorTheme)
            EngagementStatesTheme(
                operator = operatorTheme,
                connecting = engagementState,
                connected = engagementState,
                onHold = engagementState
            )
        }
    }
}

/**
 * Default theme for Engagement state
 */
private fun EngagementStateTheme(
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
internal fun OperatorTheme(pallet: ColorPallet): OperatorTheme? {
    val userImage = UserImageTheme(pallet)

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
