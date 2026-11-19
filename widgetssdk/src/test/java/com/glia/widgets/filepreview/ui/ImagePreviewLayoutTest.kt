package com.glia.widgets.filepreview.ui

import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.glia.widgets.R
import com.glia.widgets.databinding.ImagePreviewActivityBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.locale.LocaleProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
internal class ImagePreviewLayoutTest {

    @Test
    fun `back icon is tinted light so any integrator icon stays visible on the black toolbar`() {
        val application = RuntimeEnvironment.getApplication()
        Dependencies.localeProvider = LocaleProvider(ResourceProvider(application))
        val context = ContextThemeWrapper(application, R.style.Theme_Glia_Internal_ImagePreview)

        val binding = ImagePreviewActivityBinding.inflate(LayoutInflater.from(context))

        assertEquals(ContextCompat.getColor(context, R.color.glia_light_color), binding.toolbar.navigationIconTint)
    }
}
