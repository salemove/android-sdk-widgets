package com.glia.widgets

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.glia.telemetry_lib.GliaTelemetry
import com.glia.telemetry_lib.OtelConfig
import com.glia.widgets.di.Dependencies

/**
 * @hide
 */
open class InitializationProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        GliaTelemetry.init(OtelConfig("https://watchtower.beta.glia.com:4318", this.context))
        (context as? Application)?.let {
            Dependencies.onAppCreate(it)
            GliaWidgets.setupRxErrorHandler()
        }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(
        uri: Uri,
        values: ContentValues?
    ): Uri? {
        return null
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs:
        Array<out String>?
    ): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0
    }
}
