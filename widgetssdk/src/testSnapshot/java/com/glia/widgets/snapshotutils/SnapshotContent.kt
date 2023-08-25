package com.glia.widgets.snapshotutils

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import androidx.annotation.RawRes

interface SnapshotContent {
    val context: Context
    val resources: Resources
    val layoutInflater: LayoutInflater

    fun rawRes(@RawRes resId: Int): String
}
