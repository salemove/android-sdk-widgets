package com.glia.widgets.filepreview.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.glia.widgets.R
import com.glia.widgets.di.Dependencies

class FilePreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private val stringProvider = Dependencies.getStringProvider()

    init {
        contentDescription =
            stringProvider.getRemoteString(R.string.android_image_preview_accessibility)
        scaleType = ScaleType.FIT_CENTER
        transitionName = context.getString(R.string.glia_file_preview_transition_name)
    }
}
