package com.glia.widgets.view.unifiedui.theme.call

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.SnackBarTheme
import com.glia.widgets.view.unifiedui.theme.base.HeaderTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.chat.EngagementStatesTheme

internal data class CallTheme(
    val background: LayerTheme? = null,
    val bottomText: TextTheme? = null,
    val buttonBar: ButtonBarTheme? = null,
    val duration: TextTheme? = null,
    val header: HeaderTheme? = null,
    val operator: TextTheme? = null,
    val topText: TextTheme? = null,
    val connect: EngagementStatesTheme? = null,
    val snackBar: SnackBarTheme? = null,
    val visitorVideo: VisitorVideoTheme? = null,
    val mediaQualityIndicator: TextTheme? = null
) : Mergeable<CallTheme> {
    override fun merge(other: CallTheme): CallTheme = CallTheme(
        background = background merge other.background,
        bottomText = bottomText merge other.bottomText,
        buttonBar = buttonBar merge other.buttonBar,
        duration = duration merge other.duration,
        header = header merge other.header,
        operator = operator merge other.operator,
        topText = topText merge other.topText,
        connect = connect merge other.connect,
        snackBar = snackBar merge other.snackBar,
        visitorVideo = visitorVideo merge other.visitorVideo,
        mediaQualityIndicator = mediaQualityIndicator merge other.mediaQualityIndicator
    )
}
