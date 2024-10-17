package com.glia.widgets.entrywidget

import io.reactivex.rxjava3.processors.BehaviorProcessor

internal class EntryWidgetHideController {
    val onHide: BehaviorProcessor<Any> = BehaviorProcessor.create()
}
