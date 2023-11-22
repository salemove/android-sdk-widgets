package com.glia.widgets.core.dialog.model

data class ConfirmationDialogLinks(
    val link1: Link? = null,
    val link2: Link? = null
)

data class Link(
    val title: String,
    val url: String
)
