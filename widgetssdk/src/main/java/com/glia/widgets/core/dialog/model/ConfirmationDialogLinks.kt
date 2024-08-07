package com.glia.widgets.core.dialog.model

import com.glia.widgets.locale.LocaleString

internal data class ConfirmationDialogLinks(
    val link1: Link,
    val link2: Link
)

internal data class Link(
    val title: LocaleString,
    val url: LocaleString
)
