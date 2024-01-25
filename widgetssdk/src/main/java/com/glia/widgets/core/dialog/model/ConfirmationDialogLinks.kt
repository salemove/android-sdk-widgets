package com.glia.widgets.core.dialog.model

internal data class ConfirmationDialogLinks(
    val link1: Link? = null,
    val link2: Link? = null
)

internal data class Link(
    val title: String,
    val url: String
)
