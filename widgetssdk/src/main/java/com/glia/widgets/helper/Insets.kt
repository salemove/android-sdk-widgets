package com.glia.widgets.helper

import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.Insets
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins

internal val View.insetsController: WindowInsetsControllerCompat?
    get() = context.asActivity()?.let {
        WindowCompat.getInsetsController(it.window, this)
    }
internal val View.rootWindowInsetsCompat: WindowInsetsCompat?
    get() = ViewCompat.getRootWindowInsets(this)

internal fun WindowInsetsControllerCompat.hideKeyboard() {
    hide(WindowInsetsCompat.Type.ime())
}

internal fun WindowInsetsControllerCompat.showKeyboard() {
    show(WindowInsetsCompat.Type.ime())
}

internal val WindowInsetsCompat.isKeyboardVisible: Boolean
    get() = isVisible(WindowInsetsCompat.Type.ime())

internal val WindowInsetsCompat.keyboardHeight: Int
    get() = getInsets(WindowInsetsCompat.Type.ime()).bottom


internal typealias WindowInsetsAnimationCallback = (insets: Insets) -> Unit

/**
 * This class is designed
 * to handle [Insets] changes for SystemBars: [WindowInsetsCompat.Type.systemBars] and Keyboard:
 * [WindowInsetsCompat.Type.ime],
 * update the [target] View margins according to that Insets size,
 * and listen to Insets changes during Keyboard animation
 *
 * [WindowCompat.setDecorFitsSystemWindows] should be called, to make callbacks work.
 *
 * @see <a href="https://developer.android.com/develop/ui/views/layout/insets">Work with window insets and cutouts</a>
 */
internal class SimpleWindowInsetsAndAnimationHandler(
    private val target: View,
    @DispatchMode mode: Int = DISPATCH_MODE_STOP,
    private val callback: WindowInsetsAnimationCallback
) : WindowInsetsAnimationCompat.Callback(mode),
    OnApplyWindowInsetsListener {
    private var keyboardAnimationInProgress = false

    init {
        ViewCompat.setOnApplyWindowInsetsListener(target, this)
        ViewCompat.setWindowInsetsAnimationCallback(target, this)
    }

    override fun onPrepare(animation: WindowInsetsAnimationCompat) {
        keyboardAnimationInProgress = true
    }

    override fun onEnd(animation: WindowInsetsAnimationCompat) {
        keyboardAnimationInProgress = false
    }

    override fun onProgress(
        insets: WindowInsetsCompat,
        runningAnimations: MutableList<WindowInsetsAnimationCompat>
    ): WindowInsetsCompat {
        val combinedInsets = insets.run {
            Insets.max(
                getInsets(WindowInsetsCompat.Type.ime()),
                getInsets(WindowInsetsCompat.Type.systemBars())
            )
        }

        target.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            updateMargins(bottom = combinedInsets.bottom)
        }
        callback(combinedInsets)

        return WindowInsetsCompat.CONSUMED
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        if (!keyboardAnimationInProgress) {
            insets.getInsets(WindowInsetsCompat.Type.systemBars()).apply {
                v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    updateMargins(top = top, bottom = bottom)
                }
            }
        }

        return WindowInsetsCompat.CONSUMED
    }

}
