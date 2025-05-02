package com.glia.widgets.internal.dialog.domain

import com.glia.widgets.locale.LocaleString
import com.glia.widgets.R
import com.glia.widgets.internal.dialog.model.Link
import com.glia.widgets.internal.dialog.model.ConfirmationDialogLinks

internal class ConfirmationDialogLinksUseCase {

    operator fun invoke(): ConfirmationDialogLinks {
        val link1Title = LocaleString(R.string.engagement_confirm_link1_text)
        val link1Url = LocaleString(R.string.engagement_confirm_link1_url)
        val link2Title = LocaleString(R.string.engagement_confirm_link2_text)
        val link2Url = LocaleString(R.string.engagement_confirm_link2_url)
        return ConfirmationDialogLinks(
            link1 = Link(link1Title, link1Url),
            link2 = Link(link2Title, link2Url)
        )
    }
}
