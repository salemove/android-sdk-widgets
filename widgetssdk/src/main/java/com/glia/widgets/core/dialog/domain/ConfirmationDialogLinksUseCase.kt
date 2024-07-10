package com.glia.widgets.core.dialog.domain

import androidx.annotation.VisibleForTesting
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.R
import com.glia.widgets.core.dialog.model.Link
import com.glia.widgets.core.dialog.model.ConfirmationDialogLinks
import com.glia.widgets.locale.LocaleProvider

internal class ConfirmationDialogLinksUseCase(
    private val localeProvider: LocaleProvider
) {

    operator fun invoke(): ConfirmationDialogLinks {
        val link1Title = LocaleString(R.string.engagement_confirm_link1_text)
        val link1Url = localeProvider.getString(R.string.engagement_confirm_link1_url)
        val link2Title = LocaleString(R.string.engagement_confirm_link2_text)
        val link2Url = localeProvider.getString(R.string.engagement_confirm_link2_url)
        return ConfirmationDialogLinks(
            link1 = makeLink(link1Title, link1Url),
            link2 = makeLink(link2Title, link2Url)
        )
    }

    @VisibleForTesting
    fun makeLink(title: LocaleString, url: String?): Link? {
        if (url.isNullOrEmpty()) {
            return null
        }
        return Link(title, url)
    }
}
