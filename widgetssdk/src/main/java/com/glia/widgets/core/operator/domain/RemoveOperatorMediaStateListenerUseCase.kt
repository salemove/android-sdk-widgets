package com.glia.widgets.core.operator.domain

import com.glia.widgets.core.operator.GliaOperatorMediaRepository

class RemoveOperatorMediaStateListenerUseCase(
    private val gliaOperatorMediaRepository: GliaOperatorMediaRepository
) {
    operator fun invoke(listener: GliaOperatorMediaRepository.OperatorMediaStateListener) {
        gliaOperatorMediaRepository.removeMediaStateListener(listener)
    }
}