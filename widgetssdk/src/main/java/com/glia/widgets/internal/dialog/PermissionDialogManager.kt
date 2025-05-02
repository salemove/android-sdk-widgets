package com.glia.widgets.internal.dialog

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

private const val PREF_KEY = "dialog_permission_manager"
private const val OVERLAY_PERMISSION_DIALOG_SHOWN_KEY = "OVERLAY_PERMISSION_DIALOG_SHOWN_KEY"

internal class PermissionDialogManager(context: Context) {
    private val sharedPreferences: SharedPreferences by lazy { context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE) }

    fun setOverlayPermissionDialogShown() {
        sharedPreferences.edit { putBoolean(OVERLAY_PERMISSION_DIALOG_SHOWN_KEY, true) }
    }

    fun hasOverlayPermissionDialogShown(): Boolean = sharedPreferences.getBoolean(OVERLAY_PERMISSION_DIALOG_SHOWN_KEY, false)

}
