package com.glia.widgets.view.unifiedui.theme

import com.glia.widgets.view.unifiedui.theme.alert.AlertTheme
import com.glia.widgets.view.unifiedui.theme.bubble.BubbleTheme
import com.glia.widgets.view.unifiedui.theme.call.CallTheme
import com.glia.widgets.view.unifiedui.theme.callvisulaizer.CallVisualizerTheme
import com.glia.widgets.view.unifiedui.theme.chat.ChatTheme
import com.glia.widgets.view.unifiedui.theme.callvisulaizer.EndScreenSharingTheme
import com.glia.widgets.view.unifiedui.theme.survey.SurveyTheme

internal data class UnifiedTheme(
    val alertTheme: AlertTheme?,
    val bubbleTheme: BubbleTheme?,
    val callTheme: CallTheme?,
    val chatTheme: ChatTheme?,
    val surveyTheme: SurveyTheme?,
    val callVisualizerTheme: CallVisualizerTheme?
)
