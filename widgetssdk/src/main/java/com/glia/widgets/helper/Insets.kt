@file:JvmName("Insets")

package com.glia.widgets.helper

import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.graphics.Insets
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding

@get:JvmName("insetsControllerCompat")
internal val Window.insetsControllerCompat: WindowInsetsControllerCompat
    get() = WindowCompat.getInsetsController(this, decorView)

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
 * See [Work with window insets and cutouts](https://developer.android.com/develop/ui/views/layout/insets")
 */
internal class SimpleWindowInsetsAndAnimationHandler(
    private val target: View,
    private val appBarOrToolBar: View? = null,
    @DispatchMode mode: Int = DISPATCH_MODE_STOP,
    private val callback: WindowInsetsAnimationCallback? = null
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

    override fun onProgress(insets: WindowInsetsCompat, runningAnimations: MutableList<WindowInsetsAnimationCompat>): WindowInsetsCompat {
        val combinedInsets = getCombinedInsets(insets)

        target.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            updateMargins(bottom = combinedInsets.bottom)
        }
        callback?.invoke(combinedInsets)

        return WindowInsetsCompat.CONSUMED
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        if (!keyboardAnimationInProgress) {
            getCombinedInsets(insets).apply {
                v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    updateMargins(left, appBarOrToolBar?.let { 0 } ?: top, right, bottom)
                }
                appBarOrToolBar?.updatePadding(top = top)
            }
        }

        return insets
    }

    private fun getCombinedInsets(insets: WindowInsetsCompat): Insets = insets.run {
        Insets.max(
            getInsets(WindowInsetsCompat.Type.ime()),
            getInsets(WindowInsetsCompat.Type.systemBars())
        )
    }
}
