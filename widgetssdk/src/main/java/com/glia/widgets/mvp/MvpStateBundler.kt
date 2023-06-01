package com.glia.widgets.mvp

import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

/**
 * Default implementation of writing and reading state from an Android Bundle.
 * Default types are supported and more can be added here
 */
internal class MvpStateBundler<STATE : Any>(val key: String) : StateBundler<STATE> {
    override fun write(bundle: Bundle, value: STATE) {
        when (value) {
            is Parcelable -> bundle.putParcelable(key, value)
            is Serializable -> bundle.putSerializable(key, value)
            is String -> bundle.putString(key, value)
            is CharSequence -> bundle.putCharSequence(key, value)
            is Int -> bundle.putInt(key, value)
            is Long -> bundle.putLong(key, value)
            is Float -> bundle.putFloat(key, value)
            is Double -> bundle.putDouble(key, value)
            is Boolean -> bundle.putBoolean(key, value)
            else -> throw IllegalArgumentException("Unknown state type. ${value::class.java} cannot be put into a bundle.")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun read(bundle: Bundle): STATE? {
        // TODO Needs improvement
        return bundle.get(key) as STATE?
    }
}
