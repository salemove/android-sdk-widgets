package com.glia.widgets.filepreview.ui

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.glia.widgets.R
import com.glia.widgets.base.GliaActivity
import com.glia.widgets.base.GliaFragmentContract
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.ExtraKeys
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.getParcelable
import com.glia.widgets.locale.LocaleString

/**
 * This activity hosts [ImagePreviewFragment] and serves as an entry point for image preview.
 *
 * **Architecture:** This Activity is a thin wrapper that hosts the Fragment. All UI logic
 * is implemented in [ImagePreviewFragment] and [ImagePreviewView]. This Activity handles Intent-based
 * launches for backwards compatibility.
 *
 * This activity is used to preview images shared in chat in full-screen.
 *
 * It will be automatically added to the integrator's manifest file by the manifest merger during compilation.
 *
 * @see ImagePreviewFragment
 * @see ImagePreviewView
 */
internal class ImagePreviewActivity : GliaActivity<ImagePreviewView>, AppCompatActivity(), GliaFragmentContract.Host {
    private val localeProvider = Dependencies.localeProvider
    private var imagePreviewFragment: ImagePreviewFragment? = null

    override val gliaView: ImagePreviewView
        get() = imagePreviewFragment?.gliaView as? ImagePreviewView ?: error("Fragment not initialized")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.i(TAG, "Create Image Preview screen")
        setContentView(R.layout.image_preview_activity_host)

        if (savedInstanceState == null) {
            val fragment = if (intent.hasExtra(ExtraKeys.IMAGE_PREVIEW_LOCAL_IMAGE_URI)) {
                val uri = intent.getParcelable<Uri>(ExtraKeys.IMAGE_PREVIEW_LOCAL_IMAGE_URI)
                    ?: error("Local image URI must be provided")
                ImagePreviewFragment.newInstanceLocal(uri)
            } else {
                val bitmapId = intent.getStringExtra(ExtraKeys.IMAGE_PREVIEW_IMAGE_ID)
                    ?: error("Image ID must be provided")
                val bitmapName = intent.getStringExtra(ExtraKeys.IMAGE_PREVIEW_IMAGE_NAME)
                    ?: error("Image name must be provided")
                ImagePreviewFragment.newInstance(bitmapId, bitmapName)
            }

            imagePreviewFragment = fragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        } else {
            imagePreviewFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? ImagePreviewFragment
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.i(TAG, "Destroy Image Preview screen")
    }

    override fun setHostTitle(locale: LocaleString?) {
        locale?.let { setTitle(localeProvider.getString(it)) }
    }

    override fun finish() = super.finish()
}
