package com.glia.widgets.base

internal interface BaseView<T : BaseController> {
    fun setController(controller: T)
}
