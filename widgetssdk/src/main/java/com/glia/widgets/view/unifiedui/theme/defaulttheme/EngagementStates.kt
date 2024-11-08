@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.chat.EngagementStateTheme
import com.glia.widgets.view.unifiedui.theme.chat.EngagementStatesTheme
import com.glia.widgets.view.unifiedui.theme.chat.OnHoldOverlayTheme
import com.glia.widgets.view.unifiedui.theme.chat.OperatorTheme

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
            lightColorTheme,
            darkColorTheme
        ) {
            EngagementStatesTheme(
                operator = operatorTheme,
                queue = EngagementStateTheme(
                    title = darkColorTheme,
                    description = normalColorTheme
                ),
                connecting = EngagementStateTheme(
                    title = darkColorTheme,
                    description = primaryColorTheme
                ),
                connected = EngagementStateTheme(
                    title = darkColorTheme,
                    description = primaryColorTheme
                ),
                transferring = EngagementStateTheme(
                    title = darkColorTheme,
                    description = darkColorTheme
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
        composeIfAtLeastOneNotNull(operatorTheme, lightColorTheme) {
            val engagementState =
                EngagementStateTheme(title = lightColorTheme, description = lightColorTheme)
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
        composeIfAtLeastOneNotNull(userImage, primaryColorTheme, lightColorTheme) {
            OperatorTheme(
                image = userImage,
                animationColor = primaryColorTheme,
                onHoldOverlay = OnHoldOverlayTheme(tintColor = lightColorTheme)
            )
        }
    }
}
