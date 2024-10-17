package com.glia.widgets.entrywidget

import io.reactivex.rxjava3.processors.PublishProcessor

internal class EntryWidgetHideController {
    private val _onHide: PublishProcessor<Unit> = PublishProcessor.create<Unit>()

    val onHide = _onHide
    fun hide() {
        onHide.onNext(Unit)
    }
}
