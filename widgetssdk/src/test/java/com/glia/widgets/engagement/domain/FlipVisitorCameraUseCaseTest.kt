package com.glia.widgets.engagement.domain

import com.glia.androidsdk.comms.CameraDevice
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.engagement.VisitorCamera
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class FlipVisitorCameraUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var engagementRepository: EngagementRepository
    private lateinit var useCase: FlipVisitorCameraUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = FlipVisitorCameraUseCaseImpl(engagementRepository)
    }

    @Test
    fun invoke_notSetCamera_ifThereAreNoCameras() {
        every { engagementRepository.cameras } returns emptyList()
        every { engagementRepository.currentVisitorCamera } returns VisitorCamera.NoCamera
        useCase()
        verify(exactly = 0) { engagementRepository.setVisitorCamera(any()) }
    }

    @Test
    fun invoke_setBackCamera_ifCurrentCameraIsFront() {
        val cameras = mockCameras()
        every { engagementRepository.cameras } returns cameras
        every { engagementRepository.currentVisitorCamera } returns VisitorCamera.Camera(mockCamera(facing = CameraDevice.Facing.FRONT))
        useCase()
        verify { engagementRepository.setVisitorCamera(cameras[0]) }
    }

    @Test
    fun invoke_setFrontCamera_ifCurrentCameraIsBack() {
        val cameras = mockCameras()
        every { engagementRepository.cameras } returns cameras
        every { engagementRepository.currentVisitorCamera } returns VisitorCamera.Camera(mockCamera(facing = CameraDevice.Facing.BACK))
        useCase()
        verify { engagementRepository.setVisitorCamera(cameras[1]) }
    }

    @Test
    fun invoke_setFrontCamera_ifThereIsNoCurrentCamera() {
        val cameras = mockCameras()
        every { engagementRepository.cameras } returns cameras
        every { engagementRepository.currentVisitorCamera } returns VisitorCamera.NoCamera
        useCase()
        verify { engagementRepository.setVisitorCamera(cameras[1]) }
    }

    @Test
    fun invoke_setNextCamera_ifThereAreOnlyExternalCameras() {
        val cameras = listOf(
            mockCamera(name = "external0", facing = CameraDevice.Facing.EXTERNAL),
            mockCamera(name = "external1", facing = CameraDevice.Facing.EXTERNAL),
            mockCamera(name = "external2", facing = CameraDevice.Facing.EXTERNAL)
        )
        every { engagementRepository.cameras } returns cameras
        every { engagementRepository.currentVisitorCamera } returns VisitorCamera.Camera(cameras[0])
        useCase()
        verify { engagementRepository.setVisitorCamera(cameras[1]) }
    }

    @Test
    fun invoke_setFirstCamera_ifThereAreOnlyExternalCamerasAndCurrentIsLast() {
        val cameras = listOf(
            mockCamera(name = "external0", facing = CameraDevice.Facing.EXTERNAL),
            mockCamera(name = "external1", facing = CameraDevice.Facing.EXTERNAL),
            mockCamera(name = "external2", facing = CameraDevice.Facing.EXTERNAL)
        )
        every { engagementRepository.cameras } returns cameras
        every { engagementRepository.currentVisitorCamera } returns VisitorCamera.Camera(cameras[2])
        useCase()
        verify { engagementRepository.setVisitorCamera(cameras[0]) }
    }

    private fun mockCameras(): List<CameraDevice> = listOf(
        mockCamera(name = "0", facing = CameraDevice.Facing.BACK),
        mockCamera(name = "1", facing = CameraDevice.Facing.FRONT),
        mockCamera(name = "2", facing = CameraDevice.Facing.BACK),
        mockCamera(name = "3", facing = CameraDevice.Facing.FRONT),
        mockCamera(name = "external", facing = CameraDevice.Facing.EXTERNAL)
    )

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
