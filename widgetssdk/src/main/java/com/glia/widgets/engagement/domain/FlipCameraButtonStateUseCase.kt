package com.glia.widgets.engagement.domain

import com.glia.androidsdk.comms.CameraDevice
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.engagement.VisitorCamera
import com.glia.widgets.view.floatingvisitorvideoview.FloatingVisitorVideoContract.FlipButtonState
import io.reactivex.rxjava3.core.Flowable

internal interface FlipCameraButtonStateUseCase {
    operator fun invoke(): Flowable<FlipButtonState>
}

internal class FlipCameraButtonStateUseCaseImpl(
    private val engagementRepository: EngagementRepository,
    private val flipVisitorCameraUseCase: FlipVisitorCameraUseCase
) : FlipCameraButtonStateUseCase {
    override fun invoke(): Flowable<FlipButtonState> = engagementRepository.visitorCameraState
        .filter { it !is VisitorCamera.Switching }
        .map {
            if (flipVisitorCameraUseCase.nextCamera == null) {
                return@map FlipButtonState.HIDE
            }
            when (it) {
                is VisitorCamera.Camera -> {
                    if (it.cameraDevice.facing == CameraDevice.Facing.FRONT) {
                        FlipButtonState.SWITCH_TO_BACK_CAMERA
                    } else {
                        FlipButtonState.SWITCH_TO_FACE_CAMERA
                    }
                }
                else -> FlipButtonState.HIDE
            }
        }
        .distinctUntilChanged()
}
