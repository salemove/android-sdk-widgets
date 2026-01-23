package com.glia.widgets.base

import android.view.View
import androidx.fragment.app.Fragment
import io.reactivex.rxjava3.disposables.CompositeDisposable

/**
 * Base class for all Glia Fragments.
 *
 * Provides:
 * - RxJava subscription management via [compositeDisposable]
 * - Common lifecycle patterns
 * - Reference to the main Glia view
 *
 * Subclasses should override [gliaView] to return their custom view instance.
 */
internal abstract class GliaFragment : Fragment() {
    /**
     * CompositeDisposable for managing RxJava subscriptions.
     * Automatically cleared in [onDestroyView].
     */
    protected val compositeDisposable = CompositeDisposable()

    /**
     * The main Glia view for this Fragment.
     * Typically a custom view that handles UI logic.
     */
    abstract val gliaView: View

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
    }
}
