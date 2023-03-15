package com.glia.widgets.helper

fun String?.getFileExtensionOrEmpty() = this?.substringAfterLast('.', "")
    ?.uppercase()
    .orEmpty()