package com.glia.widgets.view.animation

import androidx.transition.Transition

/**
 * Only purpose of this class is to provide option to override only needed
 * methods instead of defining all of them even when they do not need any implementation.
 *
 * For example:
 *
 * val transition = TransitionSet()
 *     .addTransition(Fade().addTarget(binding.root))
 *     .addListener(object : SimpleTransitionListener() {
 *         override fun onTransitionEnd(transition: Transition) {
 *             TODO("Not yet implemented")
 *         }
 *     })
 */
internal open class SimpleTransitionListener : Transition.TransitionListener {
    override fun onTransitionStart(transition: Transition) {
        /* no-op */
    }

    override fun onTransitionEnd(transition: Transition) {
        /* no-op */
    }

    override fun onTransitionCancel(transition: Transition) {
        /* no-op */
    }

    override fun onTransitionPause(transition: Transition) {
        /* no-op */
    }

    override fun onTransitionResume(transition: Transition) {
        /* no-op */
    }
}
