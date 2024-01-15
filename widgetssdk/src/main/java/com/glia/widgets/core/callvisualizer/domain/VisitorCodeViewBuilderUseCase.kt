package com.glia.widgets.core.callvisualizer.domain

import android.content.Context
import com.glia.widgets.view.VisitorCodeView

internal class VisitorCodeViewBuilderUseCase {
    operator fun invoke(context: Context, closable: Boolean): VisitorCodeView = VisitorCodeView(context = context).apply { setClosable(closable) }
}
