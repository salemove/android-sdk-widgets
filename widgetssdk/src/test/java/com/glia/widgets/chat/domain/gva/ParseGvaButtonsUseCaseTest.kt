package com.glia.widgets.chat.domain.gva

import android.readRawResource
import com.glia.widgets.chat.model.Gva
import com.glia.widgets.chat.model.GvaButton
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ParseGvaButtonsUseCaseTest {
    private val gson: Gson = GsonBuilder().create()
    private lateinit var useCase: ParseGvaButtonsUseCase

    @Before
    fun setUp() {
        useCase = ParseGvaButtonsUseCase(gson)
    }

    @Test
    fun `invoke parses GvaButtons list when proper json passed`() {
        val jsonString = javaClass.readRawResource("gva_persistent_buttons.json")

        val jsonObject = JSONObject(jsonString).getJSONObject("metadata")

        val buttonsJson = jsonObject.getJSONArray(Gva.Keys.OPTIONS)
        val buttons = useCase(jsonObject)

        for (index in 0 until buttons.count()) {
            val buttonJson = buttonsJson.getJSONObject(index)
            val button = buttons[index]
            assertEquals(button.text, buttonJson["text"])
            assertEquals(button.url, buttonJson.opt("url"))
        }
    }

    @Test
    fun `invoke returns empty list when invalid JSON passed`() {

        val jsonObject = JSONObject()

        val buttons = useCase(jsonObject)

        assertEquals(buttons, emptyList<GvaButton>())

    }
}
