package com.glia.widgets.view.unifiedui.config.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
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
): Parcelable
