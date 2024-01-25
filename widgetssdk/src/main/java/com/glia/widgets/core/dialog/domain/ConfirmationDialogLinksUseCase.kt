package com.glia.widgets.core.dialog.domain

import androidx.annotation.VisibleForTesting
import com.glia.widgets.R
import com.glia.widgets.StringProvider
import com.glia.widgets.core.dialog.model.Link
import com.glia.widgets.core.dialog.model.ConfirmationDialogLinks

internal class ConfirmationDialogLinksUseCase(
    private val stringProvider: StringProvider
) {

    operator fun invoke(): ConfirmationDialogLinks {
        val link1Title = stringProvider.getRemoteString(R.string.engagement_confirm_link1_text)
        val link1Url = stringProvider.getRemoteString(R.string.engagement_confirm_link1_url)
        val link2Title = stringProvider.getRemoteString(R.string.engagement_confirm_link2_text)
        val link2Url = stringProvider.getRemoteString(R.string.engagement_confirm_link2_url)
        return ConfirmationDialogLinks(
            link1 = makeLink(link1Title, link1Url),
            link2 = makeLink(link2Title, link2Url)
        )
    }

    @VisibleForTesting
    fun makeLink(title: String?, url: String?): Link? {
        if (title.isNullOrEmpty() || url.isNullOrEmpty()) {
            return null
        }
        return Link(title, url)
    }
}
