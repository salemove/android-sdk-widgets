package com.glia.widgets.helper

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.queuing.Queue

fun Queue.supportsMessaging() = state.medias.contains(Engagement.MediaType.MESSAGING)