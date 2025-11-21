package com.glia.widgets.internal.fileupload

import android.content.Context
import android.content.Intent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.glia.widgets.Constants

/**
 * @hide
 */
internal class PickVisualMediaMultipleMimeTypes : ActivityResultContracts.PickVisualMedia() {

    var mimeTypes: List<String> = listOf(Constants.MIME_TYPE_IMAGES)

    override fun createIntent(context: Context, input: PickVisualMediaRequest): Intent {

        // Get the original Intent created by the superclass
        val intent = super.createIntent(context, input)

        // Set the EXTRA_MIME_TYPES
        // This array tells the picker which specific types to show/allow.
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes.toTypedArray())

        // The Intent's primary type MUST be the wildcard when using EXTRA_MIME_TYPES
        // for multiple types. The framework uses the EXTRA_MIME_TYPES list to filter.
        intent.type = "*/*"

        return intent
    }
}
