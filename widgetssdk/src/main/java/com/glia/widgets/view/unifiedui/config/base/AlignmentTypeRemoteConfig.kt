package com.glia.widgets.view.unifiedui.config.base

import android.os.Parcelable
import android.widget.TextView
import kotlinx.parcelize.Parcelize

/**
 * Represents Alignment from remote config.
 *
 * @see [com.glia.widgets.view.unifiedui.parse.AlignmentDeserializer]
 */
@Parcelize
internal enum class AlignmentTypeRemoteConfig(val type: String) : Parcelable {
    LEADING("leading"),
    CENTER("center"),
    TRAILING("trailing");

    val nativeAlignment: Int
        get() = when (this) {
            LEADING -> TextView.TEXT_ALIGNMENT_TEXT_START
            CENTER -> TextView.TEXT_ALIGNMENT_CENTER
            TRAILING -> TextView.TEXT_ALIGNMENT_TEXT_END
        }
}
