package com.glia.widgets.chat.domain.gva

import android.readRawResource
import com.glia.widgets.chat.model.Gva
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ParseGvaGalleryCardsUseCaseTest {
    private val gson: Gson = GsonBuilder().create()
    private lateinit var useCase: ParseGvaGalleryCardsUseCase

    @Before
    fun setUp() {
        useCase = ParseGvaGalleryCardsUseCase(gson)
    }

    @Test
    fun `invoke parses GvaGalleryCards list when proper json passed`() {
        val jsonString = javaClass.readRawResource("gva_gallery.json")

        val jsonObject = JSONObject(jsonString).getJSONObject("metadata")

        val galleryCardsJson = jsonObject.getJSONArray(Gva.Keys.GALLERY_CARDS)
        val galleryCards = useCase(jsonObject)

        for (index in 0 until galleryCards.count()) {
            val cardJson = galleryCardsJson.getJSONObject(index)
            val card = galleryCards[index]
            Assert.assertEquals(card.title, cardJson["title"])
            Assert.assertEquals(card.subtitle, cardJson.opt("subtitle"))

            val buttons = card.options
            val buttonsJson = cardJson.getJSONArray(Gva.Keys.OPTIONS)

            for (buttonIndex in 0 until buttons.count()) {
                val buttonJson = buttonsJson.getJSONObject(index)
                val button = buttons[index]
                Assert.assertEquals(button.text, buttonJson["text"])
                Assert.assertEquals(button.url, buttonJson.opt("url"))
            }
        }
    }
}
