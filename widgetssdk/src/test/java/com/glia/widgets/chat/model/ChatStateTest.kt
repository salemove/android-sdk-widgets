package com.glia.widgets.chat.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatStateTest {

    @Test
    fun `older history flags default to disabled and idle`() {
        val state = ChatState()

        assertFalse(state.canLoadOlderHistory)
        assertFalse(state.isLoadingOlderHistory)
    }

    @Test
    fun `loadingOlderHistory marks the load in progress and keeps availability`() {
        val state = ChatState(canLoadOlderHistory = true).loadingOlderHistory()

        assertTrue(state.isLoadingOlderHistory)
        assertTrue(state.canLoadOlderHistory)
    }

    @Test
    fun `olderHistoryAvailabilityChanged stops loading and stores availability`() {
        val loading = ChatState(canLoadOlderHistory = true).loadingOlderHistory()

        val more = loading.olderHistoryAvailabilityChanged(true)
        assertFalse(more.isLoadingOlderHistory)
        assertTrue(more.canLoadOlderHistory)

        val exhausted = loading.olderHistoryAvailabilityChanged(false)
        assertFalse(exhausted.isLoadingOlderHistory)
        assertFalse(exhausted.canLoadOlderHistory)
    }
}
