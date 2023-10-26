package com.glia.widgets.view.snackbar

import android.app.Activity
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.annotation.VisibleForTesting
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import com.glia.widgets.Constants
import com.glia.widgets.R
import com.glia.widgets.StringProvider
import com.glia.widgets.call.CallActivity
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.insetsController
import com.glia.widgets.helper.rootView
import com.glia.widgets.helper.rootWindowInsetsCompat
import com.glia.widgets.view.unifiedui.theme.SnackBarTheme
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.google.android.material.snackbar.Snackbar

internal abstract class SnackBarDelegate(
    view: View, stringProvider: StringProvider, snackBarTheme: SnackBarTheme?
) {

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    @get:IdRes
    open val anchorViewId: Int? = null

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    open val marginBottom: Int? = null

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    @get:ColorRes
    open val fallbackBackgroundColor: Int = R.color.glia_base_dark_color

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    @get:ColorRes
    open val fallbackTextColor: Int = R.color.glia_base_light_color

    @VisibleForTesting
    val snackBar: Snackbar by lazy { makeSnackBar(view, stringProvider, snackBarTheme) }

    fun show() = snackBar.show()

    private fun makeSnackBar(view: View, stringProvider: StringProvider, snackBarTheme: SnackBarTheme?): Snackbar {
        val bgColor = snackBarTheme?.backgroundColorTheme?.primaryColor ?: view.getColorCompat(fallbackBackgroundColor)
        val textColor = snackBarTheme?.textColorTheme?.primaryColor ?: view.getColorCompat(fallbackTextColor)
        val message = stringProvider.getRemoteString(R.string.live_observation_indicator_message)

        return Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(bgColor)
            .setTextColor(textColor)
            .also { ViewCompat.setElevation(it.view, Constants.WIDGETS_SDK_LAYER_ELEVATION.plus(1)) }
            .apply { anchorViewId?.also { setAnchorView(it) } }
            .apply { marginBottom?.also { updateBottomMargin(this, it) } }
    }

    private fun updateBottomMargin(snackBar: Snackbar, margin: Int) {
        snackBar.view.updateLayoutParams<MarginLayoutParams> {
            updateMargins(bottom = margin)
        }
    }
}

@VisibleForTesting
internal class CommonSnackBarDelegate(activity: Activity, stringProvider: StringProvider, unifiedTheme: UnifiedTheme?) :
    SnackBarDelegate(activity.rootView, stringProvider, unifiedTheme?.snackBarTheme) {
    override val marginBottom: Int = calculateBottomMargin(activity.rootView)

    private fun calculateBottomMargin(view: View): Int {
        val defaultMargin = view.resources.getDimensionPixelSize(R.dimen.glia_snack_bar_bottom_margin)
        val insetBottom = view.takeIf {
            it.insetsController?.isAppearanceLightNavigationBars ?: false
        }?.rootWindowInsetsCompat?.getInsets(WindowInsetsCompat.Type.systemBars())?.bottom ?: 0

        return defaultMargin + insetBottom
    }
}

@VisibleForTesting
internal class ChatActivitySnackBarDelegate(activity: ChatActivity, stringProvider: StringProvider, unifiedTheme: UnifiedTheme?) :
    SnackBarDelegate(activity.findViewById(R.id.chat_view), stringProvider, unifiedTheme?.snackBarTheme) {
    override val anchorViewId: Int = R.id.chat_message_layout
}

@VisibleForTesting
internal class CallActivitySnackBarDelegate(activity: CallActivity, stringProvider: StringProvider, unifiedTheme: UnifiedTheme?) :
    SnackBarDelegate(activity.findViewById(R.id.call_view), stringProvider, unifiedTheme?.callTheme?.snackBar) {
    override val anchorViewId: Int = R.id.buttons_layout_bg
    override val fallbackBackgroundColor: Int = R.color.glia_base_light_color
    override val fallbackTextColor: Int = R.color.glia_base_dark_color
}

internal class SnackBarDelegateFactory(
    private val activity: Activity,
    private val stringProvider: StringProvider,
    private val unifiedTheme: UnifiedTheme?
) {
    fun createDelegate(): SnackBarDelegate = when (activity) {
        is ChatActivity -> ChatActivitySnackBarDelegate(activity, stringProvider, unifiedTheme)
        is CallActivity -> CallActivitySnackBarDelegate(activity, stringProvider, unifiedTheme)
        else -> CommonSnackBarDelegate(activity, stringProvider, unifiedTheme)
    }
}
