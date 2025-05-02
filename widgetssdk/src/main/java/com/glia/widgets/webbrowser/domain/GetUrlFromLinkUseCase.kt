package com.glia.widgets.webbrowser.domain

import com.glia.widgets.internal.dialog.model.Link
import com.glia.widgets.locale.LocaleProvider

internal interface GetUrlFromLinkUseCase {
    operator fun invoke(link: Link): String?
}

internal class GetUrlFromLinkUseCaseImpl(
    private val localeProvider: LocaleProvider
) : GetUrlFromLinkUseCase {
    override operator fun invoke(link: Link): String? {
        val string = localeProvider.getString(link.url)
        if (string.isBlank()) {
            return null
        }
        return string
    }
}
