package com.glia.widgets.liveobservation

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IdRes
import com.glia.widgets.R
import com.glia.widgets.StringProvider
import com.glia.widgets.view.snackbar.SnackBarDelegateFactory
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.google.android.apps.common.testing.accessibility.framework.replacements.LayoutParams
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import kotlin.reflect.KClass

internal class SnackBarTestHelper<T : Activity>(private val context: Context, private val kClass: KClass<T>) {
    private val rootLayout: FrameLayout by lazy {
        FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
    }

    private lateinit var stringProvider: StringProvider
    private lateinit var mockActivity: T

    fun setUp() {
        stringProvider = mockk()
        every { stringProvider.getRemoteString(any()) } answers { context.getString(R.string.live_observation_indicator_message) }

        mockActivity = mockkClass(kClass)
        every { mockActivity.findViewById<View>(any()) } answers { rootLayout }
    }

    fun tearDown() {
        clearMocks(mockActivity)
        rootLayout.removeAllViews()
    }

    fun getView(unifiedTheme: UnifiedTheme?): View = SnackBarDelegateFactory(mockActivity, stringProvider, unifiedTheme)
        .createDelegate()
        .apply { addViewToRoot(anchorViewId ?: return@apply) }
        .snackBar
        .view

    private fun addViewToRoot(@IdRes id: Int) {
        View(context).apply { this.id = id }.also(rootLayout::addView)
    }

}
