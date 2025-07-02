package com.glia.widgets.engagement.domain

import com.glia.androidsdk.comms.CameraDevice
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.engagement.VisitorCamera
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

internal interface FlipVisitorCameraUseCase {
    val nextCamera: CameraDevice?
    operator fun invoke()
}

internal class FlipVisitorCameraUseCaseImpl(private val repository: EngagementRepository) : FlipVisitorCameraUseCase {
    override val nextCamera: CameraDevice?
        get() {
            val cameras = repository.cameras ?: return null
            val currentVisitorCamera = repository.currentVisitorCamera.value
            return getNextCameraByFacing(
                cameras,
                currentVisitorCamera
            )  // If it is not possible to get the next camera according to the facing of the cameras,
                ?: getNextAvailableCamera(cameras, currentVisitorCamera) // will try to get the next camera from the list.
        }

    override fun invoke() {
        if (repository.currentVisitorCamera is VisitorCamera.Switching) return
        nextCamera?.also { repository.setVisitorCamera(it) }
    }

    private fun getCamera(cameras: List<CameraDevice>, facing: CameraDevice.Facing) =
        cameras.firstOrNull { it.facing == facing }

    /**
     * @return the next camera according to the facing.
     *         If the current camera is in front, it will return the back camera.
     *         If the current camera is in back, it will return the front camera.
     *         It can return null if the system doesn't have a camera available or if something is wrong with its facing attributes.
     */
    private fun getNextCameraByFacing(cameras: List<CameraDevice>, currentVisitorCamera: CameraDevice?): CameraDevice? =
        if (currentVisitorCamera?.facing == CameraDevice.Facing.FRONT) {
            getCamera(cameras, CameraDevice.Facing.BACK)
        } else {
            getCamera(cameras, CameraDevice.Facing.FRONT)
        }

    /**
     * @return the next camera in the list.
     *         If the current camera is the last in the list, it will return the first camera device.
     */
    private fun getNextAvailableCamera(cameras: List<CameraDevice>, currentVisitorCamera: CameraDevice?): CameraDevice? {
        Logger.w(TAG, "Getting the next available camera instead of the following camera according to the facing")

        if (cameras.isEmpty()) return null
        if (currentVisitorCamera == null) return cameras.firstOrNull()

        val currentCameraIndex = cameras.indexOf(currentVisitorCamera)
        return cameras.getOrNull((currentCameraIndex + 1) % cameras.size)
    }
}
