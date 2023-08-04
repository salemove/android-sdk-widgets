package com.glia.widgets.chat.domain.gva

import android.net.Uri
import com.glia.widgets.chat.model.Gva

internal class DetermineGvaUrlTypeUseCase {

    operator fun invoke(url: String): Gva.ButtonType {
        val uri = Uri.parse(url)
        return when (uri.scheme) {
            PHONE_SCHEME -> Gva.ButtonType.Phone(uri)
            EMAIL_SCHEME -> Gva.ButtonType.Email(uri)
            else -> Gva.ButtonType.Url(uri)
        }
    }
}
