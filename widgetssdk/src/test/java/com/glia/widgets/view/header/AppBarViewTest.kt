package com.glia.widgets.view.header

import android.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import com.glia.widgets.R
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.locale.LocaleProvider
import com.google.android.material.appbar.MaterialToolbar
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
internal class AppBarViewTest {

    private lateinit var appBarView: AppBarView

    @Before
    fun setUp() {
        val application = RuntimeEnvironment.getApplication()
        Dependencies.localeProvider = LocaleProvider(ResourceProvider(application))
        appBarView = AppBarView(ContextThemeWrapper(application, R.style.Theme_Glia_Internal))
    }

    @Test
    fun `back icon keeps the home button tint after it is shown again`() {
        appBarView.hideBackButton()
        appBarView.showBackButton()

        val toolbar = appBarView.findViewById<MaterialToolbar>(R.id.toolbar)
        assertEquals(ContextCompat.getColor(appBarView.context, R.color.glia_light_color), toolbar.navigationIconTint)
    }
}
