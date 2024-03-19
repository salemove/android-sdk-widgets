package com.glia.widgets.filepreview.domain.exception

internal open class FileNotFoundException : RuntimeException {
    internal constructor() : super()
    internal constructor(message: String?) : super(message)
}
