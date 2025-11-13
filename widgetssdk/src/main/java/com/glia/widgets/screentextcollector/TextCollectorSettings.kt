package com.glia.widgets.screentextcollector

/**
 * Configuration options for TextCollector.
 */
internal data class TextCollectorSettings(
    // Ensure views are drawn - collect texts from screen after 'collectionDelayMs'.
    val collectionDelayMs: Long = 800L,

    // Remove screen texts from history if visitor was on that screen less than 'minCollectionIntervalMs'.
    val minCollectionIntervalMs: Long = 1000L,

    // Texts for 'maxHistorySize' screens are kept in the history
    val maxHistorySize: Int = 5
)
