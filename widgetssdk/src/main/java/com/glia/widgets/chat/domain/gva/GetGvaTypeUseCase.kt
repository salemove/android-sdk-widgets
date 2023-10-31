package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.model.Gva
import org.json.JSONObject

internal class GetGvaTypeUseCase {

    operator fun invoke(metadata: JSONObject): Gva.Type? = Gva.Type.values().firstOrNull { it.value == metadata.optString(Gva.Keys.TYPE) }
}
