package com.glia.widgets.helper

import android.content.res.ColorStateList
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes

internal interface IResourceProvider {
    fun getString(@StringRes id: Int): String

    fun getString(@StringRes id: Int, vararg formatArgs: Any?): String

    @ColorInt
    fun getColor(@ColorRes id: Int): Int?

    fun getColorStateList(id: Int): ColorStateList?

    fun getDimension(dimensionId: Int): Int

    fun getDimensionPixelSize(dimensionId: Int): Int

    fun convertDpToPixel(dp: Float): Float

    fun convertSpToPixel(sp: Float): Float

    fun convertDpToIntPixel(dp: Float): Int

    fun getResourceKey(@StringRes stringKey: Int): String
}
