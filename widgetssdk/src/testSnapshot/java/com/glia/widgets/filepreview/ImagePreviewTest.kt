package com.glia.widgets.filepreview

import android.view.MenuInflater
import androidx.annotation.DrawableRes
import com.android.ide.common.rendering.api.SessionParams
import com.glia.widgets.R
import com.glia.widgets.SnapshotTest
import com.glia.widgets.databinding.ImagePreviewActivityBinding
import com.glia.widgets.snapshotutils.SnapshotProviders
import org.junit.Test

internal class ImagePreviewTest : SnapshotTest(
    renderingMode = SessionParams.RenderingMode.NORMAL,
    theme = "Application_Glia_ImagePreview_Activity"
), SnapshotProviders {

    @Test
    fun defaultView() {
        snapshot(
            setupView().root
        )
    }

    fun setupView(
        title: String? = "Snapshot preview",
        showDownloadIcon: Boolean = true,
        showShareIcon: Boolean = false,
        @DrawableRes imageRes: Int? = R.drawable.test_banner
    ): ImagePreviewActivityBinding {
        localeProviderMock()

        val imagePreviewActivityBinding = ImagePreviewActivityBinding.inflate(layoutInflater)

        imagePreviewActivityBinding.toolbar.title = title

        val menu = imagePreviewActivityBinding.toolbar.menu
        MenuInflater(context).inflate(R.menu.menu_file_preview, menu)
        menu.findItem(R.id.save_item).also {
            it.isVisible = showDownloadIcon
        }
        menu.findItem(R.id.share_item).also {
            it.isVisible = showShareIcon
        }

        imageRes?.also { imagePreviewActivityBinding.previewView.setImageResource(it) }

        return imagePreviewActivityBinding
    }

}
