package com.glia.widgets.permissions

import com.glia.androidsdk.GliaException

internal typealias PermissionsRequestResult = (result: Map<String, Boolean>?, exception: GliaException?) -> Unit

internal typealias PermissionsGrantedCallback = (granted: Boolean) -> Unit
