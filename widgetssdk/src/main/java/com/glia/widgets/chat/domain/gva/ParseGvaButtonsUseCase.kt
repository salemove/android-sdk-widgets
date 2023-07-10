package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.model.Gva
import com.glia.widgets.chat.model.GvaButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject

internal class ParseGvaButtonsUseCase(private val gson: Gson) {
    operator fun invoke(metadata: JSONObject?): List<GvaButton> = metadata?.optString(Gva.Keys.OPTIONS)?.let {
        gson.fromJson(it, object : TypeToken<List<GvaButton>>() {}.type)
    } ?: emptyList()
}
