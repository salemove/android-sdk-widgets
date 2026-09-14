package com.glia.widgets.helper

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import androidx.annotation.AttrRes
import androidx.annotation.StyleableRes
import androidx.core.content.res.ResourcesCompat
import com.glia.widgets.R

internal object Utils {

    fun getTypedArrayStringValue(
        typedArray: TypedArray,
        @StyleableRes index: Int
    ): String? {
        return if (typedArray.hasValue(index)) {
            typedArray.getString(index)
        } else null
    }

    fun getTypedArrayIntegerValue(
        typedArray: TypedArray,
        context: Context,
        @StyleableRes index: Int,
        @AttrRes defaultValue: Int
    ): Int {
        return if (typedArray.hasValue(index)) {
            typedArray.getResourceId(index, 0)
        } else {
            context.gliaAttrResourceId(defaultValue) ?: 0
        }
    }

    fun getFont(typedArray: TypedArray, context: Context): Typeface? {
        val resId = getTypedArrayIntegerValue(
            typedArray,
            context,
            R.styleable.GliaView_android_fontFamily,
            androidx.appcompat.R.attr.fontFamily
        )
        return if (resId > 0) {
            ResourcesCompat.getFont(context, resId)
        } else null
    }
}
