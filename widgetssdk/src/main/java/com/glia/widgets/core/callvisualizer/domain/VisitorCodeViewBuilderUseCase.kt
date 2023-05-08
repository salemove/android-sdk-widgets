package com.glia.widgets.core.callvisualizer.domain

import android.content.Context
import com.glia.widgets.view.VisitorCodeView

class VisitorCodeViewBuilderUseCase() {
    operator fun invoke(context: Context, closable: Boolean): VisitorCodeView {
        return VisitorCodeView(context = context).apply { setClosable(closable) }
    }
}
