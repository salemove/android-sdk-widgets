package com.glia.widgets.callvisualizer.domain

import android.app.Activity
import com.glia.widgets.base.GliaActivity

internal class IsGliaActivityUseCase {
    operator fun invoke(resumedActivity: Activity?) = resumedActivity is GliaActivity
}
