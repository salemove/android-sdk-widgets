package com.glia.widgets.permissions.controller

import com.glia.widgets.permissions.PermissionsRequestContract
import com.glia.widgets.permissions.PermissionsRequestRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class PermissionsRequestControllerTest {
    private lateinit var permissionsRequestRepository: PermissionsRequestRepository
    private lateinit var launcher: PermissionsRequestRepository.Launcher
    private lateinit var watcher: PermissionsRequestContract.Watcher

    private lateinit var controller: PermissionsRequestController

    @Before
    fun setUp() {
        permissionsRequestRepository = mock()
        launcher = mock()
        whenever(permissionsRequestRepository.launcher).thenReturn(launcher)
        watcher = mock()

        controller = PermissionsRequestController(permissionsRequestRepository)
        controller.setWatcher(watcher)
    }

    // Tests for .request()

    @Test
    fun `request calls requestPermissions when current activity is ComponentActivity`() {
        val permissions = arrayOf("FirstPermission", "SecondPermission")
        whenever(watcher.hasValidActivity()).thenReturn(true)
        whenever(watcher.currentActivityIsComponentActivity()).thenReturn(true)
        whenever(watcher.requestPermissions(permissions)).thenReturn(123)

        controller.request(permissions)

        verify(watcher).requestPermissions(permissions)
        verify(watcher, never()).openSupportActivity()
        assertEquals(123, controller.requestHashCode)
        assertNull(controller.permissionsRequest)
    }

    @Test
    fun `request calls requestPermissions when current activity is ComponentActivity, request hashCode is null`() {
        val permissions = arrayOf("FirstPermission", "SecondPermission")
        whenever(watcher.hasValidActivity()).thenReturn(true)
        whenever(watcher.currentActivityIsComponentActivity()).thenReturn(true)
        whenever(watcher.requestPermissions(permissions)).thenReturn(null)

        controller.request(permissions)

        verify(watcher).requestPermissions(permissions)
        verify(watcher, never()).openSupportActivity()
        assertNull(controller.requestHashCode)
        assertEquals(permissions.toList(), controller.permissionsRequest!!.toList())
    }

    @Test
    fun `request calls openSupportActivity when current activity is not ComponentActivity`() {
        whenever(watcher.hasValidActivity()).thenReturn(true)
        whenever(watcher.currentActivityIsComponentActivity()).thenReturn(false)
        val permissions = arrayOf("FirstPermission", "SecondPermission")

        controller.request(permissions)

        verify(watcher, never()).requestPermissions(any())
        verify(watcher).openSupportActivity()
        assertNull(controller.requestHashCode)
        assertEquals(permissions.toList(), controller.permissionsRequest!!.toList())
    }

    @Test
    fun `request caches permissionsRequest when there is no activity`() {
        whenever(watcher.hasValidActivity()).thenReturn(false)
        val permissions = arrayOf("FirstPermission", "SecondPermission")

        controller.request(permissions)

        verify(watcher, never()).requestPermissions(any())
        verify(watcher, never()).openSupportActivity()
        assertNull(controller.requestHashCode)
        assertEquals(permissions.toList(), controller.permissionsRequest!!.toList())
    }

    // Tests for .onActivityResumed()

    @Test
    fun `onActivityResumed requests permissions if controller has cached request`() {
        whenever(watcher.currentActivityIsComponentActivity()).thenReturn(true)
        val permissions = arrayOf("FirstPermission", "SecondPermission")
        controller.permissionsRequest = permissions

        controller.onActivityResumed()

        verify(watcher).requestPermissions(permissions)
        assertNull(controller.permissionsRequest)
    }

    @Test
    fun `onActivityResumed doesn't requests permissions if controller has not cached request`() {
        whenever(watcher.currentActivityIsComponentActivity()).thenReturn(true)

        controller.onActivityResumed()

        verify(watcher, never()).requestPermissions(any())
    }

    // Tests for .onActivityDestroyed()

    @Test
    fun `onActivityDestroyed calls onRequestResult if request hashCode is equals to activity hashCode`() {
        val hashCode = 123
        controller.requestHashCode = hashCode

        controller.onActivityDestroyed(hashCode)

        verify(permissionsRequestRepository).onRequestResult(eq(null), any())
    }

    @Test
    fun `onActivityDestroyed doesn't call onRequestResult if request hashCode is not equals to activity hashCode`() {
        controller.requestHashCode = 123

        controller.onActivityDestroyed(321)

        verify(permissionsRequestRepository, never()).onRequestResult(eq(null), any())
    }

    // Tests for .onRequestedPermissionsResult()

    @Test
    fun `onRequestedPermissionsResult calls permissionsRequestRepository with results`() {
        whenever(permissionsRequestRepository.hasPermissionRequest()).thenReturn(false)
        val results = mapOf(
            Pair("FirstPermission", true),
            Pair("SecondPermission", false)
        )

        controller.onRequestedPermissionsResult(results)

        verify(permissionsRequestRepository).onRequestResult(results, null)
    }

    @Test
    fun `onRequestedPermissionsResult clears the request hashCode on execute`() {
        whenever(permissionsRequestRepository.hasPermissionRequest()).thenReturn(false)
        controller.requestHashCode = 123

        controller.onRequestedPermissionsResult(mapOf())

        assertNull(controller.requestHashCode)
    }

    @Test
    fun `onRequestedPermissionsResult destroys support activity if repository has no request permissions`() {
        whenever(permissionsRequestRepository.hasPermissionRequest()).thenReturn(false)

        controller.onRequestedPermissionsResult(mapOf())

        verify(watcher).destroySupportActivityIfExists()
    }

    @Test
    fun `onRequestedPermissionsResult doesn't destroy support activity if repository has request permissions`() {
        whenever(permissionsRequestRepository.hasPermissionRequest()).thenReturn(true)

        controller.onRequestedPermissionsResult(mapOf())

        verify(watcher, never()).destroySupportActivityIfExists()
    }
}
