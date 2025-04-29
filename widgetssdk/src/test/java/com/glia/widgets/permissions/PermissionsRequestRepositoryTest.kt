package com.glia.widgets.permissions

import android.Manifest
import com.glia.androidsdk.GliaException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

internal class PermissionsRequestRepositoryTest {
    private lateinit var launcher: PermissionsRequestRepository.Launcher
    private lateinit var repository: PermissionsRequestRepository

    @Before
    fun setUp() {
        launcher = mock()
        repository = PermissionsRequestRepository()
        repository.launcher = launcher
    }

    // Tests for .requestPermissions()

    @Test
    fun `requestPermissions adds the request parameters to the requests list`() {
        val permissions = listOf("FirstPermission", "SecondPermission")
        val callback = mock<PermissionsRequestResult>()

        repository.requestPermissions(permissions, callback)

        assertEquals(permissions, repository.requests[0].first)
        assertEquals(callback, repository.requests[0].second)
    }

    @Test
    fun `requestPermissions requests the permissions if the the requests list was empty`() {
        val permissions = listOf("FirstPermission", "SecondPermission")

        repository.requestPermissions(permissions, mock())

        verify(launcher).request(permissions.toTypedArray())
    }

    @Test
    fun `requestPermissions does not request the permissions if the the requests list was not empty`() {
        repository.requests.add(Pair(listOf("FirstPermission", "SecondPermission"), mock()))
        val permissions = listOf("ThirdPermission", "FourthPermission")

        repository.requestPermissions(permissions, mock())

        verify(launcher, never()).request(any())
    }

    // Tests for .onRequestResult()

    @Test
    fun `onRequestResult calls the request callback with given results`() {
        val callback = mock<PermissionsRequestResult>()
        repository.requests.add(Pair(listOf("FirstPermission", "SecondPermission"), callback))
        val results = mapOf(
            Pair("FirstPermission", true),
            Pair("SecondPermission", false)
        )

        repository.onRequestResult(results, null)

        verify(callback).invoke(results, null)
    }

    @Test
    fun `onRequestResult calls the request callback with given exception`() {
        val callback = mock<PermissionsRequestResult>()
        repository.requests.add(Pair(listOf("FirstPermission", "SecondPermission"), callback))
        val exception = GliaException("Text exception", GliaException.Cause.INTERNAL_ERROR)

        repository.onRequestResult(null, exception)

        verify(callback).invoke(null, exception)
    }

    @Test
    fun `onRequestResult removes first request from the requests list`() {
        repository.requests.add(Pair(listOf("FirstPermission", "SecondPermission"), mock()))
        val nextPermissionsRequest = Pair(listOf("ThirdPermission", "FourthPermission"), mock<PermissionsRequestResult>())
        repository.requests.add(nextPermissionsRequest)

        repository.onRequestResult(null, null)

        assertEquals(1, repository.requests.size)
        assertEquals(nextPermissionsRequest, repository.requests.first())
    }

    @Test
    fun `onRequestResult launches next request from the requests list`() {
        repository.requests.add(Pair(listOf("FirstPermission", "SecondPermission"), mock()))
        repository.requests.add(Pair(listOf("ThirdPermission", "FourthPermission"), mock()))

        repository.onRequestResult(null, null)

        verify(launcher).request(arrayOf("ThirdPermission", "FourthPermission"))
    }

    @Test
    fun `onRequestResult does not launch next request if it missed`() {
        repository.requests.add(Pair(listOf("FirstPermission", "SecondPermission"), mock()))

        repository.onRequestResult(null, null)

        verify(launcher, never()).request(any())
    }

    // Tests for .hasPermissionRequest()

    @Test
    fun `hasPermissionRequest returns true if requests is not empty`() {
        repository.requests.add(Pair(listOf("FirstPermission", "SecondPermission"), mock()))

        assertTrue(repository.hasPermissionRequest())
    }

    @Test
    fun `hasPermissionRequest returns false if requests is empty`() {
        assertFalse(repository.hasPermissionRequest())
    }

    @Test
    fun `shouldShowPermissionRationale calls launcher shouldShowPermissionRationale`() {
        val permission = Manifest.permission.POST_NOTIFICATIONS
        repository.shouldShowPermissionRationale(permission)
        verify(launcher).shouldShowPermissionRationale(permission)
    }

}
