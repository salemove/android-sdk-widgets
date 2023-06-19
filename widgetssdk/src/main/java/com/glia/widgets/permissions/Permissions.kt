package com.glia.widgets.permissions

internal data class Permissions(
    val requiredPermissions: List<String>,
    val additionalPermissions: List<String>
)
