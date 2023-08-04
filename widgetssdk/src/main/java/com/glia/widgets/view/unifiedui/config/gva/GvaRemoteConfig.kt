package com.glia.widgets.view.unifiedui.config.gva

import com.glia.widgets.view.unifiedui.config.base.ButtonRemoteConfig
import com.glia.widgets.view.unifiedui.theme.gva.GvaTheme
import com.google.gson.annotations.SerializedName

internal data class GvaRemoteConfig(
    @SerializedName("quickReplyButton")
    val quickReplyRemoteConfig: ButtonRemoteConfig?,

    @SerializedName("persistentButton")
    val persistentButtonRemoteConfig: GvaPersistentButtonRemoteConfig?,

    @SerializedName("galleryCard")
    val galleryCardRemoteConfig: GvaGalleryCardRemoteConfig?
) {
    fun toGvaTheme(): GvaTheme = GvaTheme(
        quickReplyTheme = quickReplyRemoteConfig?.toButtonTheme(),
        persistentButtonTheme = persistentButtonRemoteConfig?.toGvaPersistentButtonTheme(),
        galleryCardTheme = galleryCardRemoteConfig?.toGvaGalleryCardTheme()
    )
}
