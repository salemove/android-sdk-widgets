package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.model.Gva
import com.glia.widgets.chat.model.GvaGalleryCard
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject

internal class ParseGvaGalleryCardsUseCase(private val gson: Gson) {
    operator fun invoke(metadata: JSONObject?): List<GvaGalleryCard> = metadata?.optString(Gva.Keys.GALLERY_CARDS)?.let {
        gson.fromJson(it, object : TypeToken<List<GvaGalleryCard>>() {}.type)
    } ?: emptyList()
}
