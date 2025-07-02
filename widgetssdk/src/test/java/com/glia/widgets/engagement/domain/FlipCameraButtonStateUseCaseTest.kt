package com.glia.widgets.engagement.domain

import com.glia.androidsdk.comms.CameraDevice
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.engagement.VisitorCamera
import com.glia.widgets.view.floatingvisitorvideoview.FloatingVisitorVideoContract.FlipButtonState
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.reactivex.rxjava3.processors.BehaviorProcessor
import org.junit.Before
import org.junit.Test

class FlipCameraButtonStateUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var engagementRepository: EngagementRepository

    @MockK(relaxUnitFun = true)
    private lateinit var flipVisitorCameraUseCase: FlipVisitorCameraUseCase
    private lateinit var useCase: FlipCameraButtonStateUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = FlipCameraButtonStateUseCaseImpl(engagementRepository, flipVisitorCameraUseCase)
    }

    @Test
    fun invoke_returnsHide_ifNextDeviceIsNull() {
        val visitorCameraState = BehaviorProcessor.create<VisitorCamera>()
        every { flipVisitorCameraUseCase.nextCamera } returns null
        every { engagementRepository.visitorCameraState } returns visitorCameraState
        val testSubscriber = useCase().test()

        visitorCameraState.onNext(VisitorCamera.Camera(mockk()))

        testSubscriber
            .assertNotComplete()
            .assertValueCount(1)
            .assertValueAt(0, FlipButtonState.HIDE)
    }

    @Test
    fun invoke_returnsSwitchToBack_ifDeviceIsFront() {
        val visitorCameraState = BehaviorProcessor.create<VisitorCamera>()
        every { flipVisitorCameraUseCase.nextCamera } returns mockk()
        every { engagementRepository.visitorCameraState } returns visitorCameraState
        val testSubscriber = useCase().test()

        val cameraDevice = mockCamera(facing = CameraDevice.Facing.FRONT)
        visitorCameraState.onNext(VisitorCamera.Camera(cameraDevice))

        testSubscriber
            .assertNotComplete()
            .assertValueCount(1)
            .assertValueAt(0, FlipButtonState.SWITCH_TO_BACK_CAMERA)
    }

    @Test
    fun invoke_returnsSwitchToFront_ifDeviceIsBack() {
        val visitorCameraState = BehaviorProcessor.create<VisitorCamera>()
        every { flipVisitorCameraUseCase.nextCamera } returns mockk()
        every { engagementRepository.visitorCameraState } returns visitorCameraState
        val testSubscriber = useCase().test()

        val cameraDevice = mockCamera(facing = CameraDevice.Facing.FRONT)
        visitorCameraState.onNext(VisitorCamera.Camera(cameraDevice))

        testSubscriber
            .assertNotComplete()
            .assertValueCount(1)
            .assertValueAt(0, FlipButtonState.SWITCH_TO_BACK_CAMERA)
    }

    private fun mockCamera(
        facing: CameraDevice.Facing = CameraDevice.Facing.FRONT,
        name: String = "camera_$facing"
    ): CameraDevice {
        return mockk<CameraDevice>().also {
            every { it.facing } returns facing
            every { it.name } returns name
        }
    }
}
