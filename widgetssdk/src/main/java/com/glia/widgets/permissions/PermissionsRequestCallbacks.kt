package com.glia.widgets.permissions

import com.glia.androidsdk.GliaException

typealias PermissionsRequestResult = (result: Map<String, Boolean>?, exception: GliaException?) -> Unit

typealias PermissionsGrantedCallback = (granted: Boolean) -> Unit
