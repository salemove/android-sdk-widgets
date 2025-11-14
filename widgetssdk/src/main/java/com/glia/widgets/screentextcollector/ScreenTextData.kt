package com.glia.widgets.screentextcollector

/**
 * Data class to store screen text information
 */
internal data class ScreenTextData(
    val screenName: String,
    val timestamp: Long,
    val texts: List<String>,
    val screenType: ScreenType,
    val contentHash: String
)

/**
 * Enum representing the type of screen where text was collected.
 * @property ACTIVITY Text collected from an Activity screen
 * @property FRAGMENT Text collected from a Fragment screen
 * @property CONTENT_CHANGE Text collected when screen content changed dynamically
 * @property DIALOG Text collected from a Dialog screen
 */
internal enum class ScreenType {
    ACTIVITY,
    FRAGMENT,
    CONTENT_CHANGE,
    DIALOG
}
