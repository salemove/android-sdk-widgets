package com.glia.widgets.internal.callvisualizer.domain

import android.content.Context
import com.glia.widgets.helper.wrapWithTheme
import com.glia.widgets.view.VisitorCodeView

internal class VisitorCodeViewBuilderUseCase {
    //Wrapping with Glia theme to make the Theme available for embedded view
    operator fun invoke(context: Context, closable: Boolean): VisitorCodeView =
        VisitorCodeView(context = context.wrapWithTheme()).apply { setClosable(closable) }
}
