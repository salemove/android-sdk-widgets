package com.glia.widgets.view.snackbar

import android.app.Activity
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import com.glia.telemetry_lib.EventAttribute
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.telemetry_lib.SnackBarTypes
import com.glia.widgets.R
import com.glia.widgets.call.CallActivity
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.insetsController
import com.glia.widgets.helper.rootView
import com.glia.widgets.helper.rootWindowInsetsCompat
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.view.unifiedui.theme.SnackBarTheme
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

/**
 * Abstract class representing a delegate for managing and displaying a Snackbar.
 *
 * @property view The view to attach the Snackbar to.
 * @property titleStringKey The string resource ID for the Snackbar message.
 * @property localeProvider Provides localized strings.
 * @property snackBarTheme The unified theme to apply to the Snackbar.
 * @property duration The duration for which the Snackbar is displayed.
 */
internal abstract class SnackBarDelegate(
    private val view: View,
    @param:StringRes
    private val titleStringKey: Int,
    private val localeProvider: LocaleProvider,
    private val snackBarTheme: SnackBarTheme?,
    @param:BaseTransientBottomBar.Duration
    private val duration: Int
) {

    /**
     * ID of the anchor view for the Snackbar. Can be overridden by subclasses.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    @get:IdRes
    open val anchorViewId: Int? = null

    /**
     * Bottom margin for the Snackbar. Can be overridden by subclasses.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    open val marginBottom: Int? = null

    /**
     * Fallback background color for the Snackbar.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    @get:ColorRes
    open val fallbackBackgroundColor: Int = R.color.glia_dark_color

    /**
     * Fallback text color for the Snackbar.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    @get:ColorRes
    open val fallbackTextColor: Int = R.color.glia_light_color

    /**
     * Lazy initialization of the Snackbar instance.
     */
    val snackBar: Snackbar by lazy { makeSnackBar() }

    /**
     * Displays the Snackbar.
     */
    fun show() = snackBar.show()

    /**
     * Dismisses the Snackbar.
     */
    fun dismiss() = snackBar.dismiss()

    /**
     * Creates and configures the Snackbar instance.
     */
    private fun makeSnackBar(): Snackbar {
        val bgColor = snackBarTheme?.backgroundColorTheme?.primaryColor ?: view.getColorCompat(fallbackBackgroundColor)
        val textColor = snackBarTheme?.textColorTheme?.primaryColor ?: view.getColorCompat(fallbackTextColor)
        val message = localeProvider.getString(titleStringKey)

        return Snackbar.make(view, message, duration)
            .setBackgroundTint(bgColor)
            .setTextColor(textColor)
            .apply { anchorViewId?.also { setAnchorView(it) } }
            .apply { marginBottom?.also { updateBottomMargin(this, it) } }
    }

    /**
     * Updates the bottom margin of the Snackbar.
     *
     * @param snackBar The Snackbar instance.
     * @param margin The bottom margin to apply.
     */
    private fun updateBottomMargin(snackBar: Snackbar, margin: Int) {
        snackBar.view.updateLayoutParams<MarginLayoutParams> {
            updateMargins(bottom = margin)
        }
    }
}

/**
 * A Snackbar delegate for common activities.
 *
 * @param activity The activity to attach the Snackbar to.
 * @param titleStringKey The string resource ID for the Snackbar message.
 * @param localeProvider Provides localized strings.
 * @param unifiedTheme The unified theme to apply.
 * @param duration The duration for which the Snackbar is displayed.
 */
@VisibleForTesting
internal class CommonSnackBarDelegate(
    activity: Activity,
    @StringRes titleStringKey: Int,
    localeProvider: LocaleProvider,
    unifiedTheme: UnifiedTheme?,
    @param:BaseTransientBottomBar.Duration
    private val duration: Int
) : SnackBarDelegate(activity.rootView, titleStringKey, localeProvider, unifiedTheme?.snackBarTheme, duration) {
    override val marginBottom: Int = calculateBottomMargin(activity.rootView)

    /**
     * Calculates the bottom margin for the Snackbar.
     *
     * @param view The root view of the activity.
     * @return The calculated bottom margin.
     */
    private fun calculateBottomMargin(view: View): Int {
        val defaultMargin = view.resources.getDimensionPixelSize(R.dimen.glia_snack_bar_bottom_margin)
        val insetBottom = view.takeIf {
            it.insetsController?.isAppearanceLightNavigationBars ?: false
        }?.rootWindowInsetsCompat?.getInsets(WindowInsetsCompat.Type.systemBars())?.bottom ?: 0

        return defaultMargin + insetBottom
    }
}

/**
 * A Snackbar delegate for ChatActivity.
 *
 * @param activity The ChatActivity to attach the Snackbar to.
 * @param titleStringKey The string resource ID for the Snackbar message.
 * @param localeProvider Provides localized strings.
 * @param unifiedTheme The unified theme to apply.
 * @param duration The duration for which the Snackbar is displayed.
 */
@VisibleForTesting
internal class ChatActivitySnackBarDelegate(
    activity: ChatActivity,
    @StringRes titleStringKey: Int,
    localeProvider: LocaleProvider,
    unifiedTheme: UnifiedTheme?,
    @param:BaseTransientBottomBar.Duration
    private val duration: Int
) : SnackBarDelegate(activity.findViewById(R.id.chat_view), titleStringKey, localeProvider, unifiedTheme?.snackBarTheme, duration) {
    override val marginBottom: Int = activity.resources.getDimensionPixelSize(R.dimen.glia_snack_bar_bottom_margin)
}

/**
 * A Snackbar delegate for CallActivity.
 *
 * @param activity The CallActivity to attach the Snackbar to.
 * @param titleStringKey The string resource ID for the Snackbar message.
 * @param localeProvider Provides localized strings.
 * @param unifiedTheme The unified theme to apply.
 * @param duration The duration for which the Snackbar is displayed.
 */
@VisibleForTesting
internal class CallActivitySnackBarDelegate(
    activity: CallActivity,
    @StringRes titleStringKey: Int,
    localeProvider: LocaleProvider,
    unifiedTheme: UnifiedTheme?,
    @param:BaseTransientBottomBar.Duration
    private val duration: Int
) : SnackBarDelegate(activity.findViewById(R.id.call_view), titleStringKey, localeProvider, unifiedTheme?.callTheme?.snackBar, duration) {
    override val anchorViewId: Int = R.id.buttons_layout_bg
    override val fallbackBackgroundColor: Int = R.color.glia_light_color
    override val fallbackTextColor: Int = R.color.glia_dark_color
}

/**
 * Factory class for creating instances of SnackBarDelegate.
 *
 * @param activity The activity to attach the Snackbar to.
 * @param titleStringKey The string resource ID for the Snackbar message.
 * @param localeProvider Provides localized strings.
 * @param unifiedTheme The unified theme to apply.
 * @param duration The duration for which the Snackbar is displayed.
 */
internal class SnackBarDelegateFactory(
    private val activity: Activity,
    private val titleStringKey: Int,
    private val localeProvider: LocaleProvider,
    private val unifiedTheme: UnifiedTheme?,
    @property:BaseTransientBottomBar.Duration
    private val duration: Int = Snackbar.LENGTH_SHORT
) {
    /**
     * Creates a SnackBarDelegate instance based on the activity type.
     *
     * @return The appropriate SnackBarDelegate instance.
     */
    fun createDelegate(): SnackBarDelegate = when (activity) {
        is ChatActivity -> ChatActivitySnackBarDelegate(activity, titleStringKey, localeProvider, unifiedTheme, duration)
        is CallActivity -> CallActivitySnackBarDelegate(activity, titleStringKey, localeProvider, unifiedTheme, duration)
        else -> CommonSnackBarDelegate(activity, titleStringKey, localeProvider, unifiedTheme, duration)
    }
}

/**
 * Creates a SnackBarDelegate to show a "No Connection" status.
 *
 * @param activity The activity to attach the Snackbar to.
 * @return The created SnackBarDelegate instance.
 */
internal fun makeNoConnectionSnackBar(activity: Activity): SnackBarDelegate = SnackBarDelegateFactory(
    activity = activity,
    titleStringKey = R.string.snackbar_no_connection_message,
    localeProvider = Dependencies.localeProvider,
    unifiedTheme = Dependencies.gliaThemeManager.theme,
    duration = Snackbar.LENGTH_INDEFINITE
).createDelegate()

/**
 * Logs the event when the "No Connection" Snackbar is shown.
 */
internal fun logNoConnectionSnackBarShown() {
    GliaLogger.i(LogEvents.SNACKBAR_SHOWN) {
        put(EventAttribute.IsAutoClosable, false.toString())
        put(EventAttribute.Type, SnackBarTypes.CONNECTION)
    }
}

/**
 * Logs the event when the "No Connection" Snackbar is dismissed.
 */
internal fun logNoConnectionSnackBarDismissed() {
    GliaLogger.i(LogEvents.SNACKBAR_HIDDEN) {
        put(EventAttribute.Type, SnackBarTypes.CONNECTION)
    }
}
