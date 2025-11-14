package com.glia.widgets.screentextcollector

import java.util.concurrent.ConcurrentLinkedDeque

/**
 * Manages the history of collected screen text data for TextCollector.
 */
internal class ScreenTextHistory(private var maxHistorySize: Int) {
    private val screenTextsHistory = ConcurrentLinkedDeque<ScreenTextData>()

    fun setMaxScreenNumber(newSize: Int) {
        maxHistorySize = newSize
    }

    fun add(screenData: ScreenTextData) {
        screenTextsHistory.add(screenData)
        while (screenTextsHistory.size > maxHistorySize) {
            screenTextsHistory.removeFirst()
        }
    }

    fun getAll(): List<ScreenTextData> = screenTextsHistory.toList()
    fun getByName(screenName: String): ScreenTextData? = screenTextsHistory.firstOrNull { it.screenName == screenName }
    fun getMostRecent(): ScreenTextData? = screenTextsHistory.lastOrNull()
    fun removeMostRecent() {
        screenTextsHistory.removeLast()
    }
    fun clear() = screenTextsHistory.clear()
}

