package com.glia.widgets.internal.callvisualizer.domain

import android.content.Context
import com.glia.widgets.view.VisitorCodeView

internal class VisitorCodeViewBuilderUseCase {
    // VisitorCodeView wraps the context with the Glia theme itself
    operator fun invoke(context: Context, closable: Boolean): VisitorCodeView =
        VisitorCodeView(context = context).apply { setClosable(closable) }
}
