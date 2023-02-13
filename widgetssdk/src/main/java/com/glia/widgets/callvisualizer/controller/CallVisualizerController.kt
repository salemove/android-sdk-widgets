package com.glia.widgets.callvisualizer.controller

import com.glia.widgets.callvisualizer.CallVisualizerRepository
import com.glia.widgets.callvisualizer.domain.IsCallOrChatScreenActiveUseCase
import com.glia.widgets.helper.Logger

class CallVisualizerController(
    private val callVisualizerRepository: CallVisualizerRepository,
    val isCallOrChatScreenActiveUseCase: IsCallOrChatScreenActiveUseCase
) {
    companion object {
        private val TAG = CallVisualizerController::class.java.simpleName
    }

    fun init() {
        Logger.d(TAG, "CallVisualizerController initialized")
        callVisualizerRepository.init()
    }
}
