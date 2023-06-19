package com.glia.widgets.call.domain

import com.glia.androidsdk.Engagement
import com.glia.widgets.core.callvisualizer.domain.IsCallVisualizerUseCase
import com.glia.widgets.core.permissions.PermissionManager
import com.glia.widgets.permissions.Permissions
import com.glia.widgets.permissions.PermissionsGrantedCallback

import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class HandleCallPermissionsUseCaseTest {
    private lateinit var isCallVisualizerUseCase: IsCallVisualizerUseCase
    private lateinit var permissionManager: PermissionManager

    private lateinit var useCase: HandleCallPermissionsUseCase

    @Before
    fun setUp() {
        isCallVisualizerUseCase = mock()
        permissionManager = mock()

        useCase = HandleCallPermissionsUseCase(isCallVisualizerUseCase, permissionManager)
    }

    @Test
    fun `invoke gets permissions from permission manager with MediaType VIDEO, and is call visualizer`() {
        val mediaType = Engagement.MediaType.VIDEO
        val isCallVisualizer = true
        whenever(isCallVisualizerUseCase.invoke()).thenReturn(isCallVisualizer)
        whenever(permissionManager.getPermissionsForEngagementMediaType(any(), any())).thenReturn(mock())

        useCase.invoke(mediaType, mock())

        verify(permissionManager).getPermissionsForEngagementMediaType(mediaType, isCallVisualizer)
    }

    @Test
    fun `invoke gets permissions from permission manager with MediaType AUDIO, and is not call visualizer`() {
        val mediaType = Engagement.MediaType.AUDIO
        val isCallVisualizer = false
        whenever(isCallVisualizerUseCase.invoke()).thenReturn(isCallVisualizer)
        whenever(permissionManager.getPermissionsForEngagementMediaType(any(), any())).thenReturn(mock())

        useCase.invoke(mediaType, mock())

        verify(permissionManager).getPermissionsForEngagementMediaType(mediaType, isCallVisualizer)
    }

    @Test
    fun `invoke calls permission manager to request given permissions`() {
        val necessaryPermissions = listOf("NecessaryPermission")
        val additionalPermissions = listOf("AdditionalPermission")
        val permissions = Permissions(necessaryPermissions, additionalPermissions)
        whenever(isCallVisualizerUseCase.invoke()).thenReturn(true)
        whenever(permissionManager.getPermissionsForEngagementMediaType(any(), any())).thenReturn(permissions)
        val callback = mock<PermissionsGrantedCallback>()

        useCase.invoke(Engagement.MediaType.TEXT, callback)

        verify(permissionManager).handlePermissions(
            necessaryPermissions,
            additionalPermissions,
            callback
        )
    }
}
