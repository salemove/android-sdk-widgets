package com.glia.widgets.filepreview.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.glia.widgets.R

class FilePreviewView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    init {
        contentDescription =
            context.getString(R.string.glia_preview_activity_image_content_description)
        scaleType = ScaleType.FIT_CENTER
        transitionName = context.getString(R.string.glia_file_preview_transition_name)
    }
}
