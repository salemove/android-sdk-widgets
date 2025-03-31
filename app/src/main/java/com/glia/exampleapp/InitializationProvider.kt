package com.glia.exampleapp

import android.widget.Toast

class InitializationProvider : com.glia.widgets.InitializationProvider() {
    override fun onCreate(): Boolean {
        val startTimestamp = System.currentTimeMillis()
        super.onCreate()
        val endTimestamp = System.currentTimeMillis()

        context?.let {
            Toast.makeText(it, "onAppCreate time is ${endTimestamp - startTimestamp} ms", Toast.LENGTH_LONG).show()
        }
        return true
    }
}
