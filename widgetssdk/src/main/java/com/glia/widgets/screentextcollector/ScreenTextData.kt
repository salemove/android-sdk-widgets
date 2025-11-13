package com.glia.widgets.screentextcollector

/**
 * Data class to store screen text information
 */
data class ScreenTextData(
    val screenName: String,
    val timestamp: Long,
    val texts: List<String>,
    val screenType: ScreenType,
    val contentHash: String
)

enum class ScreenType {
    ACTIVITY,
    FRAGMENT,
    CONTENT_CHANGE,
    DIALOG
}
