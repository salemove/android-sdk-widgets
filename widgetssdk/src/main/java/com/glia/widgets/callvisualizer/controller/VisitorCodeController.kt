package com.glia.widgets.callvisualizer.controller

import com.glia.widgets.callvisualizer.VisitorCodeContract
import com.glia.widgets.core.dialog.DialogController

class VisitorCodeController(private val dialogController: DialogController) : VisitorCodeContract.Controller {

    private var view: VisitorCodeContract.View? = null

    override fun setView(view: VisitorCodeContract.View) {
        this.view = view
    }

    override fun onCloseButtonClicked() {
        dialogController.dismissCurrentDialog()
    }

    override fun onDestroy() {}
}