package com.glia.widgets.entrywidget

import io.reactivex.rxjava3.processors.PublishProcessor

internal class EntryWidgetHideController {
    val onHide: PublishProcessor<Unit> = PublishProcessor.create<Unit>()
}
