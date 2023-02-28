package com.glia.widgets.core.callvisualizer.domain

import android.content.Context
import com.glia.widgets.view.VisitorCodeView

class VisitorCodeViewBuilderUseCase(
    private val visitorCodeRepository: VisitorCodeRepository
) {
    operator fun invoke(context: Context, closable: Boolean): VisitorCodeView {
        val visitorCodeView = VisitorCodeView(context = context).apply { setClosable(closable) }
        visitorCodeView.setVisitorCodeRepository(visitorCodeRepository)
        return visitorCodeView
    }
}