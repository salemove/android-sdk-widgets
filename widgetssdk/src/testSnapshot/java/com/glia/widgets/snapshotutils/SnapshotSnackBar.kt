package com.glia.widgets.snapshotutils

import android.app.Activity
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IdRes
import com.glia.widgets.R
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.view.snackbar.SnackBarDelegateFactory
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.google.android.apps.common.testing.accessibility.framework.replacements.LayoutParams
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import kotlin.reflect.KClass

internal interface SnapshotSnackBar : SnapshotContent, SnapshotTestLifecycle, SnapshotProviders {

    data class Mock<T>(
        val localeProvider: LocaleProvider,
        val rootLayout: FrameLayout,
        val mockActivity: T
    )

    fun <T : Activity> snackBarMock(kClass: KClass<T>): Mock<T> {
        val mockResources = mockk<Resources> {
            every { getDimensionPixelSize(any()) } returns context.resources.getDimensionPixelSize(R.dimen.glia_snack_bar_bottom_margin)
        }
        val rootLayout = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        val mockActivity = mockkClass(kClass)
        every { mockActivity.findViewById<View>(any()) } answers { rootLayout }
        every { mockActivity.resources } returns mockResources

        setOnEndListener {
            clearMocks(mockActivity)
            rootLayout.removeAllViews()
        }

        return Mock(localeProviderMock(), rootLayout, mockActivity)
    }

    fun <T : Activity> setupView(
        kClass: KClass<T>,
        unifiedTheme: UnifiedTheme? = null
    ): View = snackBarMock(kClass).run {
        SnackBarDelegateFactory(mockActivity, R.string.live_observation_indicator_message, localeProvider, unifiedTheme)
            .createDelegate()
            .apply { addViewToRoot(anchorViewId ?: return@apply, rootLayout) }
            .snackBar
            .view
    }

    private fun addViewToRoot(@IdRes id: Int, rootLayout: FrameLayout) {
        View(context).apply { this.id = id }.also(rootLayout::addView)
    }

}
