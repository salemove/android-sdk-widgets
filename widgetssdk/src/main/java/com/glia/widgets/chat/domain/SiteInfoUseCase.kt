package com.glia.widgets.chat.domain

import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.site.SiteInfo
import com.glia.widgets.di.GliaCore

internal class SiteInfoUseCase(private val gliaCore: GliaCore) {
    operator fun invoke(callback: RequestCallback<SiteInfo?>) = gliaCore.getSiteInfo(callback)
}
