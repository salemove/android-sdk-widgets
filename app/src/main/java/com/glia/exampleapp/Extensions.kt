package com.glia.exampleapp

import android.content.Context
import androidx.annotation.RawRes
import java.io.BufferedReader

fun Context.rawRes(@RawRes resId: Int): String = resources.openRawResource(resId).use {
    BufferedReader(it.reader()).readText()
}
