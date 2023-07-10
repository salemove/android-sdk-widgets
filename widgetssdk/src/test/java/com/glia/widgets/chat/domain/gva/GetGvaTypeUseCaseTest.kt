package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.model.Gva
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetGvaTypeUseCaseTest {
    private lateinit var useCase: GetGvaTypeUseCase

    @Before
    fun setUp() {
        useCase = GetGvaTypeUseCase()
    }

    @Test
    fun `invoke returns appropriate type when present`() {
        val jsonObject = JSONObject().put(Gva.Keys.TYPE, Gva.Type.PLAIN_TEXT.value)
        assertEquals(Gva.Type.PLAIN_TEXT, useCase(jsonObject))

        jsonObject.put(Gva.Keys.TYPE, Gva.Type.PERSISTENT_BUTTONS.value)
        assertEquals(Gva.Type.PERSISTENT_BUTTONS, useCase(jsonObject))

        jsonObject.put(Gva.Keys.TYPE, Gva.Type.QUICK_REPLIES.value)
        assertEquals(Gva.Type.QUICK_REPLIES, useCase(jsonObject))

        jsonObject.put(Gva.Keys.TYPE, Gva.Type.GALLERY_CARDS.value)
        assertEquals(Gva.Type.GALLERY_CARDS, useCase(jsonObject))
    }

    @Test
    fun `invoke returns null when gva type not found`() {
        val jsonObject = JSONObject().put(Gva.Keys.TYPE, "asdfg")
        assertNull(useCase(jsonObject))
    }
}
