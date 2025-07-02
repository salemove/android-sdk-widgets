package com.glia.widgets.helper

import android.content.Intent
import android.os.Build
import android.os.Parcelable

@Suppress("deprecation")
inline fun <reified T : Parcelable> Intent.getParcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> getParcelableExtra(key) as? T
}
