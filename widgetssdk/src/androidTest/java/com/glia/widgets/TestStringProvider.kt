package com.glia.widgets

import android.content.Context

class TestStringProvider(private val context: Context): StringProvider {

    override fun getRemoteString(stringKey: Int, vararg values: String?): String {
        return context.getString(stringKey, *values)
    }
}
