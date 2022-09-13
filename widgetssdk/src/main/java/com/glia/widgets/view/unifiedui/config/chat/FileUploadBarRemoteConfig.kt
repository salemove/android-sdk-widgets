package com.glia.widgets.view.unifiedui.config.chat

import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.theme.chat.FileUploadBarTheme
import com.google.gson.annotations.SerializedName

internal data class FileUploadBarRemoteConfig(
    @SerializedName("filePreview")
    val filePreviewRemoteConfig: FilePreviewRemoteConfig?,

    @SerializedName("uploading")
    val uploading: FileUploadRemoteConfig?,

    @SerializedName("uploaded")
    val uploaded: FileUploadRemoteConfig?,

    @SerializedName("error")
    val error: FileUploadRemoteConfig?,

    @SerializedName("progress")
    val progress: ColorLayerRemoteConfig?,

    @SerializedName("errorProgress")
    val errorProgress: ColorLayerRemoteConfig?,

    @SerializedName("progressBackground")
    val progressBackground: ColorLayerRemoteConfig?,

    @SerializedName("removeButton")
    val removeButton: ColorLayerRemoteConfig?
) {
    fun toFileUploadBarTheme(): FileUploadBarTheme = FileUploadBarTheme(
        filePreview = filePreviewRemoteConfig?.toFilePreviewTheme(),
        uploading = uploading?.toUploadFileTheme(),
        uploaded = uploaded?.toUploadFileTheme(),
        error = error?.toUploadFileTheme(),
        progress = progress?.toColorTheme(),
        errorProgress = errorProgress?.toColorTheme(),
        progressBackground = progressBackground?.toColorTheme(),
        removeButton = removeButton?.toColorTheme()
    )
}
