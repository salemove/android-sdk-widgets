package com.glia.widgets.core.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.comms.MediaDirection
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.engagement.MediaType
import com.glia.widgets.helper.Logger
import com.glia.widgets.permissions.Permissions
import com.glia.widgets.permissions.PermissionsGrantedCallback
import com.glia.widgets.permissions.PermissionsRequestRepository
import com.glia.widgets.permissions.PermissionsRequestResult
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.function.Consumer

class PermissionManagerTest {
    private lateinit var context: Context
    private lateinit var checkSelfPermission: CheckSelfPermission
    private lateinit var permissionsRequestRepository: PermissionsRequestRepository

    private lateinit var permissionManager: PermissionManager
    private var logger: MockedStatic<Logger>? = null

    @Before
    fun setUp() {
        context = mock()
        checkSelfPermission = mock()
        permissionsRequestRepository = mock()

        permissionManager = PermissionManager(
            context,
            checkSelfPermission,
            permissionsRequestRepository,
            Build.VERSION_CODES.R
        )
        logger = Mockito.mockStatic(Logger::class.java)
    }

    @After
    fun tearDown() {
        logger?.close()
    }

    // Tests for .getPermissionsForEngagementMediaType()

    @Test
    fun `getPermissionsForEngagementMediaType returns empty lists of permissions when mediaType is TEXT`() {
        val result = permissionManager.getPermissionsForEngagementMediaType(MediaType.TEXT, false)

        assertEquals(
            Permissions(
                emptyList(),
                emptyList()
            ),
            result
        )
    }

    @Test
    fun `getPermissionsForEngagementMediaType returns empty lists of permissions when mediaType is AUDIO`() {
        val result = permissionManager.getPermissionsForEngagementMediaType(MediaType.AUDIO, false)

        assertEquals(
            Permissions(
                listOf(Manifest.permission.RECORD_AUDIO),
                emptyList()
            ),
            result
        )
    }

    @Test
    fun `getPermissionsForEngagementMediaType returns empty lists of permissions when mediaType is VIDEO`() {
        val result = permissionManager.getPermissionsForEngagementMediaType(MediaType.VIDEO, false)

        assertEquals(
            Permissions(
                listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                emptyList()
            ),
            result
        )
    }

    @Test
    fun `getPermissionsForEngagementMediaType returns empty lists of permissions when mediaType is AUDIO, isCallVisualizer`() {
        val result = permissionManager.getPermissionsForEngagementMediaType(MediaType.AUDIO, true)

        assertEquals(
            Permissions(
                emptyList(),
                emptyList()
            ),
            result
        )
    }

    @Test
    fun `getPermissionsForEngagementMediaType returns empty lists of permissions when mediaType is VIDEO, isCallVisualizer`() {
        val result = permissionManager.getPermissionsForEngagementMediaType(MediaType.VIDEO, true)

        assertEquals(
            Permissions(
                listOf(Manifest.permission.CAMERA),
                emptyList()
            ),
            result
        )
    }

    @Test
    fun `getPermissionsForEngagementMediaType returns empty lists of permissions when mediaType is TEXT, SDK is S`() {
        val permissionManager = PermissionManager(
            context,
            checkSelfPermission,
            permissionsRequestRepository,
            Build.VERSION_CODES.S
        )
        val result = permissionManager.getPermissionsForEngagementMediaType(MediaType.TEXT, false)
        assertEquals(
            Permissions(
                emptyList(),
                listOf(Manifest.permission.BLUETOOTH_CONNECT)
            ),
            result
        )
    }

    @Test
    fun `getPermissionsForEngagementMediaType returns empty lists of permissions when mediaType is AUDIO, SDK is S`() {
        val permissionManager = PermissionManager(
            context,
            checkSelfPermission,
            permissionsRequestRepository,
            Build.VERSION_CODES.S
        )

        val result = permissionManager.getPermissionsForEngagementMediaType(MediaType.AUDIO, false)

        assertEquals(
            Permissions(
                listOf(Manifest.permission.RECORD_AUDIO),
                listOf(Manifest.permission.BLUETOOTH_CONNECT)
            ),
            result
        )
    }

    @Test
    fun `getPermissionsForEngagementMediaType returns empty lists of permissions when mediaType is VIDEO, SDK is S`() {
        val permissionManager = PermissionManager(
            context,
            checkSelfPermission,
            permissionsRequestRepository,
            Build.VERSION_CODES.S
        )

        val result = permissionManager.getPermissionsForEngagementMediaType(MediaType.VIDEO, false)

        assertEquals(
            Permissions(
                listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                listOf(Manifest.permission.BLUETOOTH_CONNECT)
            ),
            result
        )
    }

    @Test
    fun `getPermissionsForEngagementMediaType returns empty lists of permissions when mediaType is AUDIO, isCallVisualizer, SDK is S`() {
        val permissionManager = PermissionManager(
            context,
            checkSelfPermission,
            permissionsRequestRepository,
            Build.VERSION_CODES.S
        )

        val result = permissionManager.getPermissionsForEngagementMediaType(MediaType.AUDIO, true)

        assertEquals(
            Permissions(
                emptyList(),
                emptyList()
            ),
            result
        )
    }

    @Test
    fun `getPermissionsForEngagementMediaType returns empty lists of permissions when mediaType is VIDEO, isCallVisualizer, SDK is S`() {
        val permissionManager = PermissionManager(
            context,
            checkSelfPermission,
            permissionsRequestRepository,
            Build.VERSION_CODES.S
        )

        val result = permissionManager.getPermissionsForEngagementMediaType(MediaType.VIDEO, true)

        assertEquals(
            Permissions(
                listOf(Manifest.permission.CAMERA),
                emptyList()
            ),
            result
        )
    }

    // Tests for .handlePermissions()

    @Test
    fun `handlePermissions requests only missing necessaryPermissions`() {
        whenever(checkSelfPermission.invoke(any(), eq("FirstPermission"))).thenReturn(PERMISSION_DENIED)
        whenever(checkSelfPermission.invoke(any(), eq("SecondPermission"))).thenReturn(PERMISSION_GRANTED)
        whenever(checkSelfPermission.invoke(any(), eq("ThirdPermission"))).thenReturn(PERMISSION_DENIED)
        val permissions = listOf("FirstPermission", "SecondPermission", "ThirdPermission")

        permissionManager.handlePermissions(necessaryPermissions = permissions)

        verify(permissionsRequestRepository).requestPermissions(
            eq(listOf("FirstPermission", "ThirdPermission")),
            any()
        )
    }

    @Test
    fun `handlePermissions requests only missing additionalPermissions`() {
        whenever(checkSelfPermission.invoke(any(), eq("FirstPermission"))).thenReturn(PERMISSION_DENIED)
        whenever(checkSelfPermission.invoke(any(), eq("SecondPermission"))).thenReturn(PERMISSION_GRANTED)
        whenever(checkSelfPermission.invoke(any(), eq("ThirdPermission"))).thenReturn(PERMISSION_DENIED)
        val permissions = listOf("FirstPermission", "SecondPermission", "ThirdPermission")

        permissionManager.handlePermissions(additionalPermissions = permissions)

        verify(permissionsRequestRepository).requestPermissions(
            eq(listOf("FirstPermission", "ThirdPermission")),
            any()
        )
    }

    @Test
    fun `handlePermissions doesn't call the permissionsRequestRepository if missing permissions are empty`() {
        whenever(checkSelfPermission.invoke(any(), any())).thenReturn(PERMISSION_GRANTED)
        val necessaryPermissions = listOf("FirstPermission", "SecondPermission")
        val additionalPermissions = listOf("ThirdPermission", "FourthPermission")

        permissionManager.handlePermissions(necessaryPermissions, additionalPermissions)

        verify(permissionsRequestRepository, never()).requestPermissions(any(), any())
    }

    // Tests for .requestGroupedPermissions()

    @Test
    fun `requestGroupedPermissions combines lists of permissions`() {
        val necessaryPermissions = listOf("FirstPermission", "SecondPermission")
        val additionalPermissions = listOf("ThirdPermission", "FourthPermission")

        permissionManager.requestGroupedPermissions(necessaryPermissions, additionalPermissions)

        verify(permissionsRequestRepository).requestPermissions(
            eq(listOf("FirstPermission", "SecondPermission", "ThirdPermission", "FourthPermission")),
            any()
        )
    }

    @Test
    fun `requestGroupedPermissions requests necessaryPermissions if additionalPermissions is null`() {
        val necessaryPermissions = listOf("FirstPermission", "SecondPermission")

        permissionManager.requestGroupedPermissions(necessaryPermissions, null)

        verify(permissionsRequestRepository).requestPermissions(
            eq(necessaryPermissions),
            any()
        )
    }

    @Test
    fun `requestGroupedPermissions requests additionalPermissions if necessaryPermissions is null`() {
        val additionalPermissions = listOf("ThirdPermission", "FourthPermission")

        permissionManager.requestGroupedPermissions(null, additionalPermissions)

        verify(permissionsRequestRepository).requestPermissions(
            eq(additionalPermissions),
            any()
        )
    }

    @Test
    fun `requestGroupedPermissions doesn't call the permissionsRequestRepository if permissions are null`() {
        permissionManager.requestGroupedPermissions(null, null)

        verify(permissionsRequestRepository, never()).requestPermissions(any(), any())
    }

    @Test
    fun `requestGroupedPermissions doesn't call the permissionsRequestRepository if permissions are empty`() {
        permissionManager.requestGroupedPermissions(emptyList(), emptyList())

        verify(permissionsRequestRepository, never()).requestPermissions(any(), any())
    }

    @Test
    fun `requestGroupedPermissions calls the necessaryPermissionsGrantedCallback with true if permissions are empty`() {
        val necessaryPermissionsGrantedCallback = mock<PermissionsGrantedCallback>()
        permissionManager.requestGroupedPermissions(
            necessaryPermissions = emptyList(),
            necessaryPermissionsGrantedCallback = necessaryPermissionsGrantedCallback
        )

        verify(necessaryPermissionsGrantedCallback).invoke(true)
    }

    @Test
    fun `requestGroupedPermissions calls the additionalPermissionsGrantedCallback with true if permissions are empty`() {
        val additionalPermissionsGrantedCallback = mock<PermissionsGrantedCallback>()
        permissionManager.requestGroupedPermissions(
            additionalPermissions = emptyList(),
            additionalPermissionsGrantedCallback = additionalPermissionsGrantedCallback
        )

        verify(additionalPermissionsGrantedCallback).invoke(true)
    }

    @Test
    fun `requestGroupedPermissions calls the callback with empty map if permissions are empty`() {
        val callback = mock<PermissionsRequestResult>()
        permissionManager.requestGroupedPermissions(
            callback = callback
        )

        verify(callback).invoke(eq(emptyMap()), eq(null))
    }

    @Test
    fun `requestGroupedPermissions calls the necessaryPermissionsGrantedCallback with false when some permission not granted`() {
        val callback = mock<PermissionsGrantedCallback>()

        permissionManager.requestGroupedPermissions(
            necessaryPermissions = listOf("FirstPermission", "SecondPermission"),
            necessaryPermissionsGrantedCallback = callback
        )
        val argumentCaptor = argumentCaptor<PermissionsRequestResult>()
        verify(permissionsRequestRepository).requestPermissions(any(), argumentCaptor.capture())
        val results = mapOf(
            Pair("FirstPermission", true),
            Pair("SecondPermission", false)
        )
        argumentCaptor.firstValue.invoke(results, null)

        verify(callback).invoke(false)
    }

    @Test
    fun `requestGroupedPermissions calls the necessaryPermissionsGrantedCallback with true when all permission granted`() {
        val callback = mock<PermissionsGrantedCallback>()

        permissionManager.requestGroupedPermissions(
            necessaryPermissions = listOf("FirstPermission", "SecondPermission"),
            necessaryPermissionsGrantedCallback = callback
        )
        val argumentCaptor = argumentCaptor<PermissionsRequestResult>()
        verify(permissionsRequestRepository).requestPermissions(any(), argumentCaptor.capture())
        val results = mapOf(
            Pair("FirstPermission", true),
            Pair("SecondPermission", true)
        )
        argumentCaptor.firstValue.invoke(results, null)

        verify(callback).invoke(true)
    }

    @Test
    fun `requestGroupedPermissions calls the necessaryPermissionsGrantedCallback with true if necessaryPermissions not needed`() {
        val callback = mock<PermissionsGrantedCallback>()

        permissionManager.requestGroupedPermissions(
            additionalPermissions = listOf("FirstPermission", "SecondPermission"),
            necessaryPermissionsGrantedCallback = callback
        )
        val argumentCaptor = argumentCaptor<PermissionsRequestResult>()
        verify(permissionsRequestRepository).requestPermissions(any(), argumentCaptor.capture())
        val results = mapOf(
            Pair("FirstPermission", true),
            Pair("SecondPermission", false)
        )
        argumentCaptor.firstValue.invoke(results, null)

        verify(callback).invoke(true)
    }

    @Test
    fun `requestGroupedPermissions calls the additionalPermissionsGrantedCallback with false when some permission not granted`() {
        val callback = mock<PermissionsGrantedCallback>()

        permissionManager.requestGroupedPermissions(
            additionalPermissions = listOf("FirstPermission", "SecondPermission"),
            additionalPermissionsGrantedCallback = callback
        )
        val argumentCaptor = argumentCaptor<PermissionsRequestResult>()
        verify(permissionsRequestRepository).requestPermissions(any(), argumentCaptor.capture())
        val results = mapOf(
            Pair("FirstPermission", true),
            Pair("SecondPermission", false)
        )
        argumentCaptor.firstValue.invoke(results, null)

        verify(callback).invoke(false)
    }

    @Test
    fun `requestGroupedPermissions calls the additionalPermissionsGrantedCallback with true when all permission granted`() {
        val callback = mock<PermissionsGrantedCallback>()

        permissionManager.requestGroupedPermissions(
            additionalPermissions = listOf("FirstPermission", "SecondPermission"),
            additionalPermissionsGrantedCallback = callback
        )
        val argumentCaptor = argumentCaptor<PermissionsRequestResult>()
        verify(permissionsRequestRepository).requestPermissions(any(), argumentCaptor.capture())
        val results = mapOf(
            Pair("FirstPermission", true),
            Pair("SecondPermission", true)
        )
        argumentCaptor.firstValue.invoke(results, null)

        verify(callback).invoke(true)
    }

    @Test
    fun `requestGroupedPermissions calls the additionalPermissionsGrantedCallback with true if necessaryPermissions not needed`() {
        val callback = mock<PermissionsGrantedCallback>()

        permissionManager.requestGroupedPermissions(
            necessaryPermissions = listOf("FirstPermission", "SecondPermission"),
            additionalPermissionsGrantedCallback = callback
        )
        val argumentCaptor = argumentCaptor<PermissionsRequestResult>()
        verify(permissionsRequestRepository).requestPermissions(any(), argumentCaptor.capture())
        val results = mapOf(
            Pair("FirstPermission", true),
            Pair("SecondPermission", false)
        )
        argumentCaptor.firstValue.invoke(results, null)

        verify(callback).invoke(true)
    }
    //

    @Test
    fun `requestGroupedPermissions calls the callback with results permissionsRequestRepository returns`() {
        val callback = mock<PermissionsRequestResult>()

        permissionManager.requestGroupedPermissions(
            necessaryPermissions = listOf("FirstPermission", "SecondPermission"),
            callback = callback
        )
        val argumentCaptor = argumentCaptor<PermissionsRequestResult>()
        verify(permissionsRequestRepository).requestPermissions(any(), argumentCaptor.capture())
        val results = mapOf(
            Pair("FirstPermission", true),
            Pair("SecondPermission", false)
        )
        argumentCaptor.firstValue.invoke(results, null)

        verify(callback).invoke(eq(results), eq(null))
    }

    @Test
    fun `requestGroupedPermissions calls callbacks when permissionsRequestRepository returns exception`() {
        val necessaryPermissionsGrantedCallback = mock<PermissionsGrantedCallback>()
        val additionalPermissionsGrantedCallback = mock<PermissionsGrantedCallback>()
        val callback = mock<PermissionsRequestResult>()

        permissionManager.requestGroupedPermissions(
            necessaryPermissions = listOf("FirstPermission", "SecondPermission"),
            additionalPermissions = listOf("FirstPermission", "SecondPermission"),
            necessaryPermissionsGrantedCallback = necessaryPermissionsGrantedCallback,
            additionalPermissionsGrantedCallback = additionalPermissionsGrantedCallback,
            callback = callback
        )
        val argumentCaptor = argumentCaptor<PermissionsRequestResult>()
        verify(permissionsRequestRepository).requestPermissions(any(), argumentCaptor.capture())
        val exception = GliaException("TestException", GliaException.Cause.INTERNAL_ERROR)
        argumentCaptor.firstValue.invoke(null, exception)

        verify(necessaryPermissionsGrantedCallback).invoke(false)
        verify(additionalPermissionsGrantedCallback).invoke(false)
        verify(callback).invoke(null, exception)
    }

    // Tests for .requestPermissions()

    @Test
    fun `requestPermissions calls permissionsRequestRepository on trigger`() {
        val permissions = listOf("FirstPermission", "SecondPermission")
        val callback = mock<PermissionsRequestResult>()

        permissionManager.requestPermissions(permissions, callback)

        verify(permissionsRequestRepository).requestPermissions(permissions, callback)
    }

    //Tests for .getPermissionsForMediaUpgradeOffer()

    @Test
    fun `getPermissionsForMediaUpgradeOffer returns only Audio permission when MediaType audio is TWO_WAY and Build version is R or below`() {
        val permissionManager = PermissionManager(
            context,
            checkSelfPermission,
            permissionsRequestRepository,
            Build.VERSION_CODES.R
        )

        val result = permissionManager.getPermissionsForMediaUpgradeOffer(mockMediaUpgradeOffer(audio = MediaDirection.TWO_WAY))

        assertEquals(
            Permissions(
                listOf(Manifest.permission.RECORD_AUDIO),
                emptyList()
            ),
            result
        )
    }

    @Test
    fun `getPermissionsForMediaUpgradeOffer returns Audio and Bluetooth permissions when MediaType audio is TWO_WAY and Build version is above R`() {
        val permissionManager = PermissionManager(
            context,
            checkSelfPermission,
            permissionsRequestRepository,
            Build.VERSION_CODES.S
        )

        val result = permissionManager.getPermissionsForMediaUpgradeOffer(mockMediaUpgradeOffer(audio = MediaDirection.TWO_WAY))

        assertEquals(
            Permissions(
                listOf(Manifest.permission.RECORD_AUDIO),
                listOf(Manifest.permission.BLUETOOTH_CONNECT)
            ),
            result
        )
    }

    @Test
    fun `getPermissionsForMediaUpgradeOffer returns empty list when MediaType video is ONE_WAY, audio is not TWO_WAY and Build version is R or below`() {
        val permissionManager = PermissionManager(
            context,
            checkSelfPermission,
            permissionsRequestRepository,
            Build.VERSION_CODES.R
        )

        val result = permissionManager.getPermissionsForMediaUpgradeOffer(mockMediaUpgradeOffer(video = MediaDirection.ONE_WAY))

        assertEquals(
            Permissions(
                emptyList(),
                emptyList()
            ),
            result
        )
    }

    @Test
    fun `getPermissionsForMediaUpgradeOffer returns empty list when MediaType video is ONE_WAY, audio is not TWO_WAY and Build version is above R`() {
        val permissionManager = PermissionManager(
            context,
            checkSelfPermission,
            permissionsRequestRepository,
            Build.VERSION_CODES.S
        )

        val result = permissionManager.getPermissionsForMediaUpgradeOffer(mockMediaUpgradeOffer(video = MediaDirection.ONE_WAY))

        assertEquals(
            Permissions(
                emptyList(),
                emptyList()
            ),
            result
        )
    }

    @Test
    fun `getPermissionsForMediaUpgradeOffer returns only Camera permission when MediaType video is TWO_WAY and Build version is R or below`() {
        val permissionManager = PermissionManager(
            context,
            checkSelfPermission,
            permissionsRequestRepository,
            Build.VERSION_CODES.R
        )

        val result = permissionManager.getPermissionsForMediaUpgradeOffer(mockMediaUpgradeOffer(video = MediaDirection.TWO_WAY))

        assertEquals(
            Permissions(
                listOf(Manifest.permission.CAMERA),
                emptyList()
            ),
            result
        )
    }

    @Test
    fun `getPermissionsForMediaUpgradeOffer returns only Camera permission when MediaType video is TWO_WAY and Build version is above R`() {
        val permissionManager = PermissionManager(
            context,
            checkSelfPermission,
            permissionsRequestRepository,
            Build.VERSION_CODES.S
        )

        val result = permissionManager.getPermissionsForMediaUpgradeOffer(
            mockMediaUpgradeOffer(
                audio = MediaDirection.ONE_WAY,
                video = MediaDirection.TWO_WAY
            )
        )

        assertEquals(
            Permissions(
                listOf(Manifest.permission.CAMERA),
                emptyList()
            ),
            result
        )
    }

    @Test
    fun `getPermissionsForMediaUpgradeOffer returns Camera and Audio permission when MediaType video is TWO_WAY, audio is TWO_WAY and Build version is R or below`() {
        val permissionManager = PermissionManager(
            context,
            checkSelfPermission,
            permissionsRequestRepository,
            Build.VERSION_CODES.R
        )

        val result = permissionManager.getPermissionsForMediaUpgradeOffer(
            mockMediaUpgradeOffer(
                audio = MediaDirection.TWO_WAY,
                video = MediaDirection.TWO_WAY
            )
        )

        assertEquals(
            Permissions(
                listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                emptyList()
            ),
            result
        )
    }

    @Test
    fun `getPermissionsForMediaUpgradeOffer returns Camera, Audio and Bluetooth permissions when MediaType video is TWO_WAY, audio is TWO_WAY and Build version is above R`() {
        val permissionManager = PermissionManager(
            context,
            checkSelfPermission,
            permissionsRequestRepository,
            Build.VERSION_CODES.S
        )

        val result = permissionManager.getPermissionsForMediaUpgradeOffer(
            mockMediaUpgradeOffer(
                audio = MediaDirection.TWO_WAY,
                video = MediaDirection.TWO_WAY
            )
        )

        assertEquals(
            Permissions(
                listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                listOf(Manifest.permission.BLUETOOTH_CONNECT)
            ),
            result
        )
    }

    @Test
    fun `getPermissionsForMediaUpgradeOffer contains POST_NOTIFICATIONS permission when Build version is 33 or higher`() {
        val permissionManager = PermissionManager(
            context,
            checkSelfPermission,
            permissionsRequestRepository,
            Build.VERSION_CODES.TIRAMISU
        )

        val result = permissionManager.getPermissionsForMediaUpgradeOffer(mockMediaUpgradeOffer(video = MediaDirection.ONE_WAY))

        assertTrue(result.additionalPermissions.contains(Manifest.permission.POST_NOTIFICATIONS))
    }

    @Test
    fun `getPermissionsForMediaUpgradeOffer does not contain POST_NOTIFICATIONS permission when Build version is below 33`() {
        val permissionManager = PermissionManager(
            context,
            checkSelfPermission,
            permissionsRequestRepository,
            Build.VERSION_CODES.S_V2
        )

        val result = permissionManager.getPermissionsForMediaUpgradeOffer(mockMediaUpgradeOffer(video = MediaDirection.ONE_WAY))

        assertFalse(result.additionalPermissions.contains(Manifest.permission.POST_NOTIFICATIONS))
    }

    @Test
    fun `getPermissionsForEngagementMediaType contains POST_NOTIFICATIONS permission when Build version is 33 or higher`() {
        val permissionManager = PermissionManager(
            context,
            checkSelfPermission,
            permissionsRequestRepository,
            Build.VERSION_CODES.TIRAMISU
        )

        val result = permissionManager.getPermissionsForEngagementMediaType(MediaType.AUDIO, true)

        assertTrue(result.additionalPermissions.contains(Manifest.permission.POST_NOTIFICATIONS))
    }

    @Test
    fun `getPermissionsForEngagementMediaType does not contain POST_NOTIFICATIONS permission when Build version is below 33`() {
        val permissionManager = PermissionManager(
            context,
            checkSelfPermission,
            permissionsRequestRepository,
            Build.VERSION_CODES.S_V2
        )

        val result = permissionManager.getPermissionsForEngagementMediaType(MediaType.VIDEO, false)

        assertFalse(result.additionalPermissions.contains(Manifest.permission.POST_NOTIFICATIONS))
    }

    @Test
    fun `shouldShowPermissionRationale calls the similar function from the permissionsRequestRepository`() {
        val permission = Manifest.permission.POST_NOTIFICATIONS
        val shouldShowRationale = true
        whenever(permissionsRequestRepository.shouldShowPermissionRationale(permission)).thenReturn(shouldShowRationale)

        val result = permissionManager.shouldShowPermissionRationale(permission)
        verify(permissionsRequestRepository).shouldShowPermissionRationale(permission)

        assertEquals(shouldShowRationale, result)
    }

    private fun mockMediaUpgradeOffer(audio: MediaDirection? = null, video: MediaDirection? = null): MediaUpgradeOffer =
        TestMediaUpgradeOffer(audio, video)

    private class TestMediaUpgradeOffer(audio: MediaDirection? = null, video: MediaDirection? = null) : MediaUpgradeOffer(audio, video) {
        override fun accept(callback: Consumer<GliaException>?) {}

        override fun decline(callback: Consumer<GliaException>?) {}

    }

}
