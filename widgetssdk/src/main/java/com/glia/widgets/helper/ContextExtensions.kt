package com.glia.widgets.helper

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

fun Context.asActivity(): Activity? = (this as? ContextWrapper)?.let {
    it as? Activity ?: it.baseContext.asActivity()
}

fun Context.requireActivity(): Activity =
    asActivity() ?: throw IllegalStateException("Context $this is not an Activity.")
