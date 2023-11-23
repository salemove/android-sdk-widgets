package com.glia.widgets.engagement.end.domain

import com.glia.widgets.di.Dependencies

internal class DestroyControllersUseCase {
    operator fun invoke() {
        Dependencies.getControllerFactory().destroyControllers()
    }
}
