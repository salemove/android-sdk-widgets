package com.glia.widgets.engagement

import com.glia.androidsdk.comms.CameraDevice

internal sealed class VisitorCamera {
    internal object NoCamera : VisitorCamera()
    internal object Switching : VisitorCamera()
    internal data class Camera(val cameraDevice: CameraDevice) : VisitorCamera()

    val value: CameraDevice? get() = (this as? Camera)?.cameraDevice
}
