package com.glia.widgets.messagecenter

import android.graphics.Color
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import kotlin.properties.Delegates

class SimpleStatefulWidgetAdapterTest {
    private val defaultState: State = State.ENABLED
    private val defaultTheme: Map<State, Theme> = mapOf(
        State.ENABLED to Theme(Color.WHITE, 10),
        State.DISABLED to Theme(Color.GRAY, 10),
        State.FOCUSED to Theme(Color.GREEN, 10)
    )

    private val newTheme = mapOf(
        State.ENABLED to Theme(Color.BLACK),
        State.DISABLED to Theme(size = -10),
    )

    data class Theme(val color: Int? = null, val size: Int? = null)
    enum class State {
        ENABLED,
        DISABLED,
        FOCUSED
    }

    private var adapter: StatefulWidgetAdapter<State, Theme> by Delegates.notNull()
    private var callback: StatefulWidgetAdapterCallback<State, Theme> by Delegates.notNull()

    @Before
    fun setUp() {
        callback = mock()
        adapter = SimpleStatefulWidgetAdapter(defaultState, callback)

        //Set default Theme
        adapter.updateStatefulTheme(defaultTheme)
        resetMocks()// reset and clear all mocks after setting the default theme
    }

    @After
    fun tearDown() {
        resetMocks()
    }

    @Test
    fun `updateStatefulTheme trigger onNewTheme when the not empty theme passed`() {
        adapter.updateStatefulTheme(newTheme)
        verify(callback).onNewTheme(any())
        verify(callback, never()).onNewState(any())
    }

    @Test
    fun `updateStatefulTheme doesn't trigger onNewTheme when the same theme passed`() {
        adapter.updateStatefulTheme(defaultTheme)
        verify(callback, never()).onNewState(any())
    }

    @Test
    fun `updateStatefulTheme doesn't trigger onNewTheme when the empty theme passed`() {
        adapter.updateStatefulTheme(emptyMap())
        verify(callback, never()).onNewState(any())
    }

    @Test
    fun `updateStatefulTheme trigger onNewTheme with merged theme when the different theme passed`() {
        adapter.updateStatefulTheme(newTheme)
        verify(callback).onNewTheme(defaultTheme[State.ENABLED]!!.copy(color = Color.BLACK))

        adapter.updateState(State.DISABLED)
        verify(callback).onNewTheme(defaultTheme[State.DISABLED]!!.copy(size = -10))
    }

    @Test
    fun `updateState doesn't trigger onNewState and onNewTheme when the same state passed`() {
        adapter.updateState(defaultState)
        verify(callback, never()).onNewState(any())
        verify(callback, never()).onNewTheme(any())
    }

    @Test
    fun `updateState trigger onNewState and onNewTheme when the state passed`() {
        adapter.updateState(State.DISABLED)
        verify(callback).onNewState(any())
        verify(callback).onNewTheme(any())
    }

    @Test
    fun `updateState trigger onNewTheme with correct theme when the state passed`() {
        adapter.updateState(State.DISABLED)
        verify(callback).onNewState(any())
        verify(callback).onNewTheme(defaultTheme[State.DISABLED]!!)
    }

    private fun resetMocks() {
        reset(callback)
        clearInvocations(callback)
    }
}